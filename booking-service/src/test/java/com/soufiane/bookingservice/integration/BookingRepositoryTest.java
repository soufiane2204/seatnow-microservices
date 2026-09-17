package com.soufiane.bookingservice.integration;

import com.soufiane.bookingservice.model.Booking;
import com.soufiane.bookingservice.model.BookingStatus;
import com.soufiane.bookingservice.repository.BookingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // that's for using the real database instead of an in-memory one H2 , and spring will use the database configuration from application.properties because in usual @DataJpaTest it uses H2 in-memory database by default
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    private Booking booking;


    @BeforeEach
    void setUp(){

        booking = Booking.builder()
                .userId(2L)
                .eventId(1L)
                .seatsBooked(2)
                .totalPrice(new BigDecimal("300.00"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("BK-TEST1234")
                .createdAt(LocalDateTime.now())
                .build(); // WE CREATE A BOOKING OBJECT TO USE IN TESTS
    }

    @Test
    void save_shouldSaveBooking() {

        Booking savedBooking = bookingRepository.save(booking);

        assertNotNull(savedBooking);
        assertNotNull(savedBooking.getId());

        assertEquals(2L , savedBooking.getUserId());
        assertEquals(1L , savedBooking.getEventId());
        assertEquals(2 , savedBooking.getSeatsBooked());
        assertEquals(BookingStatus.CONFIRMED , savedBooking.getStatus());


    }

    @Test
    void findById_shouldReturnBooking_whenBookingExists() {

        Booking savedBooking = bookingRepository.save(booking);

        Optional<Booking> result = bookingRepository.findById(savedBooking.getId());

        assertTrue(result.isPresent());
        assertEquals(savedBooking.getId(), result.get().getId());
        assertEquals(2L , result.get().getUserId());

    }
    @Test
    void findByBookingReference_shouldReturnBooking() {
        bookingRepository.save(booking);

        Optional<Booking> result = bookingRepository.findByBookingReference("BK-TEST1234");

        assertTrue(result.isPresent());
        assertEquals("BK-TEST1234", result.get().getBookingReference());

    }
    @Test
    void findById_shouldReturnEmpty_whenBookingDoesNotExist() {

        Optional<Booking> result = bookingRepository.findById(999L);

        assertTrue(result.isEmpty());
    }



}
