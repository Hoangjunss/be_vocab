package com.toiec.demo.mapper;

import com.toiec.demo.dtos.request.CreateGroupRequest;
import com.toiec.demo.dtos.request.UpdateGroupRequest;
import com.toiec.demo.dtos.response.GroupResponse;
import com.toiec.demo.entities.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    Group toEntity(CreateGroupRequest request);

    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "createdBy.fullName", target = "createdByName")
    GroupResponse toResponse(Group group);

    void updateEntity(UpdateGroupRequest request, @MappingTarget Group group);
}