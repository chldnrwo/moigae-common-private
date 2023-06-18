package com.moigae.application.component.qna.api.service;


import com.moigae.application.component.qna.dto.QuestionWithSymCountDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuestionService {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<QuestionWithSymCountDto> getQuestionsWithSymCount(Pageable pageable, String sort, String searchTerm) {
        String jpql = "SELECT new com.moigae.application.component.qna.dto.QuestionWithSymCountDto(q, COUNT(a.sym), COUNT(a)) " +
                "FROM com.moigae.application.component.qna.domain.Question q " +
                "LEFT JOIN com.moigae.application.component.qna.domain.Answer a ON a.question.id = q.id " +
                "WHERE q.questionTitle LIKE :searchTerm OR q.questionContent LIKE :searchTerm " +
                "GROUP BY q";

        if ("views".equals(sort)) {
            jpql += " ORDER BY q.viewCount DESC";
        } else {
            jpql += " ORDER BY q.createTime DESC";
        }

        return getQuestionWithSymCountDtos(pageable, jpql, searchTerm);
    }

    private Page<QuestionWithSymCountDto> getQuestionWithSymCountDtos(Pageable pageable, String jpql, String searchTerm) {
        String countJpql = "SELECT COUNT(q) FROM com.moigae.application.component.qna.domain.Question q WHERE q.questionTitle LIKE :searchTerm OR q.questionContent LIKE :searchTerm";

        TypedQuery<QuestionWithSymCountDto> query = entityManager.createQuery(jpql, QuestionWithSymCountDto.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        countQuery.setParameter("searchTerm", "%" + searchTerm + "%");

        // 페이지에 해당하는 데이터만 가져옴
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<QuestionWithSymCountDto> content = query.getResultList();
        Long total = countQuery.getSingleResult();

        for (QuestionWithSymCountDto qd : content) {
            Document document = Jsoup.parse(qd.getQuestionContent());
            String parseContent = document.text();
            qd.setQuestionContent(parseContent);
            qd.setRelativeTime(getRelativeTime(qd.getCreateTime()));
        }

        return new PageImpl<>(content, pageable, total);
    }

    public String getRelativeTime(LocalDateTime pastTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(pastTime, now);

        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "일 전";
        } else if (hours > 0) {
            return hours + "시간 전";
        } else if (minutes > 0) {
            return minutes + "분 전";
        } else {
            return seconds + "초 전";
        }
    }

    public QuestionWithSymCountDto getQuestionWithSymCount(String questionId) {
        String jpql = "SELECT new com.moigae.application.component.qna.dto.QuestionWithSymCountDto(q, COUNT(a.sym), COUNT(a)) " +
                "FROM com.moigae.application.component.qna.domain.Question q " +
                "LEFT JOIN com.moigae.application.component.qna.domain.Answer a ON a.question.id = q.id " +
                "WHERE q.id = :questionId " +
                "GROUP BY q";
        TypedQuery<QuestionWithSymCountDto> query = entityManager.createQuery(jpql, QuestionWithSymCountDto.class);
        query.setParameter("questionId", questionId);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            // No result found, return null or throw an exception as per your requirement
            return null;
        } catch (NonUniqueResultException e) {
            // More than one result found, throw an exception or handle as per your requirement
            throw new RuntimeException("Non unique result for questionId " + questionId);
        }
    }


}
