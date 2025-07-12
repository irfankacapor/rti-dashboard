package io.dashboard.repository;

import io.dashboard.model.Area;
import io.dashboard.model.Subarea;
import io.dashboard.model.SubareaIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SubareaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubareaRepository subareaRepository;

    @Autowired
    private AreaRepository areaRepository;

    private Area area1;
    private Area area2;
    private Subarea subarea1;
    private Subarea subarea2;
    private Subarea subarea3;

    @BeforeEach
    void setUp() {
        // Clean up
        subareaRepository.deleteAll();
        areaRepository.deleteAll();

        // Create test areas
        area1 = new Area();
        area1.setCode("AREA1");
        area1.setName("Area 1");
        area1 = areaRepository.save(area1);

        area2 = new Area();
        area2.setCode("AREA2");
        area2.setName("Area 2");
        area2 = areaRepository.save(area2);

        // Create test subareas
        subarea1 = new Subarea();
        subarea1.setCode("SUB1");
        subarea1.setName("Subarea 1");
        subarea1.setDescription("Description 1");
        subarea1.setArea(area1);
        subarea1 = subareaRepository.save(subarea1);

        subarea2 = new Subarea();
        subarea2.setCode("SUB2");
        subarea2.setName("Subarea 2");
        subarea2.setDescription("Description 2");
        subarea2.setArea(area1);
        subarea2 = subareaRepository.save(subarea2);

        subarea3 = new Subarea();
        subarea3.setCode("SUB3");
        subarea3.setName("Subarea 3");
        subarea3.setDescription("Description 3");
        subarea3.setArea(area2);
        subarea3 = subareaRepository.save(subarea3);

        // Flush to ensure all entities are persisted
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByCode_shouldReturnSubarea_whenExists() {
        Optional<Subarea> result = subareaRepository.findByCode("SUB1");
        
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("SUB1");
        assertThat(result.get().getName()).isEqualTo("Subarea 1");
        assertThat(result.get().getArea().getId()).isEqualTo(area1.getId());
    }

    @Test
    void findByCode_shouldReturnEmpty_whenNotExists() {
        Optional<Subarea> result = subareaRepository.findByCode("NONEXISTENT");
        
        assertThat(result).isEmpty();
    }

    @Test
    void existsByCode_shouldReturnTrue_whenExists() {
        boolean result = subareaRepository.existsByCode("SUB1");
        
        assertThat(result).isTrue();
    }

    @Test
    void existsByCode_shouldReturnFalse_whenNotExists() {
        boolean result = subareaRepository.existsByCode("NONEXISTENT");
        
        assertThat(result).isFalse();
    }

    @Test
    void findByAreaId_shouldReturnSubareasForArea() {
        List<Subarea> result = subareaRepository.findByAreaId(area1.getId());
        
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(subarea -> subarea.getArea().getId().equals(area1.getId()));
        assertThat(result).extracting("code").containsExactlyInAnyOrder("SUB1", "SUB2");
    }

    @Test
    void findByAreaId_shouldReturnEmpty_whenAreaHasNoSubareas() {
        // Create a new area with no subareas
        Area area3 = new Area();
        area3.setCode("AREA3");
        area3.setName("Area 3");
        area3 = areaRepository.save(area3);
        
        List<Subarea> result = subareaRepository.findByAreaId(area3.getId());
        
        assertThat(result).isEmpty();
    }

    @Test
    void findByAreaId_shouldReturnEmpty_whenAreaNotExists() {
        List<Subarea> result = subareaRepository.findByAreaId(999L);
        
        assertThat(result).isEmpty();
    }

    @Test
    void findAllWithAreaAndIndicators_shouldReturnAllSubareasWithRelationships() {
        List<Subarea> result = subareaRepository.findAllWithAreaAndIndicators();
        
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(subarea -> subarea.getArea() != null);
        assertThat(result).extracting("code").containsExactlyInAnyOrder("SUB1", "SUB2", "SUB3");
    }

    @Test
    void findByIdWithArea_shouldReturnSubareaWithRelationships_whenExists() {
        Optional<Subarea> result = subareaRepository.findByIdWithArea(subarea1.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("SUB1");
        assertThat(result.get().getArea()).isNotNull();
        assertThat(result.get().getArea().getId()).isEqualTo(area1.getId());
    }

    @Test
    void findByIdWithArea_shouldReturnEmpty_whenNotExists() {
        Optional<Subarea> result = subareaRepository.findByIdWithArea(999L);
        
        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldPersistNewSubarea() {
        Subarea newSubarea = new Subarea();
        newSubarea.setCode("NEWSUB");
        newSubarea.setName("New Subarea");
        newSubarea.setDescription("New Description");
        newSubarea.setArea(area1);
        
        Subarea saved = subareaRepository.save(newSubarea);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("NEWSUB");
        assertThat(saved.getName()).isEqualTo("New Subarea");
        assertThat(saved.getArea().getId()).isEqualTo(area1.getId());
        
        // Verify it's actually persisted
        Optional<Subarea> found = subareaRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("NEWSUB");
    }

    @Test
    void save_shouldUpdateExistingSubarea() {
        subarea1.setName("Updated Name");
        subarea1.setDescription("Updated Description");
        
        Subarea updated = subareaRepository.save(subarea1);
        
        assertThat(updated.getId()).isEqualTo(subarea1.getId());
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        
        // Verify the update is persisted
        Optional<Subarea> found = subareaRepository.findById(subarea1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    void delete_shouldRemoveSubarea() {
        Long subareaId = subarea1.getId();
        
        subareaRepository.delete(subarea1);
        
        Optional<Subarea> found = subareaRepository.findById(subareaId);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllSubareas() {
        List<Subarea> result = subareaRepository.findAll();
        
        assertThat(result).hasSize(3);
        assertThat(result).extracting("code").containsExactlyInAnyOrder("SUB1", "SUB2", "SUB3");
    }

    @Test
    void findById_shouldReturnSubarea_whenExists() {
        Optional<Subarea> result = subareaRepository.findById(subarea1.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("SUB1");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Subarea> result = subareaRepository.findById(999L);
        
        assertThat(result).isEmpty();
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        boolean result = subareaRepository.existsById(subarea1.getId());
        
        assertThat(result).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        boolean result = subareaRepository.existsById(999L);
        
        assertThat(result).isFalse();
    }

    @Test
    void count_shouldReturnCorrectCount() {
        long count = subareaRepository.count();
        
        assertThat(count).isEqualTo(3);
    }

    @Test
    void deleteAll_shouldRemoveAllSubareas() {
        subareaRepository.deleteAll();
        
        List<Subarea> result = subareaRepository.findAll();
        assertThat(result).isEmpty();
    }

    @Test
    void deleteAllById_shouldRemoveSpecificSubareas() {
        List<Long> idsToDelete = List.of(subarea1.getId(), subarea2.getId());
        
        subareaRepository.deleteAllById(idsToDelete);
        
        List<Subarea> remaining = subareaRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getCode()).isEqualTo("SUB3");
    }
} 