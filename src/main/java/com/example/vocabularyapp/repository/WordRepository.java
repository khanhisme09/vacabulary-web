package com.example.vocabularyapp.repository;

import com.example.vocabularyapp.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByWordContainingIgnoreCase(String keyword);
}