package com.toiec.demo.controller;

import com.toiec.demo.annotation.RateLimit;
import com.toiec.demo.dtos.request.CreateGroupRequest;
import com.toiec.demo.dtos.request.UpdateGroupRequest;
import com.toiec.demo.dtos.response.ApiResponse;
import com.toiec.demo.dtos.response.GroupResponse;
import com.toiec.demo.security.CurrentUser;
import com.toiec.demo.security.UserPrincipal;
import com.toiec.demo.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    @RateLimit(name = "createGroup", capacity = 10, refillTokens = 5, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@Valid @RequestBody CreateGroupRequest request,
                                                                  @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(groupService.createGroup(request, currentUser.getId())));
    }

    @PutMapping("/{groupId}")
    @RateLimit(name = "updateGroup", capacity = 10, refillTokens = 5, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(@PathVariable String groupId,
                                                                  @Valid @RequestBody UpdateGroupRequest request,
                                                                  @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(groupService.updateGroup(groupId, request, currentUser.getId())));
    }

    @DeleteMapping("/{groupId}")
    @RateLimit(name = "deleteGroup", capacity = 5, refillTokens = 2, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable String groupId,
                                                         @CurrentUser UserPrincipal currentUser) {
        groupService.deleteGroup(groupId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{groupId}")
    @RateLimit(name = "getGroup", capacity = 80, refillTokens = 40, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<GroupResponse>> getGroup(@PathVariable String groupId,
                                                               @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getGroupById(groupId, currentUser.getId())));
    }

    @GetMapping("/public")
    @RateLimit(name = "publicGroups", capacity = 80, refillTokens = 40, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<Page<GroupResponse>>> getPublicGroups(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getPublicGroups(pageable)));
    }

    @GetMapping("/my")
    @RateLimit(name = "myGroups", capacity = 80, refillTokens = 40, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<Page<GroupResponse>>> getMyGroups(@CurrentUser UserPrincipal currentUser,
                                                                        Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getUserGroups(currentUser.getId(), pageable)));
    }
}