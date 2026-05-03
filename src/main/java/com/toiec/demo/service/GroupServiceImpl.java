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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, String userId) {
        Group group = groupMapper.toEntity(request);
        group.setCreatedBy(UUID.fromString(userId));
        group = groupRepository.save(group);
        return enrichWithCreatorName(groupMapper.toResponse(group), userId);
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(String groupId, UpdateGroupRequest request, String userId) {
        Group group = findGroupByIdAndUser(groupId, userId);
        groupMapper.updateEntity(request, group);
        group = groupRepository.save(group);
        return enrichWithCreatorName(groupMapper.toResponse(group), String.valueOf(group.getCreatedBy()));
    }

    @Override
    @Transactional
    public void deleteGroup(String groupId, String userId) {
        Group group = findGroupByIdAndUser(groupId, userId);
        groupRepository.delete(group);
    }

    @Override
    public GroupResponse getGroupById(String groupId, String userId) {
        Group group = groupRepository.findById(UUID.fromString(groupId))
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        if (!group.isPublic() && !group.getCreatedBy().equals(userId)) {
            throw new BusinessRuleException("You don't have permission to view this group");
        }
        return enrichWithCreatorName(groupMapper.toResponse(group), String.valueOf(group.getCreatedBy()));
    }

    @Override
    public Page<GroupResponse> getPublicGroups(Pageable pageable) {
        return groupRepository.findByIsPublicTrue(pageable)
                .map(group -> enrichWithCreatorName(groupMapper.toResponse(group), String.valueOf(group.getCreatedBy())));
    }

    @Override
    public Page<GroupResponse> getUserGroups(String userId, Pageable pageable) {
        return groupRepository.findByCreatedBy(UUID.fromString(userId), pageable)
                .map(group -> enrichWithCreatorName(groupMapper.toResponse(group), String.valueOf(group.getCreatedBy())));
    }

    // Helper method để lấy fullName của creator và set vào response
    private GroupResponse enrichWithCreatorName(GroupResponse response, String creatorId) {
        if (creatorId == null) return response;
        User creator = userRepository.findById(UUID.fromString(creatorId)).orElse(null);
        if (creator != null) {
            response.setCreatedByName(creator.getFullName());
        }
        return response;
    }

    private Group findGroupByIdAndUser(String groupId, String userId) {
        Group group = groupRepository.findById(UUID.fromString(groupId))
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        if (!group.getCreatedBy().equals(userId)) {
            throw new BusinessRuleException("You don't have permission to modify this group");
        }
        return group;
    }
}