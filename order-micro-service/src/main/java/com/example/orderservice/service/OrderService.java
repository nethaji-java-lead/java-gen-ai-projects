package com.example.orderservice.service;


import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.enums.EventStatus;
import com.example.orderservice.enums.EventType;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.event.*;
import com.example.orderservice.exception.OrderException;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderEvent;
import com.example.orderservice.model.User;
import com.example.orderservice.repository.OrderEventRepository;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.UUID;

import static com.example.orderservice.constants.OrderServiceConstants.*;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final OrderEventRepository orderEventRepository;

    private final JsonMapper jsonMapper;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, OrderEventRepository orderEventRepository, JsonMapper jsonMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderEventRepository = orderEventRepository;
        this.jsonMapper = jsonMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderDTO createOrder(OrderRequest request, String idempotencyKey) {
        Order order = new Order();

        // 1. Check if request was already processed
        Optional<Order> existingOrder =
                orderRepository.findByIdempotencyKey(idempotencyKey);

        if (existingOrder.isPresent()) {
            order =  existingOrder.get();
        } else {
            // 2. Create new order
            order.setOrderNumber(generateOrderNumber());
            order.setCustomerId(request.customerId());
            order.setIdempotencyKey(idempotencyKey);
            order.setProductId(request.productId());
            order.setProductCode(request.productCode());
            order.setQuantity(request.quantity());
        }

        order = orderRepository.save(order);

        // Create OrderEvent - Transactional Outbox Database
        createOrderEvent(order);

       return mapToOrderDTO(order);
    }

    @Transactional
    public OrderDTO readOrder(Long id) {
        Optional<Order> existingOrder = orderRepository.findById(id);
        return existingOrder
                .map(this::mapToOrderDTO)
                .orElseThrow(() -> new OrderException("Order not found for ID: "+id));
    }

    private void createOrderEvent(Order order) {
        orderEventRepository
                .findByOrderId(order.getId())
                .ifPresentOrElse(
                        existingEvent -> {
                            // Event already exists - do nothing
                            throw new OrderException("Order Event already exists!");
                        },
                        () -> {
                            OrderEvent orderEvent = new OrderEvent();
                            orderEvent.setOrderId(order.getId());
                            orderEvent.setEventType(EventType.ORDER_CREATED);
                            orderEvent.setTopic(ORDER_CREATED_EVENT);
                            orderEvent.setPayload(
                                    convertToJson(createOrderCreatedEvent(order))
                            );
                            orderEvent.setStatus(EventStatus.PENDING);

                            orderEventRepository.save(orderEvent);
                        }
                );
    }

    private OrderCreatedEvent createOrderCreatedEvent(Order order) {

        /*Optional<User> optionalUser = userRepository.findById(order.getCustomerId());

        User user = optionalUser.orElseThrow(() ->
                new RuntimeException("User not found: " + order.getCustomerId()));*/


        User user = User.builder()
                .name("John Doe")
                .address("123 Main Street, Chennai")
                .shippingAddress("45 Anna Nagar, Chennai")
                .email("nethaji.techlead@gmail.com")
                .phone("8056134756")
                .emailNotifications(true)
                .smsNotifications(true)
                .build();

        return new OrderCreatedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                user.getEmail(),
                user.getPhone(),
                user.isEmailNotifications(),
                user.isSmsNotifications(),
                order.getTotalAmount(),
                order.getProductId(),
                order.getProductCode(),
                order.getQuantity()
        );
    }

    private String convertToJson(OrderCreatedEvent event) {
        try {
            return jsonMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new OrderException("Failed to serialize OrderCreatedEvent", e);
        }
    }



    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private OrderDTO mapToOrderDTO(Order order)
    {
        return new OrderDTO(order.getId()
                , order.getOrderNumber(), order.getCustomerId(), order.getTotalAmount(), order.getStatus()
        , order.getCreatedAt(), order.getUpdatedAt());
    }

    @KafkaListener(
            topics = PAYMENT_COMPLETED_EVENT,
            groupId = "order-service"
    )
    public void consumePaymentCompletedEvent(String message) {
        try {
            PaymentCompletedEvent event = jsonMapper.readValue(message, PaymentCompletedEvent.class);
            Optional<Order> optionalOrder = orderRepository.findById(event.orderId());

            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
            } else {
                throw new OrderException("Order not found for ID: " + event.orderId());
            }
        } catch (Exception e) {
            throw new OrderException("Failed to process payment.completed event", e);
        }
    }

    @KafkaListener(
            topics = PAYMENT_FAILED_EVENT,
            groupId = "order-service"
    )
    public void consumePaymentFailedEvent(String message) {

        try {

            PaymentFailedEvent event =
                    jsonMapper.readValue(message, PaymentFailedEvent.class);

            Order order = orderRepository
                    .findById(event.orderId())
                    .orElseThrow(() ->
                            new OrderException(
                                    "Order not found: " + event.orderId()
                            )
                    );

            order.setStatus(OrderStatus.PAYMENT_FAILED);

            orderRepository.save(order);

        } catch (Exception e) {
            throw new OrderException(
                    "Failed to process payment.failed event", e
            );
        }
    }

    @KafkaListener(
            topics = INVENTORY_FAILED_EVENT,
            groupId = "order-service"
    )
    public void consumeInventoryFail(String message) {


            InventoryFailedEvent event = jsonMapper.readValue(message, InventoryFailedEvent.class);

        try {
            log.warn(
                    "Inventory failed for order {}. Reason: {}",
                    event.orderNumber(),
                    event.reason()
            );

            markInventoryFailed(event.orderId(), event.reason()
            );

        } catch (Exception e) {

            log.error(
                    "Failed to process inventory.failed event: {}",
                    event.toString(),
                    e
            );

            throw new IllegalStateException(
                    "Unable to process inventory.failed event",
                    e
            );
        }
    }

    @Transactional
    private void markInventoryFailed(Long orderId, String failureReason) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found: " + orderId
                        ));

        order.setStatus(OrderStatus.INVENTORY_FAILED);
        order.setFailureReason(failureReason);

        orderRepository.save(order);
    }

}
