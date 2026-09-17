package com.soufiane.bookingservice.service;

import com.soufiane.bookingservice.client.EventClient;
import com.soufiane.bookingservice.dto.BookingRequestDTO;
import com.soufiane.bookingservice.dto.BookingResponseDTO;
import com.soufiane.bookingservice.exception.InsufficientSeatsException;
import com.soufiane.bookingservice.model.Booking;
import com.soufiane.bookingservice.model.BookingStatus;
import com.soufiane.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventClient eventClient;

    public BookingResponseDTO bookTicket(
            BookingRequestDTO dto,
            Long userId,
            String token) {

        var event = eventClient.getEventById(dto.getEventId());

        if (!"PUBLISHED".equals(event.getStatus())) {
            throw new IllegalStateException("Event is not open for booking");
        }

        boolean reserved = eventClient.reserveSeats(
                dto.getEventId(),
                dto.getSeats(),
                token
        );

        if (!reserved) {
            throw new InsufficientSeatsException("Not enough seats available");
        }

        BigDecimal totalPrice = event.getPrice()
                .multiply(BigDecimal.valueOf(dto.getSeats()));

        Booking booking = Booking.builder()
                .userId(userId)
                .eventId(dto.getEventId())
                .seatsBooked(dto.getSeats())
                .totalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED)
                .bookingReference(generateBookingReference())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        return toResponseDTO(savedBooking);
    }

    public BookingResponseDTO getBookingById(
            Long id,
            Long requesterId,
            String requesterRole) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        boolean isOwner = booking.getUserId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);

        if (!isOwner && !isAdmin) {
            throw new SecurityException(
                    "You are not allowed to access this booking"
            );
        }

        return toResponseDTO(booking);
    }

    public List<BookingResponseDTO> getMyBookings(Long userId) {

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<BookingResponseDTO> getAllBookings() {

        return bookingRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public void cancelBooking(
            Long id,
            Long requesterId,
            String token) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (!booking.getUserId().equals(requesterId)) {
            throw new SecurityException(
                    "You are not allowed to cancel this booking"
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Booking is already cancelled"
            );
        }

        eventClient.releaseSeats(
                booking.getEventId(),
                booking.getSeatsBooked(),
                token
        );

        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);
    }

    private String generateBookingReference() {

        return "BK-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }

    private BookingResponseDTO toResponseDTO(Booking booking) {

        return BookingResponseDTO.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .eventId(booking.getEventId())
                .seatsBooked(booking.getSeatsBooked())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .bookingReference(booking.getBookingReference())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}