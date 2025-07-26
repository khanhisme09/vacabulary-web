package com.example.vocabularyapp.controller;

import com.example.vocabularyapp.model.User;
import com.example.vocabularyapp.model.Word;
import com.example.vocabularyapp.model.WordList;
import com.example.vocabularyapp.repository.UserRepository;
import com.example.vocabularyapp.repository.WordListRepository;
import com.example.vocabularyapp.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WordListController {

    @Autowired
    private WordListRepository wordListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WordRepository wordRepository;

    // Giao diện: Hiển thị danh sách từ vựng của người dùng
    @GetMapping("/wordlists")
    public String showWordLists(Model model, Principal principal) {
        User user = principal != null ? userRepository.findByUsername(principal.getName()) : null;
        List<WordList> wordLists = user != null ? wordListRepository.findByUserId(user.getId()) : new ArrayList<>();
        model.addAttribute("wordLists", wordLists);
        return "wordlists";
    }

    // Giao diện: Form tạo danh sách từ mới
    @GetMapping("/wordlists/new")
    public String showNewWordListForm(Model model) {
        model.addAttribute("wordList", new WordList());
        return "wordlist-form";
    }

    // Xử lý: Tạo danh sách từ mới
    @PostMapping("/wordlists")
    public String createWordList(@ModelAttribute WordList wordList, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(principal.getName());
        wordList.setUser(user);
        wordListRepository.save(wordList);
        redirectAttributes.addFlashAttribute("message", "Word list created successfully!");
        return "redirect:/wordlists";
    }

    // Giao diện: Xem chi tiết danh sách từ
    @GetMapping("/wordlists/{id}")
    public String showWordListDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        WordList wordList = wordListRepository.findById(id).orElse(null);
        if (wordList == null) {
            redirectAttributes.addFlashAttribute("error", "Word list not found!");
            return "redirect:/wordlists";
        }
        model.addAttribute("wordList", wordList);
        model.addAttribute("allWords", wordRepository.findAll());
        return "wordlist-details";
    }

    // Xử lý: Thêm từ vào danh sách
    @PostMapping("/wordlists/{id}/add-word")
    public String addWordToList(@PathVariable Long id, @RequestParam Long wordId, RedirectAttributes redirectAttributes) {
        WordList wordList = wordListRepository.findById(id).orElse(null);
        Word word = wordRepository.findById(wordId).orElse(null);
        if (wordList == null || word == null) {
            redirectAttributes.addFlashAttribute("error", "Word or list not found!");
            return "redirect:/wordlists/" + id;
        }
        if (!wordList.getWords().contains(word)) {
            wordList.getWords().add(word);
            wordListRepository.save(wordList);
            redirectAttributes.addFlashAttribute("message", "Word added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Word already in list!");
        }
        return "redirect:/wordlists/" + id;
    }

    // Xử lý: Xóa từ khỏi danh sách
    @PostMapping("/wordlists/{id}/remove-word")
    public String removeWordFromList(@PathVariable Long id, @RequestParam Long wordId, RedirectAttributes redirectAttributes) {
        WordList wordList = wordListRepository.findById(id).orElse(null);
        Word word = wordRepository.findById(wordId).orElse(null);
        if (wordList == null || word == null) {
            redirectAttributes.addFlashAttribute("error", "Word or list not found!");
            return "redirect:/wordlists/" + id;
        }
        wordList.getWords().remove(word);
        wordListRepository.save(wordList);
        redirectAttributes.addFlashAttribute("message", "Word removed successfully!");
        return "redirect:/wordlists/" + id;
    }

    // Xử lý: Xóa danh sách từ
    @PostMapping("/wordlists/{id}/delete")
    public String deleteWordList(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        WordList wordList = wordListRepository.findById(id).orElse(null);
        if (wordList == null) {
            redirectAttributes.addFlashAttribute("error", "Word list not found!");
            return "redirect:/wordlists";
        }
        wordListRepository.delete(wordList);
        redirectAttributes.addFlashAttribute("message", "Word list deleted successfully!");
        return "redirect:/wordlists";
    }
}