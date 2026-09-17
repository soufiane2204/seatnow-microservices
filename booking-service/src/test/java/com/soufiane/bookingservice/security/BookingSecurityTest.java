package com.soufiane.bookingservice.security;

import com.soufiane.bookingservice.controller.BookingController;
import com.soufiane.bookingservice.dto.BookingResponseDTO;
import com.soufiane.bookingservice.model.BookingStatus;
import com.soufiane.bookingservice.service.BookingService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({JwtAuthFilter.class, SecurityConfig.class})
public class BookingSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private JwtService jwtService;


    @Test
    void getMyBookings_shouldReject_whenTokenIsMissing() throws Exception{

        mockMvc.perform(
                get("/api/bookings/me"))
                .andExpect(status().isForbidden());

    }

    @Test
    void getAllBookings_shouldReturn403_whenUserIsNotAdmin() throws Exception{
        when(jwtService.isTokenValid("user-token")).thenReturn(true);
        when(jwtService.extractRole("user-token")).thenReturn("USER");
        when(jwtService.extractUserId("user-token")).thenReturn(2L);

        mockMvc.perform(
                get("/api/bookings")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllBookings_shouldReturn200_whenUserIsAdmin() throws Exception{
        when(jwtService.isTokenValid("admin-token")).thenReturn(true);
        when(jwtService.extractRole("admin-token")).thenReturn("ADMIN");
        when(jwtService.extractUserId("admin-token")).thenReturn(99L);

        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(1L)
                .userId(2L)
                .eventId(1L)
                .seatsBooked(2)
                .totalPrice(new BigDecimal("300.00"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("BK-12345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.getAllBookings()).thenReturn(List.of(response));

        mockMvc.perform(
                get("/api/bookings")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(2L));

    }

    @Test
    void getBookings_shouldReturn200_whenUserTokenIsValid() throws Exception {
        when(jwtService.isTokenValid("user-token")).thenReturn(true);
        when(jwtService.extractRole("user-token")).thenReturn("USER");
        when(jwtService.extractUserId("user-token")).thenReturn(2L);

        when(bookingService.getMyBookings(2L)).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/bookings/me")
                                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk());

    }

    @Test
    void createBooking_shouldReturn201_whenUserTokenIsValid() throws Exception {
        when(jwtService.isTokenValid("user-token")).thenReturn(true);
        when(jwtService.extractRole("user-token")).thenReturn("USER");
        when(jwtService.extractUserId("user-token")).thenReturn(2L);

        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(1L)
                .userId(2L)
                .eventId(1L)
                .seatsBooked(2)
                .totalPrice(new BigDecimal("300.00"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("BK-12345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.bookTicket(any(), eq(2L), eq("user-token")))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/bookings")
                                .header("Authorization", "Bearer user-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "eventId": 1,
                                            "seats": 2
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(jsonPath("$.status").value(BookingStatus.CONFIRMED.name()));
    }

    @Test
    void getMyBookings_shouldReject_whenTokenIsInvalid() throws Exception {
        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        mockMvc.perform(
                        get("/api/bookings/me")
                                .header("Authorization",
                                        "Bearer invalid-token"
                                )
                )
                .andExpect(status().isForbidden());
    }


}
