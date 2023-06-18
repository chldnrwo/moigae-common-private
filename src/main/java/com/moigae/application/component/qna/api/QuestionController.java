package com.moigae.application.component.qna.api;

import com.moigae.application.component.article.api.request.ArticleForm;
import com.moigae.application.component.article.api.service.FileService;
import com.moigae.application.component.article.domain.Article;
import com.moigae.application.component.article.domain.enumeration.Category;
import com.moigae.application.component.article.repository.ArticleRepository;
import com.moigae.application.component.qna.api.service.QuestionService;
import com.moigae.application.component.qna.domain.Question;
import com.moigae.application.component.qna.dto.QuestionWithSymCountDto;
import com.moigae.application.component.qna.repository.QuestionRepository;
import com.moigae.application.component.user.domain.User;
import com.moigae.application.component.user.dto.CustomUser;
import com.moigae.application.component.user.repository.UserRepository;
import com.moigae.application.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {
    private final QuestionRepository questionRepository;
    private final FileService fileService;
    private final QuestionService questionService;
    private final UserRepository userRepository;
    @GetMapping("/createQuestion")
    public String createQuestion(Model model,
                                @AuthenticationPrincipal CustomUser customUser) {

        model.addAttribute("articleForm", new ArticleForm());
        model.addAttribute("customUser", customUser);
        return "questions/createQuestion";
    }
    @PostMapping("/createQuestion")
    public String createQuestion(
            @ModelAttribute ArticleForm articleForm,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        User user = userRepository.findByEmail(customUser.getUsername());
        Question question = new Question(user, articleForm.getArticleTitle(), articleForm.getContent(), 0);
        questionRepository.save(question);
        return "redirect:/questions/questionList";
    }
    //
    //          Question    question

    public String getQuestionList(Model model,
                                           @AuthenticationPrincipal CustomUser customUser,
                                           @PageableDefault(size = 10) Pageable pageable,
                                           String viewName) {


        //Page<QuestionWithSymCountDto> questions
        //       = questionService.getQuestionsWithSymCount(pageable);
        //Page<Question> questions = questionRepository.findAll(pageable);

        // Calculate the page group
        //int startPage = (pageable.getPageNumber() / 10) * 10;
        //int endPage = Math.min(startPage + 10, questions.getTotalPages());

        model.addAttribute("customUser", customUser);
        //model.addAttribute("questions", questions);
        //model.addAttribute("startPage", startPage);
        //model.addAttribute("endPage", endPage);

        return viewName;
    }
    @GetMapping("/questionList")
    public String articleList(Model model,
                              @AuthenticationPrincipal CustomUser customUser,
                              @PageableDefault(size = 6) Pageable pageable) {
        return getQuestionList(model, customUser, pageable, "questions/questionList");
    }

    @GetMapping("/sort")
    public ResponseEntity<Page<QuestionWithSymCountDto>> sortQuestions(@RequestParam String sort, @RequestParam(required = false) String searchTerm, Pageable pageable) {
        Page<QuestionWithSymCountDto> questions = questionService.getQuestionsWithSymCount(pageable, sort, searchTerm);

        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questionDetail/{id}")
    public String getQuestionDetail(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable("id") String questionId,
            Model model) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
        question.setViewCount(question.getViewCount()+1);
        questionRepository.save(question);
        model.addAttribute("customUser", customUser);
        model.addAttribute("question", question);

        return "questions/questionDetail";
    }

    @DeleteMapping("/delete/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable String questionId) {
        questionRepository.deleteById(questionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/update/{questionId}")
    public String updateQuestion(
            Model model,
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable String questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
        model.addAttribute("articleForm", new ArticleForm());
        model.addAttribute("customUser", customUser);
        model.addAttribute("question", question);

        return "questions/updateQuestion";
    }

    @PostMapping("/updateQuestion/{questionId}")
    public String updateQuestion(
            @PathVariable("questionId") String questionId,
            @ModelAttribute ArticleForm articleForm
    ) {
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            question.setQuestionTitle(articleForm.getArticleTitle());
            question.setQuestionContent(articleForm.getContent());

            System.out.println(question);
            questionRepository.save(question);
        }
        return "redirect:/questions/questionList";
    }
}
