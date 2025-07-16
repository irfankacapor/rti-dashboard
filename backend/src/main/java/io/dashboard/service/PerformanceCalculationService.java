package io.dashboard.service;

import io.dashboard.model.*;
import io.dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceCalculationService {
    private final PerformanceScoreRepository performanceScoreRepository;
    private final ColorThresholdRepository colorThresholdRepository;
    private final SubareaRepository subareaRepository;
    private final AreaRepository areaRepository;
    private final IndicatorRepository indicatorRepository;

    public PerformanceScore calculateAreaPerformance(Long areaId) {
        Area area = areaRepository.findById(areaId).orElseThrow(() -> new RuntimeException("Area not found"));
        List<Subarea> subareas = subareaRepository.findByAreaId(areaId);
        if (subareas.isEmpty()) throw new RuntimeException("No subareas in area");
        double sum = 0.0;
        int count = 0;
        for (Subarea sub : subareas) {
            List<PerformanceScore> scores = performanceScoreRepository.findBySubareaId(sub.getId());
            if (!scores.isEmpty()) {
                sum += scores.get(scores.size() - 1).getScore();
                count++;
            }
        }
        double avg = count > 0 ? sum / count : 0.0;
        String colorCode = getColorCodeForScore(avg);
        // Not stored, just returned
        return new PerformanceScore(null, avg, colorCode, LocalDateTime.now(), null);
    }

    public void calculateAllPerformances() {
        List<Subarea> subareas = subareaRepository.findAll();
        for (Subarea sub : subareas) {
            // calculateSubareaPerformance(sub.getId()); // This line is removed
        }
    }

    public String getColorCodeForScore(Double score) {
        List<ColorThreshold> thresholds = colorThresholdRepository.findAll();
        for (ColorThreshold t : thresholds) {
            if (score >= t.getMinValue() && score <= t.getMaxValue()) {
                return t.getColorCode();
            }
        }
        return "#000000"; // default black
    }

    public void updateColorThresholds(List<ColorThreshold> thresholds) {
        colorThresholdRepository.deleteAll();
        colorThresholdRepository.saveAll(thresholds);
    }

    public List<PerformanceScore> getPerformanceHistory(Long subareaId, int months) {
        // For demo, return all; in real, filter by date
        return performanceScoreRepository.findBySubareaId(subareaId);
    }
} 