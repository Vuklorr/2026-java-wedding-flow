package io.github.vuklorr.weddingservice;

import io.github.vuklorr.weddingservice.controller.WeddingController;
import io.github.vuklorr.weddingservice.dto.WeddingResponse;
import io.github.vuklorr.weddingservice.exception.WeddingNotFoundException;
import io.github.vuklorr.weddingservice.service.WeddingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(WeddingController.class)
public class WeddingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeddingService weddingService;

    @Test
    void shouldReturn404WhenWeddingNotFound() throws Exception {
        final int id = 999;

        when(weddingService.readWedding(id))
                .thenThrow(new WeddingNotFoundException(id));

        mockMvc.perform(get("/weddings/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail")
                        .value("Wedding 999 not found"));
    }

    @Test
    void shouldReturn200WhenWeddingFound() throws Exception {
        final int id = 0;
        final String title = "Second wedding";
        final LocalDate date = LocalDate.of(2030, 10, 11);
        final Instant timestamp = Instant.ofEpochSecond(123456789);

        WeddingResponse weddingResponse = new WeddingResponse(id, title, date, timestamp);

        when(weddingService.readWedding(id))
                .thenReturn(weddingResponse);


        mockMvc.perform(get("/weddings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.weddingDate").value(date.toString()))
                .andExpect(jsonPath("$.createdAt").value(timestamp.toString()));

    }
}
