package com.example.vocabularyapp.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String word;
    private String definition;
    private String example;
    private String pronunciation;
    private String partOfSpeech;
}