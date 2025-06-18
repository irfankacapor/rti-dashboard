package io.dashboard.repository;

import io.dashboard.model.GoalIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalIndicatorRepository extends JpaRepository<GoalIndicator, GoalIndicator.GoalIndicatorId> {
    
    List<GoalIndicator> findByGoalId(Long goalId);
    
    List<GoalIndicator> findByIndicatorId(Long indicatorId);
    
    Optional<GoalIndicator> findByGoalIdAndIndicatorId(Long goalId, Long indicatorId);
    
    boolean existsByGoalIdAndIndicatorId(Long goalId, Long indicatorId);
    
    void deleteByGoalId(Long goalId);
    
    void deleteByIndicatorId(Long indicatorId);
    
    @Query("SELECT gi FROM GoalIndicator gi WHERE gi.goal.id = :goalId")
    List<GoalIndicator> findGoalIndicatorsByGoalId(@Param("goalId") Long goalId);
    
    @Query("SELECT gi FROM GoalIndicator gi WHERE gi.indicator.id = :indicatorId")
    List<GoalIndicator> findGoalIndicatorsByIndicatorId(@Param("indicatorId") Long indicatorId);
    
    @Query("SELECT SUM(gi.aggregationWeight) FROM GoalIndicator gi WHERE gi.goal.id = :goalId")
    Double getTotalWeightByGoalId(@Param("goalId") Long goalId);
} 