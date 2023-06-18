package com.moigae.application.component.qna.repository;

import com.moigae.application.component.qna.domain.Answer;
import com.moigae.application.component.qna.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, String> {
    Answer findByUserIdAndQuestionId(String userId, String questionId);
}
