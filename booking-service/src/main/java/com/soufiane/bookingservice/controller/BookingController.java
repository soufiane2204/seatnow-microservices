package com.soufiane.bookingservice.controller;

import com.soufiane.bookingservice.dto.BookingRequestDTO;
import com.soufiane.bookingservice.dto.BookingResponseDTO;
import com.soufiane.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(
            @Valid @RequestBody BookingRequestDTO dto,
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = (Long) authentication.getPrincipal();

        String token = authHeader.substring(7);

        BookingResponseDTO booking =
                bookingService.bookTicket(
                        dto,
                        userId,
                        token
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(booking);
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingResponseDTO>> getMyBookings(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                bookingService.getMyBookings(userId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        String role = authentication
                .getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");

        return ResponseEntity.ok(
                bookingService.getBookingById(
                        id,
                        userId,
                        role
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = (Long) authentication.getPrincipal();

        String token = authHeader.substring(7);

        bookingService.cancelBooking(
                id,
                userId,
                token
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {

        return ResponseEntity.ok(
                bookingService.getAllBookings()
        );
    }
}