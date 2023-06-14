package com.moigae.application.component.article.repository;

import com.moigae.application.component.article.domain.Article;
import com.moigae.application.component.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Article findById(int id);
}