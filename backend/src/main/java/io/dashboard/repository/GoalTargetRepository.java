package io.dashboard.repository;

import io.dashboard.entity.GoalTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalTargetRepository extends JpaRepository<GoalTarget, Long> {
    
    List<GoalTarget> findByGoalId(Long goalId);
    
    Optional<GoalTarget> findByGoalIdAndId(Long goalId, Long targetId);
    
    @Query("SELECT gt FROM GoalTarget gt LEFT JOIN FETCH gt.indicator WHERE gt.goal.id = :goalId")
    List<GoalTarget> findByGoalIdWithIndicator(Long goalId);
} 