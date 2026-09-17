package com.soufiane.bookingservice.integration;

import com.soufiane.bookingservice.client.EventClient;
import com.soufiane.bookingservice.dto.BookingRequestDTO;
import com.soufiane.bookingservice.dto.BookingResponseDTO;
import com.soufiane.bookingservice.dto.EventResponseDTO;
import com.soufiane.bookingservice.model.Booking;
import com.soufiane.bookingservice.model.BookingStatus;
import com.soufiane.bookingservice.repository.BookingRepository;
import com.soufiane.bookingservice.service.BookingService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @MockitoBean
    private EventClient eventClient;

    private BookingRequestDTO requestDTO;
    private EventResponseDTO event;


    @BeforeEach
    void setUp(){
        requestDTO = new BookingRequestDTO();
        requestDTO.setEventId(1L);
        requestDTO.setSeats(2);

        event = new EventResponseDTO();
        event.setId(1L);
        event.setStatus("PUBLISHED");
        event.setPrice(new BigDecimal("150.00"));
        event.setAvailableSeats(100);
    }


    @Test
    void bookTicket_shouldSaveBookingInDatabase() {
        when(eventClient.getEventById(1L))
                .thenReturn(event);

        when(eventClient.reserveSeats(1L, 2, "token"))
                .thenReturn(true);

        BookingResponseDTO result = bookingService.bookTicket(requestDTO, 2L, "token");

        assertNotNull(result);
        assertNotNull(result.getId());

        assertEquals(2L, result.getUserId());
        assertEquals(1L, result.getEventId());
        assertEquals(2, result.getSeatsBooked());
        assertEquals(new BigDecimal("300.00"), result.getTotalPrice());
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());


        Booking savedBooking = bookingRepository.findById(result.getId()).orElseThrow();

        assertEquals(2L, savedBooking.getUserId());
        assertEquals(1L, savedBooking.getEventId());
        assertEquals(BookingStatus.CONFIRMED, savedBooking.getStatus());

        verify(eventClient).reserveSeats(1L, 2, "token");

    }
    @Test
    void getMyBookings_shouldReturnBookingsFromDatabase() {

        Booking booking = Booking.builder()
                .userId(4L)
                .eventId(1L)
                .seatsBooked(2)
                .totalPrice(new BigDecimal("300.00"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("BK-INTEGRATION-1")
                .build();

        bookingRepository.save(booking);
        List<BookingResponseDTO> result = bookingService.getMyBookings(4L);

        assertFalse(result.isEmpty());
        assertEquals(4L, result.get(0).getUserId());
        assertEquals(BookingStatus.CONFIRMED, result.get(0).getStatus());

    }
    @Test
    void cancelBooking_shouldUpdateDatabase() {
        Booking booking = Booking.builder()
                .userId(2L)
                .eventId(1L)
                .seatsBooked(2)
                .totalPrice(new BigDecimal("300.00"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("BK-INTEGRATION-2")
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        bookingService.cancelBooking(savedBooking.getId(), 2L, "token");

        Booking cancelledBooking = bookingRepository.findById(savedBooking.getId()).orElseThrow();

        assertEquals(BookingStatus.CANCELLED, cancelledBooking.getStatus());

        verify(eventClient).releaseSeats(1L, 2, "token");


    }
}
