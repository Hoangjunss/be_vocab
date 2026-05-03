package com.toiec.demo.entities;


import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vocab_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabSet extends BaseEntity {
    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "is_public")
    private boolean isPublic = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;   // null for system sets

    private String topic;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel = 1;

    @Column(name = "word_count")
    private Integer wordCount = 0;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "vocabSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VocabCard> cards = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;
}