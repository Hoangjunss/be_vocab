package com.toiec.demo.repository;

import com.toiec.demo.entities.VocabCard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface VocabCardRepository extends JpaRepository<VocabCard, String> {
    List<VocabCard> findByVocabSetId(String setId);
    Optional<VocabCard> findByIdAndVocabSetId(String id, String setId);
    void deleteByIdAndVocabSetId(String id, String setId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VocabCard c WHERE c.vocabSet.id = :setId")
    void deleteAllByVocabSetId(String setId);
}