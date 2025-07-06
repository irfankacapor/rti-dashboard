package io.dashboard.repository;

import io.dashboard.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    
    @Query("SELECT g FROM Goal g LEFT JOIN FETCH g.targets WHERE g.id = :goalId")
    Goal findByIdWithTargets(Long goalId);
    
    @Query("SELECT g FROM Goal g LEFT JOIN FETCH g.goalIndicators gi LEFT JOIN FETCH gi.indicator WHERE g.id = :goalId")
    Goal findByIdWithIndicators(Long goalId);
    

    
    @Query("SELECT COUNT(gt) FROM GoalTarget gt WHERE gt.goal.id = :goalId")
    long countTargetsByGoalId(Long goalId);

    List<Goal> findByGoalGroupId(Long goalGroupId);

    @Query("SELECT g FROM Goal g LEFT JOIN FETCH g.targets WHERE g.goalGroup.id = :goalGroupId")
    List<Goal> findByGoalGroupIdWithTargets(Long goalGroupId);
} 