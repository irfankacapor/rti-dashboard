package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.AreaCreateRequest;
import io.dashboard.dto.AreaUpdateRequest;
import io.dashboard.model.Area;
import io.dashboard.repository.AreaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AreaControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AreaRepository areaRepository;

    @BeforeEach
    void setup() {
        areaRepository.deleteAll();
    }

    @Test
    void createArea_shouldReturn201_andArea() throws Exception {
        AreaCreateRequest req = new AreaCreateRequest();
        req.setCode("A1");
        req.setName("Area 1");
        req.setDescription("desc");
        mockMvc.perform(post("/api/v1/areas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("A1"));
    }

    @Test
    void createArea_shouldReturn409_onDuplicateCode() throws Exception {
        Area area = new Area();
        area.setCode("A2");
        area.setName("Area 2");
        areaRepository.save(area);
        AreaCreateRequest req = new AreaCreateRequest();
        req.setCode("A2");
        req.setName("Area 2");
        mockMvc.perform(post("/api/v1/areas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getAllAreas_shouldReturnList() throws Exception {
        Area area = new Area();
        area.setCode("A3");
        area.setName("Area 3");
        areaRepository.save(area);
        mockMvc.perform(get("/api/v1/areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("A3"));
    }

    @Test
    void getAreaById_shouldReturnArea() throws Exception {
        Area area = new Area();
        area.setCode("A4");
        area.setName("Area 4");
        area = areaRepository.save(area);
        mockMvc.perform(get("/api/v1/areas/" + area.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A4"));
    }

    @Test
    void getAreaById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/areas/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateArea_shouldUpdateFields() throws Exception {
        Area area = new Area();
        area.setCode("A5");
        area.setName("Area 5");
        area = areaRepository.save(area);
        AreaUpdateRequest req = new AreaUpdateRequest();
        req.setName("Updated");
        req.setDescription("desc");
        mockMvc.perform(put("/api/v1/areas/" + area.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteArea_shouldDelete_whenNoSubareas() throws Exception {
        Area area = new Area();
        area.setCode("A6");
        area.setName("Area 6");
        area = areaRepository.save(area);
        mockMvc.perform(delete("/api/v1/areas/" + area.getId()))
                .andExpect(status().isNoContent());
        assertThat(areaRepository.findById(area.getId())).isEmpty();
    }

    @Test
    void deleteArea_shouldReturn400_whenHasSubareas() throws Exception {
        Area area = new Area();
        area.setCode("A7");
        area.setName("Area 7");
        area = areaRepository.save(area);
        // Add a subarea
        io.dashboard.model.Subarea subarea = new io.dashboard.model.Subarea();
        subarea.setCode("S1" + System.nanoTime());
        subarea.setName("Subarea 1");
        subarea.setArea(area);
        area.getSubareas().add(subarea);
        areaRepository.save(area);
        mockMvc.perform(delete("/api/v1/areas/" + area.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
} 