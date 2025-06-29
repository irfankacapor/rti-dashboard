package io.dashboard.repository;

import io.dashboard.model.GoalGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalGroupRepository extends JpaRepository<GoalGroup, Long> {
    
    Optional<GoalGroup> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT gg FROM GoalGroup gg LEFT JOIN FETCH gg.goals")
    List<GoalGroup> findAllWithGoals();
    
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.goalGroup.id = :goalGroupId")
    long countGoalsByGoalGroupId(Long goalGroupId);
} 