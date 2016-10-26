package com.psv.spark;


import com.psv.entities.Article;
import com.psv.entities.ArticleWord;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ArticleAnalyzer implements Serializable {

    private final SparkSession spark;

    private final Pattern ignore_symbols = Pattern.compile("[\\.,-:;]");
    private final int MIN_LENGTH_OF_WORD = 2;
    private final Pattern SPACE = Pattern.compile(" ");

    public ArticleAnalyzer() {
        spark = SparkSession
                .builder()
                .appName("JavaWordCount")
                .getOrCreate();
    }

    public void endAnalysis() {
        spark.stop();
    }

    public List<ArticleWord> analyze(final String text, final Article article) {
        JavaSparkContext javaSparkContext = new JavaSparkContext(spark.sparkContext());

        return javaSparkContext.parallelize(Collections.singletonList(text))
                .flatMap((FlatMapFunction<String, String>) s -> Arrays.asList(SPACE.split(s)).iterator())
                .map(i -> i.toLowerCase().replaceAll(ignore_symbols.pattern(), ""))
                .filter(f -> f.length() > MIN_LENGTH_OF_WORD)
                .mapToPair((PairFunction<String, String, Integer>) s -> new Tuple2<>(s, 1))
                .reduceByKey((Function2<Integer, Integer, Integer>) (i1, i2) -> i1 + i2)
                .mapToPair((PairFunction<Tuple2<String, Integer>, Integer, String>) e -> new Tuple2<>(e._2(), e._1()))
                .sortByKey(false)
                .mapToPair((PairFunction<Tuple2<Integer, String>, String, Integer>) e -> new Tuple2<>(e._2(), e._1()))
                .filter(f -> f._2() >= 5)
                .map(m -> new ArticleWord(article, m._1(), m._2()))
                .collect();
    }
}
