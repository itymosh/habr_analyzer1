package com.psv.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
public class Article implements Serializable {
    @Id
    private long id;
    private String title;
    @ManyToOne
    private Article parent;

    public Article(Long id, String title, Article parent) {
        this.id = id;
        this.title = title;
        this.parent = parent;

    }

    public Article() {}

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Article getParent() {
        return parent;
    }
}
