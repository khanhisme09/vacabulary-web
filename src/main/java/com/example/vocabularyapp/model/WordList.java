package com.example.vocabularyapp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class WordList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "word_list_items",
            joinColumns = @JoinColumn(name = "word_list_id"),
            inverseJoinColumns = @JoinColumn(name = "word_id")
    )
    private List<Word> words;
}