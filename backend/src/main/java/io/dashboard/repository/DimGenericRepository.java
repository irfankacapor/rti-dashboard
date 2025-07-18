package io.dashboard.repository;

import io.dashboard.model.DimGeneric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DimGenericRepository extends JpaRepository<DimGeneric, Long> {
    
    List<DimGeneric> findByName(String name);
    
    List<DimGeneric> findByValue(String value);
    
    List<DimGeneric> findByNameAndValue(String name, String value);
    
    Optional<DimGeneric> findByDimensionNameAndValue(String dimensionName, String value);
    
    @Query("SELECT dg FROM DimGeneric dg WHERE dg.name LIKE %:name%")
    List<DimGeneric> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT dg FROM DimGeneric dg WHERE dg.value LIKE %:value%")
    List<DimGeneric> findByValueContaining(@Param("value") String value);
} 