package com.toiec.demo.repository;

import com.toiec.demo.entities.VocabCard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VocabCardRepository extends JpaRepository<VocabCard, UUID> {
    List<VocabCard> findByVocabSetId(UUID setId);
    Optional<VocabCard> findByIdAndVocabSetId(UUID id, UUID setId);
    void deleteByIdAndVocabSetId(UUID id, UUID setId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VocabCard c WHERE c.vocabSet.id = :setId")
    void deleteAllByVocabSetId(UUID setId);
}