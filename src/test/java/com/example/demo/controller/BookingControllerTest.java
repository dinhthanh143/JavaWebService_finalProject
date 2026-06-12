package com.example.demo.controller;

import com.example.demo.dto.BookingRequest;
import com.example.demo.dto.BookingResponse;
import com.example.demo.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Chặn đứng Filter Security can thiệp vào test
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    // --- TEST 1: POST /api/v1/bookings/create -> THÀNH CÔNG ---
    @Test
    void createBooking_Success() throws Exception {
        BookingResponse response = BookingResponse.builder()
                .id(100L)
                .courtName("Sân Số 1")
                .clusterName("Cụm Sân Bình Thạnh")
                .timeSlot("08:00 - 09:00")
                .bookingDate(LocalDate.parse("2026-06-20"))
                .totalPrice(BigDecimal.valueOf(150000.0))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.createBooking(any(), any(BookingRequest.class))).thenReturn(response);

        // Viết chuỗi JSON trực tiếp để loại bỏ hoàn toàn lỗi cấu hình ObjectMapper của hệ thống
        String rawJsonRequestBody = "{\"slotId\":1,\"bookingDate\":\"2026-06-20\"}";

        mockMvc.perform(post("/api/v1/bookings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJsonRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Đặt lịch sân thành công!"))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.courtName").value("Sân Số 1"));
    }

    // --- TEST 2: GET /api/v1/bookings/my-history -> THÀNH CÔNG ---
    @Test
    void getMyBookings_Success() throws Exception {
        Page<BookingResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        when(bookingService.getMyBookings(any(), anyInt())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/bookings/my-history")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy lịch sử đặt sân thành công!"));
    }

    // --- TEST 3: GET /api/v1/bookings (Không truyền status) -> THÀNH CÔNG ---
    @Test
    void getAllBookings_WithoutStatus_Success() throws Exception {
        Page<BookingResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        when(bookingService.getBookingsByStatus(anyInt(), eq(null))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/bookings")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách quản lý đặt lịch thành công!"));
    }

    // --- TEST 4: GET /api/v1/bookings?status=PENDING -> THÀNH CÔNG ---
    @Test
    void getAllBookings_WithStatus_Success() throws Exception {
        Page<BookingResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        when(bookingService.getBookingsByStatus(anyInt(), anyString())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/bookings")
                        .param("page", "1")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    // --- TEST 5: PUT /api/v1/bookings/{id}/status -> THÀNH CÔNG ---
    @Test
    void changeBookingStatus_Success() throws Exception {
        BookingResponse response = BookingResponse.builder()
                .id(1L)
                .courtName("Sân Số 1")
                .status("CANCELLED")
                .build();

        when(bookingService.changeBookingStatus(anyLong(), anyString())).thenReturn(response);

        mockMvc.perform(put("/api/v1/bookings/1/status")
                        .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật trạng thái đặt sân thành công!"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}