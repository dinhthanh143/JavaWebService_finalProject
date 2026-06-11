package com.example.demo.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtResponse {
    private Long id;
    private String courtName;
    private String type;
    private boolean isAvailable;
    private List<String> images;
    private Long clusterId;
    private String clusterName;
    private String address;
    private String hotLine;
}