package com.soufiane.bookingservice.dto;

import com.soufiane.bookingservice.model.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {

    private Long id;

    private Long userId;

    private Long eventId;

    private int seatsBooked;

    private BigDecimal totalPrice;

    private BookingStatus status;

    private String bookingReference;

    private LocalDateTime createdAt;
}