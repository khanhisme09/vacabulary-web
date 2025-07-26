package com.example.vocabularyapp.controller;

import com.example.vocabularyapp.model.LearningProgress;
import com.example.vocabularyapp.model.User;
import com.example.vocabularyapp.model.Word;
import com.example.vocabularyapp.repository.LearningProgressRepository;
import com.example.vocabularyapp.repository.UserRepository;
import com.example.vocabularyapp.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class WordController {

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LearningProgressRepository progressRepository;

    @Autowired
    private RestTemplate restTemplate;

    // API: Lấy tất cả từ vựng
    @GetMapping("/api/words")
    @ResponseBody
    public List<Word> getAllWords() {
        return wordRepository.findAll();
    }

    // API: Tìm kiếm từ vựng theo từ khóa
    @GetMapping("/api/words/search")
    @ResponseBody
    public List<Word> searchWords(@RequestParam String keyword) {
        return wordRepository.findByWordContainingIgnoreCase(keyword);
    }

    // API: Thêm từ vựng mới
    @PostMapping("/api/words")
    @ResponseBody
    public Word addWord(@RequestBody Word word) {
        return wordRepository.save(word);
    }

    // API: Tra cứu từ điển
    @GetMapping("/api/dictionary")
    @ResponseBody
    public ResponseEntity<?> getDictionary(@RequestParam String word) {
        String apiUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;
        try {
            ResponseEntity<Object[]> response = restTemplate.getForEntity(apiUrl, Object[].class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new String[]{"Error fetching dictionary data"});
        }
    }

    // Giao diện: Hiển thị danh sách từ vựng
    @GetMapping("/words")
    public String showWords(Model model) {
        model.addAttribute("words", wordRepository.findAll());
        return "words";
    }

    // Giao diện: Tìm kiếm từ vựng
    @GetMapping("/words/search")
    public String searchWords(@RequestParam String keyword, Model model) {
        model.addAttribute("words", wordRepository.findByWordContainingIgnoreCase(keyword));
        return "words";
    }

    // Giao diện: Trang học từ vựng (trắc nghiệm)
    @GetMapping("/learn")
    public String showLearnPage(Model model, Principal principal) {
        User user = principal != null ? userRepository.findByUsername(principal.getName()) : null;
        List<LearningProgress> progress = user != null ? progressRepository.findByUserId(user.getId()) : new ArrayList<>();
        List<Word> words = wordRepository.findAll();

        if (!words.isEmpty()) {
            double progressPercentage = ((double) progress.size() / words.size()) * 100;
            model.addAttribute("progressPercentage", Math.round(progressPercentage));
            Word nextWord = selectNextWord(words, progress);
            model.addAttribute("word", nextWord);
            model.addAttribute("options", generateOptions(nextWord, words));
        }
        return "learn";
    }

    // Xử lý câu trả lời trắc nghiệm
    @PostMapping("/learn")
    public String submitAnswer(@RequestParam String answer, @RequestParam Long wordId, Principal principal, RedirectAttributes redirectAttributes) {
        User user = principal != null ? userRepository.findByUsername(principal.getName()) : null;
        Word word = wordRepository.findById(wordId).orElse(null);
        if (user != null && word != null) {
            boolean isCorrect = answer.equals(word.getDefinition());
            redirectAttributes.addFlashAttribute("isCorrect", isCorrect);
            redirectAttributes.addFlashAttribute("correctDefinition", word.getDefinition());

            LearningProgress progress = progressRepository.findByUserIdAndWordId(user.getId(), wordId)
                    .orElse(new LearningProgress());
            progress.setUser(user);
            progress.setWord(word);
            progress.setStatus(isCorrect ? "learned" : "reviewing");
            progress.setLastReviewed(LocalDateTime.now());
            progressRepository.save(progress);
        }
        return "redirect:/learn";
    }

    // Giao diện: Hiển thị tiến độ học
    @GetMapping("/progress")
    public String showProgress(Model model, Principal principal) {
        User user = principal != null ? userRepository.findByUsername(principal.getName()) : null;
        List<LearningProgress> progress = user != null ? progressRepository.findByUserId(user.getId()) : new ArrayList<>();
        List<Word> words = wordRepository.findAll();
        double progressPercentage = words.isEmpty() ? 0 : ((double) progress.size() / words.size()) * 100;
        model.addAttribute("progressPercentage", Math.round(progressPercentage));
        model.addAttribute("progress", progress);
        return "progress";
    }

    // Giao diện: Trang từ điển
    @GetMapping("/dictionary")
    public String showDictionaryPage(Model model) {
        return "dictionary";
    }

    // Chọn từ tiếp theo dựa trên Spaced Repetition
    private Word selectNextWord(List<Word> words, List<LearningProgress> progress) {
        Collections.shuffle(words);
        for (Word word : words) {
            LearningProgress p = progress.stream()
                    .filter(pr -> pr.getWord().getId().equals(word.getId()))
                    .findFirst()
                    .orElse(null);
            if (p == null || (p.getStatus().equals("reviewing") && p.getLastReviewed().isBefore(LocalDateTime.now().minusDays(1)))) {
                return word;
            }
        }
        return words.get(0);
    }

    // Tạo danh sách lựa chọn trắc nghiệm
    private List<String> generateOptions(Word correctWord, List<Word> allWords) {
        List<String> options = new ArrayList<>();
        options.add(correctWord.getDefinition());
        Collections.shuffle(allWords);
        for (Word word : allWords) {
            if (!word.equals(correctWord) && options.size() < 4) {
                options.add(word.getDefinition());
            }
        }
        Collections.shuffle(options);
        return options;
    }
}