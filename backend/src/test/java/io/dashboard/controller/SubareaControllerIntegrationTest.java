package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.SubareaCreateRequest;
import io.dashboard.dto.SubareaUpdateRequest;
import io.dashboard.model.Area;
import io.dashboard.model.Subarea;
import io.dashboard.model.SubareaIndicator;
import io.dashboard.repository.AreaRepository;
import io.dashboard.repository.SubareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SubareaControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AreaRepository areaRepository;
    @Autowired
    private SubareaRepository subareaRepository;

    private Area area;

    @BeforeEach
    void setup() {
        subareaRepository.deleteAll();
        areaRepository.deleteAll();
        area = new Area();
        area.setCode("AREA1");
        area.setName("Area 1");
        area = areaRepository.save(area);
    }

    @Test
    void createSubarea_shouldSucceed() throws Exception {
        SubareaCreateRequest req = new SubareaCreateRequest();
        req.setCode("SUB1");
        req.setName("Subarea 1");
        req.setAreaId(area.getId());
        mockMvc.perform(post("/api/v1/subareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUB1"));
        assertThat(subareaRepository.findByCode("SUB1")).isPresent();
    }

    @Test
    void createSubarea_shouldFail_duplicateCode() throws Exception {
        Subarea sub = new Subarea();
        sub.setCode("DUP");
        sub.setName("Sub");
        sub.setArea(area);
        subareaRepository.save(sub);
        SubareaCreateRequest req = new SubareaCreateRequest();
        req.setCode("DUP");
        req.setName("Another");
        req.setAreaId(area.getId());
        mockMvc.perform(post("/api/v1/subareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createSubarea_shouldFail_invalidArea() throws Exception {
        SubareaCreateRequest req = new SubareaCreateRequest();
        req.setCode("SUB2");
        req.setName("Subarea 2");
        req.setAreaId(9999L);
        mockMvc.perform(post("/api/v1/subareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getAllSubareas_shouldReturnList() throws Exception {
        Subarea sub = new Subarea();
        sub.setCode("S1" + System.nanoTime());
        sub.setName("Sub 1");
        sub.setArea(area);
        subareaRepository.save(sub);
        mockMvc.perform(get("/api/v1/subareas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value(sub.getCode()));
    }

    @Test
    void getSubareaById_shouldReturnSubarea() throws Exception {
        Subarea sub = new Subarea();
        sub.setCode("S2");
        sub.setName("Sub 2");
        sub.setArea(area);
        sub = subareaRepository.save(sub);
        mockMvc.perform(get("/api/v1/subareas/" + sub.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S2"));
    }

    @Test
    void getSubareaById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/subareas/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSubareasByArea_shouldReturnList() throws Exception {
        Subarea sub = new Subarea();
        sub.setCode("S3");
        sub.setName("Sub 3");
        sub.setArea(area);
        subareaRepository.save(sub);
        mockMvc.perform(get("/api/v1/areas/" + area.getId() + "/subareas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("S3"));
    }

    @Test
    void updateSubarea_shouldUpdateFields() throws Exception {
        Subarea sub = new Subarea();
        sub.setCode("S4");
        sub.setName("Old");
        sub.setArea(area);
        sub = subareaRepository.save(sub);
        SubareaUpdateRequest req = new SubareaUpdateRequest();
        req.setName("Updated");
        req.setDescription("desc");
        req.setAreaId(area.getId());
        mockMvc.perform(put("/api/v1/subareas/" + sub.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateSubarea_shouldReturn404_whenNotFound() throws Exception {
        SubareaUpdateRequest req = new SubareaUpdateRequest();
        req.setName("Name");
        req.setAreaId(area.getId());
        mockMvc.perform(put("/api/v1/subareas/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSubarea_shouldReturn400_whenInvalidArea() throws Exception {
        Subarea sub = new Subarea();
        sub.setCode("S5");
        sub.setName("Sub 5");
        sub.setArea(area);
        sub = subareaRepository.save(sub);
        SubareaUpdateRequest req = new SubareaUpdateRequest();
        req.setName("Name");
        req.setAreaId(9999L);
        mockMvc.perform(put("/api/v1/subareas/" + sub.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void deleteSubarea_shouldDelete_whenNoIndicators() throws Exception {
        Subarea sub = new Subarea();
        sub.setCode("S6");
        sub.setName("Sub 6");
        sub.setArea(area);
        sub = subareaRepository.save(sub);
        mockMvc.perform(delete("/api/v1/subareas/" + sub.getId()))
                .andExpect(status().isNoContent());
        assertThat(subareaRepository.findById(sub.getId())).isEmpty();
    }

    @Test
    void deleteSubarea_shouldReturn400_whenHasIndicators() throws Exception {
        Subarea sub = new Subarea();
        sub.setCode("S7");
        sub.setName("Sub 7");
        sub.setArea(area);
        sub = subareaRepository.save(sub);
        // Simulate indicator presence in memory only
        sub.setSubareaIndicators(List.of(new SubareaIndicator()));
        mockMvc.perform(delete("/api/v1/subareas/" + sub.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
} 