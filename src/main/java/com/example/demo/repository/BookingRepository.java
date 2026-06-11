package com.example.demo.repository;

import com.example.demo.constraints.BookingStatus;
import com.example.demo.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsBySlotIdAndBookingDateAndStatusIn(Long slotId, LocalDate bookingDate, List<BookingStatus> statuses);
    @Query(value = "SELECT b FROM Booking b " +
            "JOIN FETCH b.slot s " +
            "JOIN FETCH s.court c " +
            "JOIN FETCH c.cluster " +
            "WHERE b.user.id = :userId ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId")
    Page<Booking> findByUserId(@Param("userId") Long userId, Pageable pageable);

    boolean existsBySlotIdAndBookingDate(Long slotId, LocalDate bookingDate);

    @Query("SELECT b FROM Booking b WHERE (:status IS NULL OR b.status = :status)")
    Page<Booking> findAllByStatus(@Param("status") BookingStatus status, Pageable pageable);
}
