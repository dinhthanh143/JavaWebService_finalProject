package com.example.demo.service;

import com.example.demo.constraints.BookingStatus;
import com.example.demo.dto.BookingRequest;
import com.example.demo.dto.BookingResponse;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Booking;
import com.example.demo.model.Slot;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.SlotRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SlotRepository slotRepository;

    public Page<BookingResponse> getMyBookings(Long userId, Integer page){
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, 5);
        Page<Booking> bookingPage = bookingRepository.findByUserId(userId, pageable);
        return bookingPage.map(this::mapToBookingResponse);
    }

    public Page<BookingResponse> getBookingsByStatus(Integer page, String status) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, 5, Sort.by("createdAt").descending());

        BookingStatus bookingStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                // Ép kiểu chuỗi text truyền lên từ API thành Enum tương ứng
                bookingStatus = BookingStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Trạng thái tìm kiếm '" + status + "' không hợp lệ!");
            }
        }

        Page<Booking> bookingPage = bookingRepository.findAllByStatus(bookingStatus, pageable);
        return bookingPage.map(this::mapToBookingResponse);
    }

    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng!"));

        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Khung giờ (Slot) này không tồn tại hoặc đã bị xóa!"));

        boolean isSlotTaken = bookingRepository.existsBySlotIdAndBookingDate(request.getSlotId(), request.getBookingDate());
        if (isSlotTaken) {
            throw new BadRequestException("Khung giờ này vào ngày " + request.getBookingDate() + " đã có người đặt rồi. Vui lòng chọn khung giờ khác!");
        }
        Booking booking = Booking.builder()
                .user(user)
                .slot(slot)
                .bookingDate(request.getBookingDate())
                .totalPrice(slot.getPrice())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        bookingRepository.save(booking);
        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponse changeBookingStatus(Long id, String status){
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin đặt sân!"));
        BookingStatus newStatus;
        try {
            newStatus = BookingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Trạng thái '" + status + "' không hợp lệ!");
        }

        booking.setStatus(newStatus);

        if (newStatus == BookingStatus.CANCELLED) {
            booking.setSlot(null);
        }

        return mapToBookingResponse(booking);
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        String courtName = "N/A";
        String clusterName = "N/A";
        String timeSlot = "N/A";
        if (booking.getSlot() != null) {
            timeSlot = booking.getSlot().getStartTime() + " - " + booking.getSlot().getEndTime();
            if (booking.getSlot().getCourt() != null) {
                courtName = booking.getSlot().getCourt().getCourtName();
                if (booking.getSlot().getCourt().getCluster() != null) {
                    clusterName = booking.getSlot().getCourt().getCluster().getClusterName();
                }
            }
        }
        return BookingResponse.builder()
                .id(booking.getId())
                .courtName(courtName)
                .clusterName(clusterName)
                .timeSlot(timeSlot)
                .bookingDate(booking.getBookingDate())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .build();
    }

}
