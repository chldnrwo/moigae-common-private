package com.moigae.application.component.qna.api.service;


import com.moigae.application.component.qna.dto.QuestionWithSymCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class QuestionService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Object[]> getQuestionsWithSymCount() {
        String jpql = "SELECT q, COUNT(a.sym) FROM Question q " +
                "LEFT JOIN Answer a ON a.question.id = q.id AND a.sym = true " +
                "GROUP BY q";

        return entityManager.createQuery(jpql).getResultList();
    }
    public Page<QuestionWithSymCountDto> getQuestionsWithSymCount(Pageable pageable) {
        String jpql = "SELECT new com.moigae.application.component.qna.dto.QuestionWithSymCountDto(q, COUNT(a.sym)) " +
                "FROM com.moigae.application.component.qna.domain.Question q " +
                "LEFT JOIN com.moigae.application.component.qna.domain.Answer a ON a.question.id = q.id AND a.sym = true " +
                "GROUP BY q";

        List<QuestionWithSymCountDto> results = entityManager.createQuery(jpql, QuestionWithSymCountDto.class)
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(results, pageable, results.size());
    }
}
