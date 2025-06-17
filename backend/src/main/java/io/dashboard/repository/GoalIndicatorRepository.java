package io.dashboard.repository;

import io.dashboard.entity.GoalIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalIndicatorRepository extends JpaRepository<GoalIndicator, Long> {
    
    List<GoalIndicator> findByGoalId(Long goalId);
    
    Optional<GoalIndicator> findByGoalIdAndIndicatorId(Long goalId, Long indicatorId);
    
    @Query("SELECT gi FROM GoalIndicator gi LEFT JOIN FETCH gi.indicator WHERE gi.goal.id = :goalId")
    List<GoalIndicator> findByGoalIdWithIndicator(Long goalId);
    
    @Query("SELECT gi FROM GoalIndicator gi LEFT JOIN FETCH gi.goal WHERE gi.indicator.id = :indicatorId")
    List<GoalIndicator> findByIndicatorIdWithGoal(Long indicatorId);
    
    boolean existsByGoalIdAndIndicatorId(Long goalId, Long indicatorId);
    
    @Query("SELECT COUNT(gi) FROM GoalIndicator gi WHERE gi.goal.id = :goalId")
    long countByGoalId(Long goalId);
    
    @Query("SELECT COUNT(gi) FROM GoalIndicator gi WHERE gi.indicator.id = :indicatorId")
    long countByIndicatorId(Long indicatorId);
    
    @Query("SELECT SUM(gi.aggregationWeight) FROM GoalIndicator gi WHERE gi.goal.id = :goalId")
    BigDecimal sumWeightsByGoalId(Long goalId);
    
    @Query("SELECT gi FROM GoalIndicator gi LEFT JOIN FETCH gi.indicator LEFT JOIN FETCH gi.goal WHERE gi.goal.id = :goalId")
    List<GoalIndicator> findByGoalIdWithIndicatorAndGoal(Long goalId);
    
    @Query("SELECT gi FROM GoalIndicator gi LEFT JOIN FETCH gi.indicator LEFT JOIN FETCH gi.goal WHERE gi.indicator.id = :indicatorId")
    List<GoalIndicator> findByIndicatorIdWithIndicatorAndGoal(Long indicatorId);
    
    void deleteByGoalIdAndIndicatorId(Long goalId, Long indicatorId);
} 