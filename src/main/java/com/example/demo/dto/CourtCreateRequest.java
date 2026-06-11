package com.example.demo.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourtCreateRequest {
    private String courtName;
    private String type;
    private Long clusterId;

    private List<MultipartFile> files;
}