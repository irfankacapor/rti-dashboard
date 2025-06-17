package io.dashboard.repository;

import io.dashboard.model.DimLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DimLocationRepository extends JpaRepository<DimLocation, Long> {
    
    Optional<DimLocation> findByCode(String code);
    
    Optional<DimLocation> findByName(String name);
    
    Optional<DimLocation> findByValue(String value);
    
    List<DimLocation> findByType(DimLocation.LocationType type);
    
    List<DimLocation> findByParentId(Long parentId);
    
    List<DimLocation> findByLevel(Integer level);
    
    @Query("SELECT dl FROM DimLocation dl WHERE dl.parent IS NULL")
    List<DimLocation> findRootLocations();
    
    @Query("SELECT dl FROM DimLocation dl WHERE dl.name LIKE %:name%")
    List<DimLocation> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT dl FROM DimLocation dl WHERE dl.code LIKE %:code%")
    List<DimLocation> findByCodeContaining(@Param("code") String code);
    
    @Query("SELECT dl FROM DimLocation dl WHERE dl.type = :type AND dl.level = :level")
    List<DimLocation> findByTypeAndLevel(@Param("type") DimLocation.LocationType type, @Param("level") Integer level);
} 