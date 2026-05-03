package com.toiec.demo.entities;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserVocabProgressId implements Serializable {
    private UUID userId;
    private UUID cardId;
}