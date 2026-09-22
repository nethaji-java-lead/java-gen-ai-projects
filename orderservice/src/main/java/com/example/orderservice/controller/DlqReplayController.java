package com.example.orderservice.controller;

import com.example.orderservice.event.PaymentCreatedEvent;
import com.example.orderservice.kafkaconsumer.DlqReplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/kafka")
@RequiredArgsConstructor
public class DlqReplayController {

    private final DlqReplayService replayService;

    @PostMapping("/replay")
    public ResponseEntity<String> replay(
            @RequestBody PaymentCreatedEvent event) {

        replayService.replay(event);

        return ResponseEntity.ok(
                "Event replayed successfully"
        );
    }
}