package io.dashboard.repository;

import io.dashboard.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    
    List<Goal> findByGoalTypeId(Long goalTypeId);
    
    @Query("SELECT g FROM Goal g LEFT JOIN FETCH g.targets LEFT JOIN FETCH g.indicators WHERE g.id = :id")
    Optional<Goal> findByIdWithTargetsAndIndicators(Long id);
    
    @Query("SELECT COUNT(gt) FROM GoalTarget gt WHERE gt.goal.id = :goalId")
    long countTargetsByGoalId(Long goalId);
    
    @Query("SELECT COUNT(gi) FROM GoalIndicator gi WHERE gi.goal.id = :goalId")
    long countIndicatorsByGoalId(Long goalId);
} 