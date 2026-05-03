package com.toiec.demo.entities;


import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

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
    private String createdBy;

    @Column(name = "is_public")
    private boolean isPublic = false;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VocabSet> vocabSets = new ArrayList<>();
}