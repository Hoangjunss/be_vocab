package com.toiec.demo.mapper;

import com.toiec.demo.dtos.request.CreateVocabSetRequest;
import com.toiec.demo.dtos.request.UpdateVocabSetRequest;
import com.toiec.demo.dtos.response.VocabSetResponse;
import com.toiec.demo.entities.VocabSet;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VocabSetMapper {
    VocabSet toEntity(CreateVocabSetRequest request);

    @Mapping(source = "createdBy.id", target = "createdBy")
    @Mapping(source = "createdBy.fullName", target = "createdByName")
    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "group.name", target = "groupName")
    VocabSetResponse toResponse(VocabSet set);
    

    void updateEntity(UpdateVocabSetRequest request, @MappingTarget VocabSet set);
}