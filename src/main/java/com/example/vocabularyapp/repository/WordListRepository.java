package com.example.vocabularyapp.repository;

import com.example.vocabularyapp.model.WordList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordListRepository extends JpaRepository<WordList, Long> {
    List<WordList> findByUserId(Long userId);
}