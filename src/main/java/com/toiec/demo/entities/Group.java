package com.toiec.demo.entities;


import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group extends BaseEntity {
    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "is_public")
    private boolean isPublic = false;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VocabSet> vocabSets = new ArrayList<>();
}