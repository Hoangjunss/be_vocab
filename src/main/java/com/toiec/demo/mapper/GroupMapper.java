package com.toiec.demo.mapper;

import com.toiec.demo.dtos.request.CreateGroupRequest;
import com.toiec.demo.dtos.request.UpdateGroupRequest;
import com.toiec.demo.dtos.response.GroupResponse;
import com.toiec.demo.entities.Group;
import com.toiec.demo.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    Group toEntity(CreateGroupRequest request);

    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(target = "createdByName", ignore = true)  // sẽ set thủ công trong service
    @Mapping(target = "vocabSetCount", expression = "java(group.getVocabSets() != null ? group.getVocabSets().size() : 0)")
    @Mapping(target = "vocabSets", ignore = true)
    GroupResponse toResponse(Group group);

    void updateEntity(UpdateGroupRequest request, @MappingTarget Group group);


}