package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "badminton_clusters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadmintonCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String clusterName;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "hot_line", length = 20)
    private String hotLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Builder.Default
    @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL)
    private List<Court> courts = new ArrayList<>();
}