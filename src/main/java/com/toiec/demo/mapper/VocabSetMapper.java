package com.toiec.demo.mapper;

import com.toiec.demo.dtos.request.CreateVocabSetRequest;
import com.toiec.demo.dtos.request.UpdateVocabSetRequest;
import com.toiec.demo.dtos.response.VocabSetResponse;
import com.toiec.demo.entities.User;
import com.toiec.demo.entities.VocabSet;
import com.toiec.demo.helper.MappingHelper;
import org.mapstruct.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {MappingHelper.class})
public interface VocabSetMapper {

    // Chỉ map groupId, createdBy sẽ được set trong service (không từ request)

    @Mapping(target = "createdBy", ignore = true)   // bỏ qua
    VocabSet toEntity(CreateVocabSetRequest request);

    // Map group.id -> groupId (UUID -> String)
    @Mapping(source = "group.id", target = "groupId", qualifiedByName = "uuidToString")
    @Mapping(source = "group.name", target = "groupName")
    @Mapping(source = "createdBy.id", target = "createdBy", qualifiedByName = "uuidToString")
    VocabSetResponse toResponse(VocabSet set);

    void updateEntity(UpdateVocabSetRequest request, @MappingTarget VocabSet set);


}