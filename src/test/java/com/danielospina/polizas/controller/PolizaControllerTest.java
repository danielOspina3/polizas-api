package com.danielospina.polizas.controller;

import com.danielospina.polizas.config.ApiKeyFilter;
import com.danielospina.polizas.dto.CrearRiesgoRequest;
import com.danielospina.polizas.exception.GlobalExceptionHandler;
import com.danielospina.polizas.exception.ReglaNegocioException;
import com.danielospina.polizas.service.PolizaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PolizaControllerTest {

    private static final String API_KEY = "123456";

    private PolizaService polizaService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        polizaService = mock(PolizaService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PolizaController(polizaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new ApiKeyFilter(API_KEY))
                .build();
    }

    @Test
    void sinApiKeyResponde401() throws Exception {
        mockMvc.perform(get("/polizas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conApiKeyListaLasPolizas() throws Exception {
        when(polizaService.listar(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/polizas").header(ApiKeyFilter.HEADER_API_KEY, API_KEY))
                .andExpect(status().isOk());
    }

    @Test
    void reglaDeNegocioVioladaResponde422() throws Exception {
        when(polizaService.agregarRiesgo(eq(1L), any(CrearRiesgoRequest.class)))
                .thenThrow(new ReglaNegocioException("Solo se pueden agregar riesgos a polizas colectivas"));

        mockMvc.perform(post("/polizas/1/riesgos")
                        .header(ApiKeyFilter.HEADER_API_KEY, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"direccionInmueble":"Calle 1","arrendatario":"Juan","arrendador":"Pedro","canonMensual":1000000}
                                """))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.mensaje").value("Solo se pueden agregar riesgos a polizas colectivas"));
    }

    @Test
    void datosInvalidosResponden400() throws Exception {
        mockMvc.perform(post("/polizas/2/riesgos")
                        .header(ApiKeyFilter.HEADER_API_KEY, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"direccionInmueble":"","arrendatario":"Juan","arrendador":"Pedro","canonMensual":-5}
                                """))
                .andExpect(status().isBadRequest());
    }
}
