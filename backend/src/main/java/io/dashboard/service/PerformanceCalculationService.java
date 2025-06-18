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
    private final SubareaIndicatorRepository subareaIndicatorRepository;

    public PerformanceScore calculateSubareaPerformance(Long subareaId) {
        Subarea subarea = subareaRepository.findById(subareaId).orElseThrow(() -> new RuntimeException("Subarea not found"));
        List<SubareaIndicator> subareaIndicators = subareaIndicatorRepository.findBySubareaId(subareaId);
        if (subareaIndicators.isEmpty()) {
            throw new RuntimeException("No indicators linked to subarea");
        }
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        List<Long> indicatorIds = new ArrayList<>();
        for (SubareaIndicator si : subareaIndicators) {
            Indicator indicator = si.getIndicator();
            if (indicator == null) continue;
            // For demo, use a mock value; in real, fetch latest value
            double value = 80.0; // TODO: fetch real value
            double weight = si.getAggregationWeight() != null ? si.getAggregationWeight() : 1.0;
            weightedSum += value * weight;
            totalWeight += weight;
            indicatorIds.add(si.getId().getIndicatorId());
        }
        double score = totalWeight > 0 ? weightedSum / totalWeight : 0.0;
        score = Math.max(0, Math.min(100, score));
        String colorCode = getColorCodeForScore(score);
        PerformanceScore ps = new PerformanceScore(subareaId, score, colorCode, LocalDateTime.now(), indicatorIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        return performanceScoreRepository.save(ps);
    }

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
            calculateSubareaPerformance(sub.getId());
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