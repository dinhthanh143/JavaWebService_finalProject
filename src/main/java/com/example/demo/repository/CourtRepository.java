package com.example.demo.repository;

import com.example.demo.model.Court;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    @Query(value = "SELECT c FROM Court c JOIN FETCH c.cluster " +
            "WHERE (:courtName IS NULL OR LOWER(c.courtName) LIKE LOWER(CONCAT('%', :courtName, '%')))",
            countQuery = "SELECT COUNT(c) FROM Court c WHERE (:courtName IS NULL OR LOWER(c.courtName) LIKE LOWER(CONCAT('%', :courtName, '%')))")
    Page<Court> findAllWithCluster(@Param("courtName") String courtName, Pageable pageable);

}
