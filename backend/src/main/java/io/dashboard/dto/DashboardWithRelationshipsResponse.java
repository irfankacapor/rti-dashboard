package io.dashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DashboardWithRelationshipsResponse {
    private List<AreaResponse> areas;
    private List<SubareaResponse> subareas;
    private List<GoalResponse> goals;
    private List<GoalGroupResponse> goalGroups;
    private RelationshipMappings relationships;
    private LocalDateTime lastUpdated;

    @Data
    public static class RelationshipMappings {
        private Map<String, List<String>> goalToSubareas; // goal ID → subarea IDs
        private Map<String, List<String>> subareaToGoals; // subarea ID → goal IDs
    }
} 