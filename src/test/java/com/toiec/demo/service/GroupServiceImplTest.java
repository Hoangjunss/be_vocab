package com.toiec.demo.service;

import com.toiec.demo.dtos.request.CreateGroupRequest;
import com.toiec.demo.dtos.request.UpdateGroupRequest;
import com.toiec.demo.dtos.response.GroupResponse;
import com.toiec.demo.entities.Group;
import com.toiec.demo.entities.User;
import com.toiec.demo.exception.BusinessRuleException;
import com.toiec.demo.exception.ResourceNotFoundException;
import com.toiec.demo.mapper.GroupMapper;
import com.toiec.demo.repository.GroupRepository;
import com.toiec.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupMapper groupMapper;

    @InjectMocks
    private GroupServiceImpl groupService;

    private Group mockGroup;
    private UUID groupId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        groupId = UUID.randomUUID();
        userId = UUID.randomUUID();

        mockGroup = new Group();
        mockGroup.setId(groupId);
        mockGroup.setName("TOEIC Group");
        mockGroup.setCreatedBy(userId);

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setFullName("Group Creator");
        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
    }

    @Test
    void createGroup_ShouldSaveAndReturnResponse() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("TOEIC Group");

        GroupResponse expectedResponse = GroupResponse.builder().name("TOEIC Group").build();

        when(groupMapper.toEntity(request)).thenReturn(mockGroup);
        when(groupRepository.save(any(Group.class))).thenReturn(mockGroup);
        when(groupMapper.toResponse(mockGroup)).thenReturn(expectedResponse);

        GroupResponse response = groupService.createGroup(request, userId.toString());

        assertThat(response.getName()).isEqualTo("TOEIC Group");
        verify(groupRepository).save(mockGroup);
    }

    @Test
    void updateGroup_WhenCreator_ShouldUpdateAndReturnResponse() {
        UpdateGroupRequest request = new UpdateGroupRequest();
        request.setName("Updated Group");

        Group updatedGroup = new Group();
        updatedGroup.setId(groupId);
        updatedGroup.setName("Updated Group");
        updatedGroup.setCreatedBy(userId);

        GroupResponse expectedResponse = GroupResponse.builder().name("Updated Group").build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.save(mockGroup)).thenReturn(updatedGroup);
        when(groupMapper.toResponse(updatedGroup)).thenReturn(expectedResponse);

        GroupResponse response = groupService.updateGroup(groupId.toString(), request, userId.toString());

        assertThat(response.getName()).isEqualTo("Updated Group");
    }

    @Test
    void updateGroup_WhenNotCreator_ShouldThrowException() {
        UpdateGroupRequest request = new UpdateGroupRequest();
        String differentUserId = UUID.randomUUID().toString();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(mockGroup));

        assertThatThrownBy(() -> groupService.updateGroup(groupId.toString(), request, differentUserId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("You don't have permission to modify this group");
    }

    @Test
    void getGroupById_WhenExists_ShouldReturnResponse() {
        GroupResponse expectedResponse = GroupResponse.builder().name("TOEIC Group").build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(mockGroup));
        when(groupMapper.toResponse(mockGroup)).thenReturn(expectedResponse);

        GroupResponse response = groupService.getGroupById(groupId.toString(), userId.toString());

        assertThat(response.getName()).isEqualTo("TOEIC Group");
        assertThat(response.getCreatedByName()).isEqualTo("Group Creator");
    }

    @Test
    void getGroupById_WhenNotExists_ShouldThrowException() {
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.getGroupById(groupId.toString(), userId.toString()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Group not found");
    }

    @Test
    void deleteGroup_WhenCreator_ShouldDelete() {
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(mockGroup));

        groupService.deleteGroup(groupId.toString(), userId.toString());

        verify(groupRepository).delete(mockGroup);
    }
}
