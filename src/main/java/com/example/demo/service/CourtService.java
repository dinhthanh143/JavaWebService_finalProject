package com.example.demo.service;

import com.example.demo.dto.CourtCreateRequest;
import com.example.demo.dto.CourtResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.BadmintonCluster;
import com.example.demo.model.Court;
import com.example.demo.model.CourtImage;
import com.example.demo.repository.ClusterRepository;
import com.example.demo.repository.CourtRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourtService {
    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public Page<CourtResponse> getAllCourts(Integer page, String name){
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, 5);
        String searchName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;

        Page<Court> courts = courtRepository.findAllWithCluster(searchName, pageable);
        return courts.map(court -> CourtResponse.builder()
                .id(court.getId())
                .courtName(court.getCourtName())
                .type(court.getType())
                .isAvailable(court.isAvailable())
                .clusterId(court.getCluster().getId())
                .clusterName(court.getCluster().getClusterName())
                .address(court.getCluster().getAddress())
                .hotLine(court.getCluster().getHotLine())
                .images(court.getImages().stream().map(CourtImage::getImageUrl).toList())
                .build());
    }

    @Transactional
    public CourtResponse createCourt(CourtCreateRequest request) {
        BadmintonCluster cluster = clusterRepository.findById(request.getClusterId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cụm sân cha với ID: " + request.getClusterId()));

        Court court = Court.builder()
                .courtName(request.getCourtName() != null ? request.getCourtName() : request.getCourtName()) // Linh động theo DTO của bạn
                .type(request.getType())
                .isAvailable(true)
                .cluster(cluster)
                .build();

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            List<String> uploadedUrls = cloudinaryService.uploadMultipleFiles(request.getFiles());

            List<CourtImage> courtImages = uploadedUrls.stream()
                    .map(url -> CourtImage.builder()
                            .imageUrl(url)
                            .court(court)
                            .build())
                    .toList();

            court.getImages().addAll(courtImages);
        }

        courtRepository.save(court);

        return CourtResponse.builder()
                .id(court.getId())
                .courtName(court.getCourtName())
                .type(court.getType())
                .isAvailable(court.isAvailable())
                .clusterId(cluster.getId())
                .clusterName(cluster.getClusterName())
                .address(cluster.getAddress())
                .hotLine(cluster.getHotLine())
                .images(court.getImages().stream().map(CourtImage::getImageUrl).toList())
                .build();
    }
}