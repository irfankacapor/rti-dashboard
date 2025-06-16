package io.dashboard.repository;

import io.dashboard.model.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {
    List<UploadFile> findByUploadJobId(Long jobId);
} 