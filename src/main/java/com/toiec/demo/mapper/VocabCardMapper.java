package com.toiec.demo.mapper;

import com.toiec.demo.dtos.request.CreateVocabCardRequest;
import com.toiec.demo.dtos.request.UpdateVocabCardRequest;
import com.toiec.demo.dtos.response.VocabCardResponse;
import com.toiec.demo.entities.VocabCard;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VocabCardMapper {
    VocabCard toEntity(CreateVocabCardRequest request);
    VocabCardResponse toResponse(VocabCard card);
    void updateEntity(UpdateVocabCardRequest request, @MappingTarget VocabCard card);
}