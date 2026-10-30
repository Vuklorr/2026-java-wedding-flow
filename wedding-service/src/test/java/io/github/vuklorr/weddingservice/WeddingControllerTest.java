package io.github.vuklorr.weddingservice;

import io.github.vuklorr.weddingservice.controller.WeddingController;
import io.github.vuklorr.weddingservice.dto.CreateWeddingRequest;
import io.github.vuklorr.weddingservice.dto.UpdateWeddingDateRequest;
import io.github.vuklorr.weddingservice.dto.WeddingResponse;
import io.github.vuklorr.weddingservice.exception.WeddingNotFoundException;
import io.github.vuklorr.weddingservice.service.WeddingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void shouldReturn404WhenUpdateWeddingNotFound() throws Exception {
        final int id = 999;
        final LocalDate date = LocalDate.of(2030, 10, 11);

        when(weddingService.updateWeddingDate(id, new UpdateWeddingDateRequest(date)))
                .thenThrow(new WeddingNotFoundException(id));

        mockMvc.perform(
                        patch("/weddings/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "weddingDate": "2030-10-11"
                        }
                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail")
                        .value("Wedding 999 not found"));
    }

    @Test
    void shouldReturn200WhenUpdateWedding() throws Exception {
        final int id = 0;
        final String title = "Second wedding";
        final LocalDate newDate = LocalDate.of(2035, 1, 22);
        final Instant timestamp = Instant.ofEpochSecond(123456789);

        WeddingResponse weddingResponse = new WeddingResponse(id, title, newDate, timestamp);

        when(weddingService.updateWeddingDate(id, new UpdateWeddingDateRequest(newDate)))
                .thenReturn(weddingResponse);


        mockMvc.perform(
                        patch("/weddings/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "weddingDate": "2035-01-22"
                        }
                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.weddingDate").value(newDate.toString()))
                .andExpect(jsonPath("$.createdAt").value(timestamp.toString()));

    }

    @Test
    void shouldReturn404WhenDeleteWeddingNotFound() throws Exception {
        final int id = 999;

        doThrow(new WeddingNotFoundException(id))
                .when(weddingService)
                .deleteWedding(id);

        mockMvc.perform(delete("/weddings/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail")
                        .value("Wedding 999 not found"));
    }

    @Test
    void shouldReturn204WhenDeleteWedding() throws Exception {
        final int id = 0;
        mockMvc.perform(delete("/weddings/{id}", id))
                .andExpect(status().isNoContent());
        verify(weddingService).deleteWedding(id);
    }

    @Test
    void shouldReturn201WhenCreateWedding() throws Exception {
        final int id = 0;
        final String title = "Second wedding";
        final LocalDate date = LocalDate.of(2035, 1, 22);
        final Instant timestamp = Instant.ofEpochSecond(123456789);

        CreateWeddingRequest createWeddingRequest = new CreateWeddingRequest(title, date);
        WeddingResponse weddingResponse = new WeddingResponse(id, title, date, timestamp);

        when(weddingService.createWedding(createWeddingRequest))
                .thenReturn(weddingResponse);


        mockMvc.perform(
                        post("/weddings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "title" : "Second wedding",
                            "weddingDate" : "2035-01-22"
                        }
                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(weddingResponse.id()))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.weddingDate").value(date.toString()))
                .andExpect(jsonPath("$.createdAt").value(timestamp.toString()));

    }

    @Test
    void shouldReturn400WhenTitleIsEmpty() throws Exception {
        mockMvc.perform(
                        post("/weddings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "title" : "",
                            "weddingDate" : "2035-01-22"
                        }
                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Название свадьбы не может быть пустым"));

    }

    @Test
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        mockMvc.perform(
                        post("/weddings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "title": "  ",
                                "weddingDate": "2035-01-22"
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Название свадьбы не может быть пустым"));

    }

    @Test
    void shouldReturn400WhenWeddingDateIsAbsent() throws Exception {
        mockMvc.perform(
                        post("/weddings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "title" : "Wedding"
                        }
                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Некорректные данные запроса"))
                .andExpect(jsonPath("$.errors[0].field")
                        .value("weddingDate"));

    }

    @Test
    void shouldReturn400WhenWeddingDateIsNull() throws Exception {
        mockMvc.perform(
                        patch("/weddings/{id}", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "weddingDate" : null
                        }
                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Некорректные данные запроса"));

    }

    @Test
    void shouldReturn400WhenWeddingDateIsIncorrect() throws Exception {
        mockMvc.perform(
                        patch("/weddings/{id}", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "weddingDate" : "2035-02-30"
                        }
                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Неверно переданы данные в теле запроса"));

    }

}
