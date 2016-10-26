package com.psv.repositories;

import com.psv.entities.ArticleWord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleWordRepository extends CrudRepository<ArticleWord, Long> {
}
