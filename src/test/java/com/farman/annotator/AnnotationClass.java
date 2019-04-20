package com.farman.annotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.stagirs.lingvo.morph.model.Morph;
import com.github.stagirs.lingvo.syntax.SentenceExtractor;
import com.github.stagirs.lingvo.syntax.SentenceIterator;
import com.github.stagirs.lingvo.syntax.disambiguity.DisambiguityProcessor;
import com.github.stagirs.lingvo.syntax.model.Sentence;
import com.github.stagirs.lingvo.syntax.model.SyntaxItem;
import com.github.stagirs.lingvo.morph.MorphAnalyzer;
import com.github.stagirs.lingvo.syntax.model.items.AmbigSyntaxItem;
import com.github.stagirs.lingvo.syntax.model.items.WordSyntaxItem;

/**
 * Created by Катерина on 16.03.2019.
 */
public class AnnotationClass {

    static List<String> articlesToAnnotate = new ArrayList<>();
    static List<String> titlesForAnnotation = new ArrayList<>();
    static List<String> articlesForAnnotation = new ArrayList<>();
    static String basePath;

    public static void main(String[] args) {
        basePath = AnnotationClass.class.getClassLoader().getResource("").getPath() + "/";

//        String testText = "статистическая оценка, значения к-рой суть точки во множестве значений оцениваемой величины. Оценка статистическая оценка Пусть по реализации случайного вектора принимающего значения в выборочном пространстве надлежит оценить неизвестный параметр (или некоторую функцию). Тогда любая статистика Т n=Т п (Х), осуществляющая отображение множества в (или в множество значений функции наз. точечной оценкой параметра (оцениваемой функции Важными характеристиками Т. о. Т п являются ее математич. ожидание и дисперсионная матрица (ковариационная матрица) Вектор наз. вектором ошибок Т. о. Т п. Если - нулевой вектор при всех то говорят, что Т п является несмещенной оценкой функции или что Т п лишена систематич. ошибки, в противном случае Т. о. Т п наз. смещенной, а вектор - смещением или систематической ошибкой Т.";
////        List<String> tokenizedTestText = new ArrayList<>();
//        String tokenizedTestText = GeneralUtils.getTokenized(testText);
//        String[] toks = tokenizedTestText.split(" ");
//        List<String> testTitles = new ArrayList<>();
////        testTitles.add("Вектор");
//        testTitles.add("Статистический оценка");
//        testTitles.add("Оценка");
//        List<String> defTest = new ArrayList<>();
////        defTest.add("Вектор вектор");
//        defTest.add("Статистический оценка");
//        defTest.add("Оценка оценка");

        String russianArticlesTokenizedShortMin = basePath + "russianArticlesTokenizedShort-min.txt";
        String russianArticlesTokenizedShort = basePath + "russianArticlesTokenizedShort.txt";

        getArticlesToAnnotate(russianArticlesTokenizedShortMin);
        getTitlesForAnnotation(russianArticlesTokenizedShort);
        getArticlesForAnnotation(russianArticlesTokenizedShort);

//        List<Annotator.Data> result = Annotator.annotate(articlesToAnnotate.get(2), titlesForAnnotation, articlesForAnnotation,
//                "", new HashMap<>(), -0.1f, 1f, 10, 10, 0,
//                1f, 0.01f, 150, 0.99f);
//        List<Annotator.Data> result = Annotator.annotate(articlesToAnnotate.get(3), titlesForAnnotation, articlesForAnnotation);
//        String[] toks = articlesToAnnotate.get(3).split(" ");
//
//        System.out.println("====================RESULTS=====================");
//        for (Annotator.Data data : result) {
//            String st = "";
//            for (int i = data.min; i <= data.max; ++i) {
//                st = st + toks[i] + " ";
//            }
//            System.out.println("Title = "+data.title+" titleInd = "+data.titleInd+" min = "+data.min+
//                    " max = "+data.max+" text = "+st+" score: "+data.score);
//        }
        SentenceExtractor sentenceExtractor = new SentenceExtractor();
        String text = "йод - 1) Д. многочлена f(x) йод \\u003da йод 0 йод x йод n йод +a йод 1 йод йод х йод n-1 йод +...+ а йод n йод , с йод корни к-рого равны a йод 1 йод , a йод 2 йод йод , йод ... , a йод йод п йод , йод - произведение йод йод йод йод йод Д. равен нулю тогда и только тогда, когда йод многочлен йод имеет кратные корни. Д. симметричен относительно корней многочлена и поэтому может быть выражен через его коэффициенты. йод йод Д. квадратного трехчлена йод ax йод 2 йод +bx+c йод равен b йод 2 йод - йод 4ас йод ;Д. многочлена йод x йод 3 йод +px+q йод (корни к-рого вычисляются по йод Кардано формуле йод )равен -27q йод 2 йод -4р йод 3 йод . Если f(х) - многочлен над полем характеристики 0, то йод йод йод йод йод где R( йод f, йод f\\u0027) - йод результант йод многочлена f(x)и его производной йод f\\u0027 йод (x) йод . йод Производной многочлена f(x) йод \\u003d a йод 0 йод x йод n йод +a йод 1 йод x йод n-1 йод +...+a йод n йод йод с коэффициентами из любого поля наз. многочлен йод па йод 0 йод х йод п-1 йод + йод (п-1) йод а йод 1 йод х йод n-2 йод +... йод + a йод n-1 йод . йод йод йод ";
        text = text.replaceAll("йод[ ]*", "");
        System.out.println(text);
        List<Sentence> list = SentenceExtractor.extract(text);
//        List<Sentence> list = SentenceExtractor.extract("1) Д. многочлена f(x) йод \\u003da йод 0 йод x йод n йод +a йод 1 йод йод х йод n-1 йод +...+ а йод n йод , с йод корни к-рого равны a йод 1 йод , a йод 2 йод йод , йод ... , a йод йод п йод , йод - произведение йод йод йод йод йод Д. равен нулю тогда и только тогда, когда йод многочлен йод имеет кратные корни. Д. симметричен относительно корней многочлена и поэтому может быть выражен через его коэффициенты. йод йод Д. квадратного трехчлена йод ax йод 2 йод +bx+c йод равен b йод 2 йод - йод 4ас йод ;Д. многочлена йод x йод 3 йод +px+q йод (корни к-рого вычисляются по йод Кардано формуле йод )равен -27q йод 2 йод -4р йод 3 йод . Если f(х) - многочлен над полем характеристики 0, то йод йод йод йод йод где R( йод f, йод f\\u0027) - йод результант йод многочлена f(x)и его производной йод f\\u0027 йод (x) йод . йод Производной многочлена f(x) йод \\u003d a йод 0 йод x йод n йод +a йод 1 йод x йод n-1 йод +...+a йод n йод йод с коэффициентами из любого поля наз. многочлен йод па йод 0 йод х йод п-1 йод + йод (п-1) йод а йод 1 йод х йод n-2 йод +... йод + a йод n-1 йод . йод йод йод ");
        for (Sentence s:list) {
//            System.out.println(s.toString());
            List<SyntaxItem>items = s.getSyntaxItem();
//            List<WordSyntaxItem>items = s.getSyntaxItem();
//            DisambiguityProcessor.process(s);
            for (SyntaxItem item:items) {
//                System.out.println(item.getName());//
//                AmbigSyntaxItem aitem = (AmbigSyntaxItem)item;
//                List<WordSyntaxItem> witems = aitem.getSyntaxItems();
//                for (WordSyntaxItem witem:witems) {
//                    System.out.println(witem.getNormTerm());
//                }
                if (item instanceof WordSyntaxItem) {
                    WordSyntaxItem witem = (WordSyntaxItem)item;
                    System.out.println(witem.getNormTerm());
                }
                else {
                    System.out.println(item.getName()+" jjj");
                }
            }
        }
//        String text = "йод - 1) Д. многочлена f(x) йод \\u003da йод 0 йод x йод n йод +a йод 1 йод йод х йод n-1 йод +...+ а йод n йод , с йод корни к-рого равны a йод 1 йод , a йод 2 йод йод , йод ... , a йод йод п йод , йод - произведение йод йод йод йод йод Д. равен нулю тогда и только тогда, когда йод многочлен йод имеет кратные корни. Д. симметричен относительно корней многочлена и поэтому может быть выражен через его коэффициенты. йод йод Д. квадратного трехчлена йод ax йод 2 йод +bx+c йод равен b йод 2 йод - йод 4ас йод ;Д. многочлена йод x йод 3 йод +px+q йод (корни к-рого вычисляются по йод Кардано формуле йод )равен -27q йод 2 йод -4р йод 3 йод . Если f(х) - многочлен над полем характеристики 0, то йод йод йод йод йод где R( йод f, йод f\\u0027) - йод результант йод многочлена f(x)и его производной йод f\\u0027 йод (x) йод . йод Производной многочлена f(x) йод \\u003d a йод 0 йод x йод n йод +a йод 1 йод x йод n-1 йод +...+a йод n йод йод с коэффициентами из любого поля наз. многочлен йод па йод 0 йод х йод п-1 йод + йод (п-1) йод а йод 1 йод х йод n-2 йод +... йод + a йод n-1 йод . йод йод йод ";
//        String[] tokens = text.split(" ");
//        String toAnnotate = "";
//        String stt = "";
//        for (String token:tokens) {
//            Morph morph = MorphAnalyzer.get(token);
//            if (morph != null) {
////                System.out.println(morph.getNorm());
////                toAnnotate += morph.getNorm() + " ";
//                toAnnotate += morph.getCorrectNorm() + " ";
//                stt += morph.getCorrectNorm() + " ";
//            }
//            else {
////                System.out.println(token);
//                toAnnotate += token + " ";
//            }
//        }

//        System.out.println(toAnnotate);
//        System.out.println(stt);
//
//        result = Annotator.annotate(toAnnotate, titlesForAnnotation, articlesForAnnotation);
//        toks = toAnnotate.split(" ");
//
//        System.out.println("====================RESULTS=====================");
//        for (Annotator.Data data : result) {
//            String st = "";
//            for (int i = data.min; i <= data.max; ++i) {
//                st = st + toks[i] + " ";
//            }
//            System.out.println("Title = "+data.title+" titleInd = "+data.titleInd+" min = "+data.min+
//                    " max = "+data.max+" text = "+st+" score: "+data.score);
//        }

    }

    private static void getArticlesToAnnotate(String filePath) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String line = "";
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("[ ]+--:--[ ]+");
                articlesToAnnotate.add(parts[1]);
//                titlesForAnnotation.add(parts[0]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getTitlesForAnnotation(String filePath) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String line = "";
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("[ ]+--:--[ ]+");
//                articlesToAnnotate.add(parts[1]);
                titlesForAnnotation.add(parts[0]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getArticlesForAnnotation(String filePath) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String line = "";
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("[ ]+--:--[ ]+");
//                articlesToAnnotate.add(parts[1]);
//                titlesForAnnotation.add(parts[0]);
                articlesForAnnotation.add(parts[1]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
