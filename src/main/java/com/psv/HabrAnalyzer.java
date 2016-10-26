package com.psv;

import com.psv.entities.Article;
import com.psv.entities.ArticleWord;
import com.psv.repositories.ArticleRepository;
import com.psv.repositories.ArticleWordRepository;
import com.psv.spark.ArticleAnalyzer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class HabrAnalyzer implements Serializable {

    private final long URL = 256081;
    private final String DEFAULT_HOST = "https://habrahabr.ru";
    private final Pattern article_pattern = Pattern.compile("^(|" + DEFAULT_HOST + "/(.*))/(blog|post)/(\\d+)/$");
    private final Pattern internal_link_pattern = Pattern.compile("(#.+$)");
    private final Set<Long> articles = new HashSet<>();

    private final ArticleAnalyzer articleAnalyzer;

    private ArticleRepository articleRepository;
    private final ArticleWordRepository articleWordRepository;

    @Autowired
    public HabrAnalyzer(ArticleRepository articleRepository,
                        ArticleWordRepository articleWordRepository) throws IOException {
        this.articleRepository = articleRepository;
        this.articleWordRepository = articleWordRepository;

        articleRepository.findAll().forEach(a -> articles.add(a.getId()));

        articleAnalyzer = new ArticleAnalyzer();

        parsePage(URL, null);

        articleAnalyzer.endAnalysis();
    }

    private void parsePage(long id, Article parent) {

        Document doc = null;
        try {
            doc = Jsoup.connect(DEFAULT_HOST + "/post/" + id + "/").get();
        } catch (IOException e) {
            return;
        }

        String title = doc.select(".post__title")
                .select("span")
                .not(".post__title-arrow")
                .not(".flag")
                .text();

        System.out.println("Analyze " + id);
        Article article = new Article(id, title, parent);
        articleRepository.save(article);
        List<ArticleWord> words = articleAnalyzer.analyze(doc.select(".post__body_full").text(), article);
        articleWordRepository.save(words);

        doc.select("a")
                .stream()
                .map(m -> m.attr("href").replaceAll(internal_link_pattern.pattern(), ""))
                .filter(f -> {
                    Matcher matcher = article_pattern.matcher(f);
                    return matcher.find() && !articles.contains(Long.parseLong(matcher.group(4)));
                })
                .map(m -> {
                    Matcher matcher = article_pattern.matcher(m);
                    return matcher.find() ? matcher.group(4) : null;
                })
                .distinct()
                .forEach(i -> {
                    long newArticleId = Long.parseLong(i);
                    articles.add(newArticleId);
                    this.parsePage(newArticleId, article);
                });
    }

    public static void main(String[] args) throws IOException {
        SpringApplication.run(HabrAnalyzer.class);
    }
}
