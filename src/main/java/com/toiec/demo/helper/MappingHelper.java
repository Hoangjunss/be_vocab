package com.toiec.demo.helper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
public class MappingHelper {

    @Named("stringToUuid")
    public UUID stringToUuid(String id) {
        if (id == null || id.isBlank()) return null;
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("uuidToString")
    public String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
}