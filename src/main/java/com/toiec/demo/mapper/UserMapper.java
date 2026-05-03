package com.toiec.demo.mapper;


import com.toiec.demo.dtos.request.RegisterRequest;
import com.toiec.demo.dtos.response.UserProfileResponse;
import com.toiec.demo.entities.User;
import com.toiec.demo.entities.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "vocabProgresses", ignore = true)
    @Mapping(target = "ownedSets", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(source = "password", target = "passwordHash")
    @Mapping(target = "role", constant = "USER")
    User toEntity(RegisterRequest request);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.avatarUrl", target = "avatarUrl")
    UserProfileResponse toProfileResponse(UserProfile profile);
}