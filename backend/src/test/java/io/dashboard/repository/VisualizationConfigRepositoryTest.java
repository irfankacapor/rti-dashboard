package io.dashboard.repository;

import io.dashboard.entity.VisualizationConfig;
import io.dashboard.entity.VisualizationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class VisualizationConfigRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VisualizationConfigRepository visualizationConfigRepository;

    @Test
    void findByIndicatorId_shouldReturnConfigs() {
        // Given
        VisualizationConfig config1 = createConfig(1L, VisualizationType.LINE, false);
        VisualizationConfig config2 = createConfig(1L, VisualizationType.BAR, true);
        VisualizationConfig config3 = createConfig(2L, VisualizationType.PIE, false);

        // When
        List<VisualizationConfig> result = visualizationConfigRepository.findByIndicatorId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("visualizationType").containsExactlyInAnyOrder(VisualizationType.LINE, VisualizationType.BAR);
    }

    @Test
    void findByIndicatorId_withNoConfigs_shouldReturnEmptyList() {
        // When
        List<VisualizationConfig> result = visualizationConfigRepository.findByIndicatorId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByIndicatorIdAndIsDefault_shouldReturnDefaultConfigs() {
        // Given
        VisualizationConfig config1 = createConfig(1L, VisualizationType.LINE, false);
        VisualizationConfig config2 = createConfig(1L, VisualizationType.BAR, true);
        VisualizationConfig config3 = createConfig(2L, VisualizationType.PIE, true);

        // When
        List<VisualizationConfig> result = visualizationConfigRepository.findByIndicatorIdAndIsDefault(1L, true);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVisualizationType()).isEqualTo(VisualizationType.BAR);
        assertThat(result.get(0).isDefault()).isTrue();
    }

    @Test
    void findByIndicatorIdAndIsDefault_withNoDefaults_shouldReturnEmptyList() {
        // Given
        createConfig(1L, VisualizationType.LINE, false);
        createConfig(1L, VisualizationType.BAR, false);

        // When
        List<VisualizationConfig> result = visualizationConfigRepository.findByIndicatorIdAndIsDefault(1L, true);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByIndicatorIdAndIsDefault_withNonDefaults_shouldReturnNonDefaultConfigs() {
        // Given
        VisualizationConfig config1 = createConfig(1L, VisualizationType.LINE, false);
        VisualizationConfig config2 = createConfig(1L, VisualizationType.BAR, true);
        VisualizationConfig config3 = createConfig(2L, VisualizationType.PIE, false);

        // When
        List<VisualizationConfig> result = visualizationConfigRepository.findByIndicatorIdAndIsDefault(1L, false);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVisualizationType()).isEqualTo(VisualizationType.LINE);
        assertThat(result.get(0).isDefault()).isFalse();
    }

    @Test
    void findByVisualizationType_shouldReturnConfigs() {
        // Given
        VisualizationConfig config1 = createConfig(1L, VisualizationType.LINE, false);
        VisualizationConfig config2 = createConfig(2L, VisualizationType.LINE, true);
        VisualizationConfig config3 = createConfig(3L, VisualizationType.BAR, false);

        // When
        List<VisualizationConfig> result = visualizationConfigRepository.findByVisualizationType(VisualizationType.LINE);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("indicatorId").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void findByVisualizationType_withNoConfigs_shouldReturnEmptyList() {
        // When
        List<VisualizationConfig> result = visualizationConfigRepository.findByVisualizationType(VisualizationType.PIE);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldPersistConfig() {
        // Given
        VisualizationConfig config = new VisualizationConfig();
        config.setIndicatorId(1L);
        config.setVisualizationType(VisualizationType.LINE);
        config.setConfig("{\"title\":\"Test Config\"}");
        config.setDefault(false);
        config.setCreatedAt(LocalDateTime.now());

        // When
        VisualizationConfig saved = visualizationConfigRepository.save(config);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIndicatorId()).isEqualTo(1L);
        assertThat(saved.getVisualizationType()).isEqualTo(VisualizationType.LINE);
    }

    @Test
    void findById_shouldReturnConfig() {
        // Given
        VisualizationConfig config = createConfig(1L, VisualizationType.LINE, false);

        // When
        var result = visualizationConfigRepository.findById(config.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIndicatorId()).isEqualTo(1L);
    }

    @Test
    void findById_withNonExistentId_shouldReturnEmpty() {
        // When
        var result = visualizationConfigRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_shouldRemoveConfig() {
        // Given
        VisualizationConfig config = createConfig(1L, VisualizationType.LINE, false);

        // When
        visualizationConfigRepository.deleteById(config.getId());

        // Then
        assertThat(visualizationConfigRepository.findById(config.getId())).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllConfigs() {
        // Given
        createConfig(1L, VisualizationType.LINE, false);
        createConfig(2L, VisualizationType.BAR, true);
        createConfig(3L, VisualizationType.PIE, false);

        // When
        List<VisualizationConfig> result = visualizationConfigRepository.findAll();

        // Then
        assertThat(result).hasSize(3);
    }

    private VisualizationConfig createConfig(Long indicatorId, VisualizationType type, boolean isDefault) {
        VisualizationConfig config = new VisualizationConfig();
        config.setIndicatorId(indicatorId);
        config.setVisualizationType(type);
        config.setConfig("{\"title\":\"Test Config\"}");
        config.setDefault(isDefault);
        config.setCreatedAt(LocalDateTime.now());
        return entityManager.persistAndFlush(config);
    }
} 