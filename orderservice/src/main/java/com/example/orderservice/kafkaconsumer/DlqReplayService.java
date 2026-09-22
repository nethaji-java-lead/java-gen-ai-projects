package com.example.orderservice.kafkaconsumer;
import com.example.orderservice.enums.DlqStatus;
import com.example.orderservice.event.PaymentCreatedEvent;

import com.example.orderservice.model.DlqEvent;
import com.example.orderservice.repository.DlqEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DlqReplayService {

    private static final String PAYMENT_CREATED_TOPIC =
            "payment-created";

    private final KafkaTemplate<String, PaymentCreatedEvent>
            kafkaTemplate;

    private final DlqEventRepository dlqEventRepository;


    @Transactional
    public void replay(PaymentCreatedEvent event) {

        String eventId = event.eventId();

        log.info(
                "Checking DLQ event before replay. eventId={}, paymentId={}",
                eventId,
                event.paymentId()
        );

        /*
         * 1. Check whether the event exists in DLQ.
         */
        DlqEvent dlqEvent =
                dlqEventRepository
                        .findByEventId(eventId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Event not found in DLQ. "
                                                + "eventId=" + eventId
                                )
                        );


        /*
         * 2. Prevent replaying an event that has
         *    already been successfully replayed.
         */
        if (dlqEvent.getStatus() == DlqStatus.REPLAYED) {

            throw new IllegalStateException(
                    "Event has already been replayed. "
                            + "eventId=" + eventId
            );
        }


        /*
         * 3. Only FAILED events are eligible for replay.
         */
        if (dlqEvent.getStatus() != DlqStatus.FAILED) {

            throw new IllegalStateException(
                    "Event is not eligible for replay. "
                            + "eventId=" + eventId
                            + ", status="
                            + dlqEvent.getStatus()
            );
        }


        /*
         * 4. Replay the event to the original topic.
         */
        log.info(
                "Replaying DLQ event. eventId={}, paymentId={}",
                eventId,
                event.paymentId()
        );

        try {

            kafkaTemplate.send(
                    PAYMENT_CREATED_TOPIC,
                    String.valueOf(event.paymentId()),
                    event
            ).get();


            /*
             * 5. Mark the DLQ record as replayed.
             */
            dlqEvent.setStatus(
                    DlqStatus.REPLAYED
            );

            dlqEventRepository.save(dlqEvent);

            log.info(
                    "DLQ event replayed successfully. "
                            + "eventId={}, paymentId={}",
                    eventId,
                    event.paymentId()
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to replay DLQ event. eventId={}",
                    eventId,
                    ex
            );

            throw new IllegalStateException(
                    "Failed to replay DLQ event. "
                            + "eventId=" + eventId,
                    ex
            );
        }
    }
}