package com.example.vocabularyapp.repository;

import com.example.vocabularyapp.model.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {
    List<LearningProgress> findByUserId(Long userId);
    Optional<LearningProgress> findByUserIdAndWordId(Long userId, Long wordId);
}