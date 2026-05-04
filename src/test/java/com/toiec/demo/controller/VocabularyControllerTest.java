package com.toiec.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toiec.demo.dtos.request.*;
import com.toiec.demo.dtos.response.FlashcardSessionResponse;
import com.toiec.demo.dtos.response.VocabCardResponse;
import com.toiec.demo.dtos.response.VocabSetResponse;
import com.toiec.demo.security.JwtTokenProvider;
import com.toiec.demo.service.VocabularyService;
import com.toiec.demo.entities.User;
import com.toiec.demo.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VocabularyController.class)
@AutoConfigureMockMvc(addFilters = false)
class VocabularyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VocabularyService vocabularyService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private VocabSetResponse mockSetResponse;
    private VocabCardResponse mockCardResponse;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        user.setRole("USER");
        UserPrincipal userPrincipal = new UserPrincipal(user);
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockSetResponse = VocabSetResponse.builder()
                .id("set-1")
                .title("TOEIC Basic Words")
                .description("Basic vocabulary for TOEIC")
                .isPublic(true)
                .build();

        mockCardResponse = VocabCardResponse.builder()
                .id("card-1")
                .word("abandon")
                .meaning("to leave a place, thing, or person")
                .build();
    }

    // --- Set Tests ---

    @Test
    void createSet_ValidRequest_ShouldReturnSetResponse() throws Exception {
        CreateVocabSetRequest request = new CreateVocabSetRequest();
        request.setTitle("TOEIC Basic Words");
        request.setIsPublic(true);

        when(vocabularyService.createSet(any(CreateVocabSetRequest.class), any())).thenReturn(mockSetResponse);

        mockMvc.perform(post("/api/vocab/sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("TOEIC Basic Words"));
    }

    @Test
    void createSet_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        CreateVocabSetRequest request = new CreateVocabSetRequest();
        request.setTitle(""); // Invalid

        mockMvc.perform(post("/api/vocab/sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSet_ShouldReturnUpdatedSet() throws Exception {
        UpdateVocabSetRequest request = new UpdateVocabSetRequest();
        request.setTitle("Updated Title");

        VocabSetResponse updatedResponse = VocabSetResponse.builder()
                .id("set-1")
                .title("Updated Title")
                .build();

        when(vocabularyService.updateSet(anyString(), any(UpdateVocabSetRequest.class), any())).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/vocab/sets/set-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    void deleteSet_ShouldReturnSuccess() throws Exception {
        doNothing().when(vocabularyService).deleteSet(anyString(), any());

        mockMvc.perform(delete("/api/vocab/sets/set-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getSet_ShouldReturnSet() throws Exception {
        when(vocabularyService.getSetById(anyString(), any())).thenReturn(mockSetResponse);

        mockMvc.perform(get("/api/vocab/sets/set-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("set-1"));
    }

    @Test
    void getPublicSets_ShouldReturnPagedSets() throws Exception {
        Page<VocabSetResponse> page = new PageImpl<>(Collections.singletonList(mockSetResponse));
        when(vocabularyService.getPublicSets(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/vocab/sets/public")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value("set-1"));
    }

    @Test
    void getMySets_ShouldReturnPagedSets() throws Exception {
        Page<VocabSetResponse> page = new PageImpl<>(Collections.singletonList(mockSetResponse));
        when(vocabularyService.getUserSets(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/vocab/sets/my")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value("set-1"));
    }

    // --- Card Tests ---

    @Test
    void addCard_ValidRequest_ShouldReturnCardResponse() throws Exception {
        CreateVocabCardRequest request = new CreateVocabCardRequest();
        request.setWord("abandon");
        request.setMeaning("to leave");

        when(vocabularyService.addCard(anyString(), any(CreateVocabCardRequest.class), any())).thenReturn(mockCardResponse);

        mockMvc.perform(post("/api/vocab/sets/set-1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.word").value("abandon"));
    }

    @Test
    void updateCard_ShouldReturnUpdatedCard() throws Exception {
        UpdateVocabCardRequest request = new UpdateVocabCardRequest();
        request.setMeaning("updated meaning");

        VocabCardResponse updatedCard = VocabCardResponse.builder()
                .id("card-1")
                .word("abandon")
                .meaning("updated meaning")
                .build();

        when(vocabularyService.updateCard(anyString(), any(UpdateVocabCardRequest.class), any())).thenReturn(updatedCard);

        mockMvc.perform(put("/api/vocab/cards/card-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meaning").value("updated meaning"));
    }

    @Test
    void deleteCard_ShouldReturnSuccess() throws Exception {
        doNothing().when(vocabularyService).deleteCard(anyString(), any());

        mockMvc.perform(delete("/api/vocab/cards/card-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getCards_ShouldReturnList() throws Exception {
        List<VocabCardResponse> cards = Collections.singletonList(mockCardResponse);
        when(vocabularyService.getCardsBySet(anyString(), any())).thenReturn(cards);

        mockMvc.perform(get("/api/vocab/sets/set-1/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("card-1"));
    }

    // --- Import Tests ---

    @Test
    void importCards_ShouldReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "content".getBytes());

        doNothing().when(vocabularyService).importCardsFromFile(anyString(), any(), any());

        mockMvc.perform(multipart("/api/vocab/sets/set-1/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // --- Study Tests ---

    @Test
    void getNextFlashcard_ShouldReturnFlashcard() throws Exception {
        FlashcardSessionResponse sessionResponse = FlashcardSessionResponse.builder()
                .cardId("card-1")
                .word("abandon")
                .build();

        when(vocabularyService.getNextFlashcard(any(), anyString())).thenReturn(sessionResponse);

        mockMvc.perform(get("/api/vocab/study/set-1/next")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.word").value("abandon"));
    }

    @Test
    void submitAnswer_ShouldReturnSuccess() throws Exception {
        FlashcardAnswerRequest request = new FlashcardAnswerRequest();
        request.setCardId("card-1");
        request.setQuality(4);

        doNothing().when(vocabularyService).submitAnswer(any(), any(FlashcardAnswerRequest.class));

        mockMvc.perform(post("/api/vocab/study/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
