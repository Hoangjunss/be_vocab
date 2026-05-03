package com.toiec.demo.service;

import com.toiec.demo.dtos.request.CreateGroupRequest;
import com.toiec.demo.dtos.request.UpdateGroupRequest;
import com.toiec.demo.dtos.response.GroupResponse;
import com.toiec.demo.entities.Group;
import com.toiec.demo.exception.BusinessRuleException;
import com.toiec.demo.exception.ResourceNotFoundException;
import com.toiec.demo.mapper.GroupMapper;
import com.toiec.demo.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    @Override
    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, String userId) {
        Group group = groupMapper.toEntity(request);
        group.setCreatedBy(userId);
        group = groupRepository.save(group);
        return groupMapper.toResponse(group);
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(String groupId, UpdateGroupRequest request, String userId) {
        Group group = findGroupByIdAndUser(groupId, userId);
        groupMapper.updateEntity(request, group);
        group = groupRepository.save(group);
        return groupMapper.toResponse(group);
    }

    @Override
    @Transactional
    public void deleteGroup(String groupId, String userId) {
        Group group = findGroupByIdAndUser(groupId, userId);
        groupRepository.delete(group);
    }

    @Override
    public GroupResponse getGroupById(String groupId, String userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        if (!group.isPublic() && !group.getCreatedBy().equals(userId)) {
            throw new BusinessRuleException("You don't have permission to view this group");
        }
        return groupMapper.toResponse(group);
    }

    @Override
    public Page<GroupResponse> getPublicGroups(Pageable pageable) {
        return groupRepository.findByIsPublicTrue(pageable).map(groupMapper::toResponse);
    }

    @Override
    public Page<GroupResponse> getUserGroups(String userId, Pageable pageable) {
        return groupRepository.findByCreatedBy(userId, pageable).map(groupMapper::toResponse);
    }

    private Group findGroupByIdAndUser(String groupId, String userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        if (!group.getCreatedBy().equals(userId)) {
            throw new BusinessRuleException("You don't have permission to modify this group");
        }
        return group;
    }
}