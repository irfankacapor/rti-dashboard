package io.dashboard.repository;

import io.dashboard.model.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalTypeRepository extends JpaRepository<GoalType, Long> {
    
    Optional<GoalType> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT gt FROM GoalType gt LEFT JOIN FETCH gt.goals")
    List<GoalType> findAllWithGoals();
    
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.goalType.id = :goalTypeId")
    long countGoalsByGoalTypeId(Long goalTypeId);
} 