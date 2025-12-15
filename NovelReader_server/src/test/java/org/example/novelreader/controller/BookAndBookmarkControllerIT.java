package org.example.novelreader.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.novelreader.dto.BookmarkRequest;
import org.example.novelreader.dto.BookmarkResponse;
import org.example.novelreader.dto.LoginRequest;
import org.example.novelreader.dto.LoginResponse;
import org.example.novelreader.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookAndBookmarkControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    private static final Long TEST_BOOK_ID = 1L;

    @BeforeEach
    void setUp() throws Exception {
        if (jwtToken == null) {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsernameOrEmail("test@example.com");
            loginRequest.setPassword("password");

            var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();

            if (loginResult.getResponse().getStatus() == 401) {
                RegisterRequest registerRequest = new RegisterRequest();
                registerRequest.setUsername("testuser");
                registerRequest.setEmail("test@example.com");
                registerRequest.setPassword("password");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                        .andExpect(status().isCreated());

                loginResult = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andReturn();
            }

            assertThat(loginResult.getResponse().getStatus()).isEqualTo(200);

            String responseJson = loginResult.getResponse().getContentAsString();
            LoginResponse loginResponse = objectMapper.readValue(responseJson, LoginResponse.class);
            jwtToken = "Bearer " + loginResponse.getToken();
        }
    }

    @Test
    void shouldCreateBookmarkForBookWhenValidRequestProvided() throws Exception {
        BookmarkRequest request = new BookmarkRequest();
        request.setBookId(null);
        request.setChapterIndex(0);
        request.setCharacterOffset(0);
        request.setTitle("Zakładka testowa");

        String responseJson = mockMvc.perform(post("/api/v1/books/" + TEST_BOOK_ID + "/bookmarks")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookmarkResponse response = objectMapper.readValue(responseJson, BookmarkResponse.class);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getBookId()).isEqualTo(TEST_BOOK_ID);
        assertThat(response.getChapterIndex()).isEqualTo(0);
    }

    @Test
    void shouldReturnBookmarksForBookWhenTheyExist() throws Exception {
        BookmarkRequest request = new BookmarkRequest();
        request.setBookId(null);
        request.setChapterIndex(0);
        request.setCharacterOffset(0);
        request.setTitle("Zakładka z testu GET");

        mockMvc.perform(post("/api/v1/books/" + TEST_BOOK_ID + "/bookmarks")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        String responseJson = mockMvc.perform(get("/api/v1/books/" + TEST_BOOK_ID + "/bookmarks")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<BookmarkResponse> bookmarks = objectMapper.readValue(responseJson, new TypeReference<List<BookmarkResponse>>() {});

        assertThat(bookmarks).isNotEmpty();
        assertThat(bookmarks.stream().anyMatch(b -> TEST_BOOK_ID.equals(b.getBookId()))).isTrue();
    }
}
