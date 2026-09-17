package com.soufiane.bookingservice.service;

import com.soufiane.bookingservice.client.EventClient;
import com.soufiane.bookingservice.dto.BookingRequestDTO;
import com.soufiane.bookingservice.dto.BookingResponseDTO;
import com.soufiane.bookingservice.dto.EventResponseDTO;
import com.soufiane.bookingservice.exception.InsufficientSeatsException;
import com.soufiane.bookingservice.model.Booking;
import com.soufiane.bookingservice.model.BookingStatus;
import com.soufiane.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private EventClient eventClient;

    @InjectMocks
    private BookingService bookingService;

    private BookingRequestDTO requestDTO;
    private EventResponseDTO event;
    private Booking booking;

    @BeforeEach
    void setUp() {
        requestDTO = new BookingRequestDTO();
        requestDTO.setEventId(1L);
        requestDTO.setSeats(2);

        event = new EventResponseDTO();
        event.setId(1L);
        event.setStatus("PUBLISHED");
        event.setPrice(new BigDecimal("150.00"));
        event.setAvailableSeats(100);

        booking = Booking.builder()
                .id(1L)
                .userId(2L)
                .eventId(1L)
                .seatsBooked(2)
                .totalPrice(new BigDecimal("300.00"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("BK-12345678")
                .createdAt(LocalDateTime.now())
                .build();

    }


    @Test
    void bookTicket_shouldCreateBooking_whenSeatsAreAvailable() {

        when(eventClient.getEventById(1L))
                .thenReturn(event);

        when(eventClient.reserveSeats(1L, 2, "token"))
                .thenReturn(true);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingResponseDTO result =
                bookingService.bookTicket(requestDTO, 2L, "token");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(2, result.getUserId());
        assertEquals(1L, result.getEventId());
        assertEquals(2, result.getSeatsBooked());
        assertEquals(new BigDecimal("300.00"),
                result.getTotalPrice());
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());

        verify(eventClient).getEventById(1L);
        verify(eventClient).reserveSeats(1L, 2, "token");
        verify(bookingRepository).save(any(Booking.class));

    }

    @Test
    void bookTicket_shouldFail_whenEventIsNotPublished() {
        event.setStatus("DRAFT");

        when(eventClient.getEventById(1L)).thenReturn(event);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> bookingService.bookTicket(requestDTO, 2L, "token")
        );

        assertEquals("Event is not open for booking", exception.getMessage());

        verify(eventClient, never())
                .reserveSeats(anyLong(), anyInt(), anyString());

        verify(bookingRepository, never())
                .save(any());
    }


    @Test
    void bookTicket_shouldFail_whenSeatsAreInsufficient() {

        when(eventClient.getEventById(1L)).thenReturn(event);

        when(eventClient.reserveSeats(1L, 2, "token"))
                .thenReturn(false);

        InsufficientSeatsException exception = assertThrows(
                InsufficientSeatsException.class,
                () -> bookingService.bookTicket(requestDTO, 2L, "token")
        );

        assertEquals("Not enough seats available", exception.getMessage());

        verify(bookingRepository, never())
                .save(any());
    }


    @Test
    void getBookingById_shouldReturnBooking_whenRequesterIsOwner() {
        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        BookingResponseDTO result = bookingService.getBookingById(1L, 2L, "USER");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2L, result.getUserId());


    }

    @Test
    void getBookingById_shouldReturnBooking_whenUserIsAdmin() {

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));


        BookingResponseDTO result = bookingService.getBookingById(1L, 999L, "ADMIN");

        assertNotNull(result);
        assertEquals(1L, result.getId());


    }

    @Test
    void getBookingById_shouldFail_whenUserIsNotOwnerOrAdmin() {
        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        assertThrows(SecurityException.class, () -> {
            bookingService.getBookingById(1L, 5L, "USER");
        });

    }

    @Test
    void getMyBookings_shouldReturnUserBookings() {

        when(bookingRepository.findByUserId(2L))
                .thenReturn(List.of(booking));

        List<BookingResponseDTO> result = bookingService.getMyBookings(2L);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());


        verify(bookingRepository).findByUserId(2L);
    }


    @Test
    void getAllBookings_shouldReturnAllBookings() {

        when(bookingRepository.findAll())
                .thenReturn(List.of(booking));

        List<BookingResponseDTO> result = bookingService.getAllBookings();

        assertEquals(1, result.size());

        verify(bookingRepository).findAll();


    }


    @Test
    void cancelBooking_shouldCancelBooking_andReleaseSeats() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));


        bookingService.cancelBooking(1L, 2L, "USER");

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());

        verify(eventClient)
                .releaseSeats(1L, 2, "USER");

        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelBooking_shouldFail_whenAlreadyCancelled() {

        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> bookingService.cancelBooking(
                                1L,
                                2L,
                                "token"
)
                );

        assertEquals("Booking is already cancelled", exception.getMessage());

        verify(eventClient, never()).releaseSeats(anyLong(), anyInt(),anyString());

        verify(bookingRepository, never()).save(any());
    }
}

