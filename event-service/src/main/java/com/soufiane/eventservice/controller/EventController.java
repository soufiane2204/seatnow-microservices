package com.soufiane.eventservice.controller;

import com.soufiane.eventservice.dto.EventRequestDTO;
import com.soufiane.eventservice.dto.EventResponseDTO;
import com.soufiane.eventservice.security.JwtService;
import com.soufiane.eventservice.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllPublishedEvents() {
        return ResponseEntity.ok(eventService.getAllPublishedEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO dto,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);

        Long organizerId = jwtService.extractUserId(token);

        EventResponseDTO created =
                eventService.createEvent(dto, organizerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequestDTO dto) {

        return ResponseEntity.ok(
                eventService.updateEvent(id, dto)
        );
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<EventResponseDTO> publishEvent(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                eventService.publishEvent(id)
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EventResponseDTO> cancelEvent(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                eventService.cancelEvent(id)
        );
    }
    @PostMapping("/{id}/reserve")
    public ResponseEntity<Integer> reserveSeats(
            @PathVariable Long id,
            @RequestParam int seats) {

        int rowsAffected = eventService.reserveSeats(id, seats);

        return ResponseEntity.ok(rowsAffected);
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<Void> releaseSeats(
            @PathVariable Long id,
            @RequestParam int seats) {

        eventService.releaseSeats(id, seats);

        return ResponseEntity.ok().build();
    }
}