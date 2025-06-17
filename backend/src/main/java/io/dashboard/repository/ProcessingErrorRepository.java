package io.dashboard.repository;

import io.dashboard.model.ProcessingError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessingErrorRepository extends JpaRepository<ProcessingError, Long> {
    
    // Find by processing job ID
    List<ProcessingError> findByProcessingJobId(Long processingJobId);
    
    // Find by processing job ID and severity
    List<ProcessingError> findByProcessingJobIdAndSeverity(Long processingJobId, String severity);
    
    // Find unresolved errors
    List<ProcessingError> findByProcessingJobIdAndIsResolvedFalse(Long processingJobId);
    
    // Find by error type
    List<ProcessingError> findByProcessingJobIdAndErrorType(Long processingJobId, String errorType);
    
    // Count by processing job ID
    long countByProcessingJobId(Long processingJobId);
    
    // Count by processing job ID and severity
    long countByProcessingJobIdAndSeverity(Long processingJobId, String severity);
    
    // Find errors by row number range
    @Query("SELECT pe FROM ProcessingError pe WHERE pe.processingJob.id = :processingJobId AND pe.rowNumber BETWEEN :startRow AND :endRow")
    List<ProcessingError> findByProcessingJobIdAndRowNumberRange(@Param("processingJobId") Long processingJobId,
                                                                @Param("startRow") Integer startRow,
                                                                @Param("endRow") Integer endRow);
    
    // Find most common error types
    @Query("SELECT pe.errorType, COUNT(pe) FROM ProcessingError pe WHERE pe.processingJob.id = :processingJobId GROUP BY pe.errorType ORDER BY COUNT(pe) DESC")
    List<Object[]> findMostCommonErrorTypes(@Param("processingJobId") Long processingJobId);
    
    // Find errors by column name
    List<ProcessingError> findByProcessingJobIdAndColumnName(Long processingJobId, String columnName);
    
    // Find errors containing specific text in message
    @Query("SELECT pe FROM ProcessingError pe WHERE pe.processingJob.id = :processingJobId AND pe.errorMessage LIKE %:searchText%")
    List<ProcessingError> findByProcessingJobIdAndErrorMessageContaining(@Param("processingJobId") Long processingJobId,
                                                                        @Param("searchText") String searchText);
} 