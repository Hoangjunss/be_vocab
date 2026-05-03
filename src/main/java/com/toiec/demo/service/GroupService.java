package com.toiec.demo.service;

import com.toiec.demo.dtos.request.CreateGroupRequest;
import com.toiec.demo.dtos.request.UpdateGroupRequest;
import com.toiec.demo.dtos.response.GroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {
    GroupResponse createGroup(CreateGroupRequest request, String userId);
    GroupResponse updateGroup(String groupId, UpdateGroupRequest request, String userId);
    void deleteGroup(String groupId, String userId);
    GroupResponse getGroupById(String groupId, String userId);
    Page<GroupResponse> getPublicGroups(Pageable pageable);
    Page<GroupResponse> getUserGroups(String userId, Pageable pageable);
}