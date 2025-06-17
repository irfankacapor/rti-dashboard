package io.dashboard.repository;

import io.dashboard.model.ProcessingJob;
import io.dashboard.model.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, Long> {
    
    // Find by upload job ID
    Optional<ProcessingJob> findByUploadJobId(Long uploadJobId);
    
    // Find by status
    List<ProcessingJob> findByStatus(ProcessingStatus status);
    
    // Find by status and date range
    List<ProcessingJob> findByStatusAndCreatedAtBetween(ProcessingStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find running jobs
    List<ProcessingJob> findByStatusIn(List<ProcessingStatus> statuses);
    
    // Find jobs with errors
    @Query("SELECT pj FROM ProcessingJob pj WHERE pj.errorCount > 0 ORDER BY pj.createdAt DESC")
    List<ProcessingJob> findJobsWithErrors();
    
    // Find jobs by date range
    List<ProcessingJob> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find latest job for upload
    @Query("SELECT pj FROM ProcessingJob pj WHERE pj.uploadJobId = :uploadJobId ORDER BY pj.createdAt DESC")
    List<ProcessingJob> findLatestByUploadJobId(@Param("uploadJobId") Long uploadJobId);
    
    // Count by status
    long countByStatus(ProcessingStatus status);
    
    // Find jobs that have been running too long
    @Query("SELECT pj FROM ProcessingJob pj WHERE pj.status = 'RUNNING' AND pj.startedAt < :cutoffTime")
    List<ProcessingJob> findStuckJobs(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find jobs with high error rates
    @Query("SELECT pj FROM ProcessingJob pj WHERE pj.errorCount > 0 AND (pj.recordsProcessed = 0 OR (pj.errorCount * 100.0 / pj.recordsProcessed) > :errorThreshold)")
    List<ProcessingJob> findJobsWithHighErrorRate(@Param("errorThreshold") Double errorThreshold);
} 