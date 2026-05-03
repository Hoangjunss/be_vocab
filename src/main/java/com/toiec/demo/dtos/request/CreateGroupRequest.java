package com.toiec.demo.dtos.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGroupRequest {
    @NotBlank
    private String name;
    private String description;
    private Boolean isPublic = false;
}