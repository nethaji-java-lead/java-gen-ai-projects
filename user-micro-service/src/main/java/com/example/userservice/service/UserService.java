package com.example.userservice.service;

import com.example.userservice.dto.UserDTO;
import com.example.userservice.dto.UserRequest;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDTO createUser(UserRequest request) {

        User user = User.builder()
                .name(request.name())
                .address(request.address())
                .shippingAddress(request.shippingAddress())
                .email(request.email())
                .phone(request.phone())
                .emailNotifications(request.emailNotifications())
                .smsNotifications(request.smsNotifications())
                .build();

        User savedCustomer = userRepository.save(user);

        return mapToDTO(savedCustomer);
    }

    @Transactional(readOnly = true)
    public UserDTO getUser(Long customerId) {

        User user = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer not found: " + customerId));

        return mapToDTO(user);
    }

    private UserDTO mapToDTO(User user) {

        return new UserDTO(
                user.getUserId(),
                user.getName(),
                user.getAddress(),
                user.getShippingAddress(),
                user.getEmail(),
                user.getPhone(),
                user.isEmailNotifications(),
                user.isSmsNotifications()
        );
    }
}
