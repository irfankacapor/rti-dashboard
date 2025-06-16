package io.dashboard.repository;

import io.dashboard.model.UploadJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadJobRepository extends JpaRepository<UploadJob, Long> {
} 