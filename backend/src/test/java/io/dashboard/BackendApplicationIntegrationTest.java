package io.dashboard;

import io.dashboard.model.Area;
import io.dashboard.model.Subarea;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BackendApplicationIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveArea() {
        // Create and save Area
        Area area = new Area();
        area.setCode("TEST_AREA");
        area.setName("Test Area");
        area.setDescription("Test Area Description");

        entityManager.persist(area);
        entityManager.flush();

        // Retrieve Area
        Area retrievedArea = entityManager.find(Area.class, area.getId());

        assert retrievedArea != null;
        assert "TEST_AREA".equals(retrievedArea.getCode());
        assert "Test Area".equals(retrievedArea.getName());
        assert "Test Area Description".equals(retrievedArea.getDescription());
    }

    @Test
    void shouldSaveAndRetrieveSubareaWithAreaRelationship() {
        // Create and save Area
        Area area = new Area();
        area.setCode("TEST_AREA");
        area.setName("Test Area");
        area.setDescription("Test Area Description");

        entityManager.persist(area);
        entityManager.flush();

        // Create and save Subarea
        Subarea subarea = new Subarea();
        subarea.setCode("TEST_SUBAREA");
        subarea.setName("Test Subarea");
        subarea.setDescription("Test Subarea Description");
        subarea.setArea(area);

        entityManager.persist(subarea);
        entityManager.flush();

        // Retrieve Subarea
        Subarea retrievedSubarea = entityManager.find(Subarea.class, subarea.getId());

        assert retrievedSubarea != null;
        assert "TEST_SUBAREA".equals(retrievedSubarea.getCode());
        assert "Test Subarea".equals(retrievedSubarea.getName());
        assert "Test Subarea Description".equals(retrievedSubarea.getDescription());
        assert retrievedSubarea.getArea() != null;
        assert area.getId().equals(retrievedSubarea.getArea().getId());
    }

    @Test
    void shouldVerifyAreaSubareaRelationship() {
        // Create and save Area
        Area area = new Area();
        area.setCode("TEST_AREA");
        area.setName("Test Area");
        area.setDescription("Test Area Description");

        entityManager.persist(area);
        entityManager.flush();

        // Create and save Subarea
        Subarea subarea = new Subarea();
        subarea.setCode("TEST_SUBAREA");
        subarea.setName("Test Subarea");
        subarea.setDescription("Test Subarea Description");
        subarea.setArea(area);

        entityManager.persist(subarea);
        entityManager.flush();

        // Refresh area to load subareas
        entityManager.refresh(area);

        assert area.getSubareas() != null;
        assert area.getSubareas().size() == 1;
        assert subarea.getId().equals(area.getSubareas().get(0).getId());
    }
} 