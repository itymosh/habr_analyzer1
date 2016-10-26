package com.psv.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class ArticleWord implements Serializable {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne
    private Article article;
    private String word;
    private int count;

    public ArticleWord(Article article, String word, int count) {
        this.article = article;
        this.word = word;
        this.count = count;
    }

    public long getId() {
        return id;
    }

    public Article getArticle() {
        return article;
    }

    public String getWord() {
        return word;
    }

    public int getCount() {
        return count;
    }
}
