package com.soufiane.bookingservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequestDTO {

    @NotNull(message = "eventId is required")
    private Long eventId;

    @Min(value = 1, message = "seats must be at least 1")
    private int seats;
}