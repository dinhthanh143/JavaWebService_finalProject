package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CourtCreateRequest;
import com.example.demo.dto.CourtResponse;
import com.example.demo.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/courts")
public class CourtController {
    @Autowired
    private CourtService courtService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourtResponse>>> getAllCourts(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "name", required = false) String name
    ) {
        Page<CourtResponse> courtsPage = courtService.getAllCourts(page, name);

        ApiResponse<Page<CourtResponse>> response = ApiResponse.<Page<CourtResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách sân đấu thành công!")
                .data(courtsPage)
                .error(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<CourtResponse>> createCourt(@ModelAttribute CourtCreateRequest request) {
        CourtResponse courtResponse = courtService.createCourt(request);

        ApiResponse<CourtResponse> response = ApiResponse.<CourtResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo sân đấu mới thành công!")
                .data(courtResponse)
                .error(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
