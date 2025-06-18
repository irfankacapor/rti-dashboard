package io.dashboard.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GoalSubareaLinkResponse {
    
    private Long goalId;
    private String goalName;
    private Long subareaId;
    private String subareaName;
    private String subareaCode;
    private LocalDateTime createdAt;
} 