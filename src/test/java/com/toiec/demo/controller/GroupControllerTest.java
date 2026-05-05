package com.toiec.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toiec.demo.dtos.request.CreateGroupRequest;
import com.toiec.demo.dtos.request.UpdateGroupRequest;
import com.toiec.demo.dtos.response.GroupResponse;
import com.toiec.demo.security.JwtTokenProvider;
import com.toiec.demo.service.GroupService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
@AutoConfigureMockMvc(addFilters = false)
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupService groupService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private GroupResponse mockGroupResponse;

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

        mockGroupResponse = GroupResponse.builder()
                .id("group-1")
                .name("TOEIC Test Group")
                .description("A group for TOEIC preparation")
                .isPublic(true)
                .build();
    }

    @Test
    void createGroup_ValidRequest_ShouldReturnGroupResponse() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("TOEIC Test Group");
        request.setDescription("A group for TOEIC preparation");
        request.setIsPublic(true);

        when(groupService.createGroup(any(CreateGroupRequest.class), any())).thenReturn(mockGroupResponse);

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("TOEIC Test Group"));
    }

    @Test
    void createGroup_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName(""); // Invalid name (NotBlank)

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateGroup_ValidRequest_ShouldReturnUpdatedGroup() throws Exception {
        UpdateGroupRequest request = new UpdateGroupRequest();
        request.setName("Updated Group Name");

        GroupResponse updatedResponse = GroupResponse.builder()
                .id("group-1")
                .name("Updated Group Name")
                .build();

        when(groupService.updateGroup(anyString(), any(UpdateGroupRequest.class), any())).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/groups/group-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Group Name"));
    }

    @Test
    void deleteGroup_ShouldReturnSuccess() throws Exception {
        doNothing().when(groupService).deleteGroup(anyString(), any());

        mockMvc.perform(delete("/api/groups/group-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getGroup_ShouldReturnGroupResponse() throws Exception {
        when(groupService.getGroupById(anyString(), any())).thenReturn(mockGroupResponse);

        mockMvc.perform(get("/api/groups/group-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("group-1"));
    }

    @Test
    void getPublicGroups_ShouldReturnPagedGroups() throws Exception {
        Page<GroupResponse> page = new PageImpl<>(Collections.singletonList(mockGroupResponse));
        when(groupService.getPublicGroups(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/groups/public")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value("group-1"));
    }

    @Test
    void getMyGroups_ShouldReturnPagedGroups() throws Exception {
        Page<GroupResponse> page = new PageImpl<>(Collections.singletonList(mockGroupResponse));
        when(groupService.getUserGroups(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/groups/my")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value("group-1"));
    }
}
