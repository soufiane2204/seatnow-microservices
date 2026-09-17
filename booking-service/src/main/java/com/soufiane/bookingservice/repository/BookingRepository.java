package com.soufiane.bookingservice.repository;

import com.soufiane.bookingservice.model.Booking;
import com.soufiane.bookingservice.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByEventId(Long eventId);

    Optional<Booking> findByBookingReference(String reference);

    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);
}