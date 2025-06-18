package io.dashboard.repository;

import io.dashboard.model.GoalSubarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalSubareaRepository extends JpaRepository<GoalSubarea, GoalSubarea.GoalSubareaId> {
    
    List<GoalSubarea> findByGoalId(Long goalId);
    
    List<GoalSubarea> findBySubareaId(Long subareaId);
    
    Optional<GoalSubarea> findByGoalIdAndSubareaId(Long goalId, Long subareaId);
    
    boolean existsByGoalIdAndSubareaId(Long goalId, Long subareaId);
    
    void deleteByGoalId(Long goalId);
    
    void deleteBySubareaId(Long subareaId);
    
    @Query("SELECT gs FROM GoalSubarea gs WHERE gs.goal.id = :goalId")
    List<GoalSubarea> findGoalSubareasByGoalId(@Param("goalId") Long goalId);
    
    @Query("SELECT gs FROM GoalSubarea gs WHERE gs.subarea.id = :subareaId")
    List<GoalSubarea> findGoalSubareasBySubareaId(@Param("subareaId") Long subareaId);
} 