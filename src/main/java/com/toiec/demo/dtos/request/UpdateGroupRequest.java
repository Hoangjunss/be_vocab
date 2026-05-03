package com.toiec.demo.dtos.request;


import lombok.Data;

@Data
public class UpdateGroupRequest {
    private String name;
    private String description;
    private Boolean isPublic;
}