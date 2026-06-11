package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "court_name", nullable = false, length = 50)
    private String courtName;

    @Column(length = 50)
    private String type;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private BadmintonCluster cluster;

    // cascade = CascadeType.ALL: Khi lưu/sửa/xóa Court thì đống ảnh tự động được xử lý theo
    // orphanRemoval = true: Khi  xóa 1 ảnh ra khỏi List, Hibernate tự xóa bản ghi đó dưới DB luôn
    @Builder.Default
    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourtImage> images = new ArrayList<>();
}