package io.dashboard.repository;

import io.dashboard.model.DataType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DataTypeRepository extends JpaRepository<DataType, Long> {
    @Query("SELECT COUNT(i) > 0 FROM Indicator i WHERE i.dataType.id = :dataTypeId")
    boolean hasIndicators(@Param("dataTypeId") Long dataTypeId);
} 