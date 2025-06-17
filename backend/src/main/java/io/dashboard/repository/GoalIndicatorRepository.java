package io.dashboard.repository;

import io.dashboard.entity.GoalIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalIndicatorRepository extends JpaRepository<GoalIndicator, Long> {
    
    List<GoalIndicator> findByGoalId(Long goalId);
    
    Optional<GoalIndicator> findByGoalIdAndIndicatorId(Long goalId, Long indicatorId);
    
    @Query("SELECT gi FROM GoalIndicator gi LEFT JOIN FETCH gi.indicator WHERE gi.goal.id = :goalId")
    List<GoalIndicator> findByGoalIdWithIndicator(Long goalId);
    
    boolean existsByGoalIdAndIndicatorId(Long goalId, Long indicatorId);
} 