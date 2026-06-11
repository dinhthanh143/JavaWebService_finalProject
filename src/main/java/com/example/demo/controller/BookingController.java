package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.BookingRequest;
import com.example.demo.dto.BookingResponse;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody BookingRequest request
    ) {
        BookingResponse bookingResponse = bookingService.createBooking(principal.getUser().getId(), request);

        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Đặt lịch sân thành công!")
                .data(bookingResponse)
                .error(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page
    ) {
        Page<BookingResponse> historyPage = bookingService.getMyBookings(principal.getUser().getId(), page);

        ApiResponse<Page<BookingResponse>> response = ApiResponse.<Page<BookingResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy lịch sử đặt sân thành công!")
                .data(historyPage)
                .error(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAllBookings(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "status", required = false) String status
    ) {
        Page<BookingResponse> allBookings = bookingService.getBookingsByStatus(page, status);

        ApiResponse<Page<BookingResponse>> response = ApiResponse.<Page<BookingResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách quản lý đặt lịch thành công!")
                .data(allBookings)
                .error(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<BookingResponse>> changeBookingStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        BookingResponse bookingResponse = bookingService.changeBookingStatus(id, status);

        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật trạng thái đặt sân thành công!")
                .data(bookingResponse)
                .error(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

}
