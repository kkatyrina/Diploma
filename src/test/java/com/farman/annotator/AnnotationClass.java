package com.farman.annotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        List<Annotator.Data> result = Annotator.annotate(articlesToAnnotate.get(3), titlesForAnnotation, articlesForAnnotation);
        String[] toks = articlesToAnnotate.get(3).split(" ");

//        List<Annotator.Data> result = Annotator.annotate(tokenizedTestText, testTitles, defTest);
//        List<Annotator.Data> result = Annotator.annotate(tokenizedTestText, testTitles, defTest, "", new HashMap<>(), -0.1f, 1f, 2,
//                2, 0, 1f, 0.01f, 150, 0.99f);
        System.out.println("====================RESULTS=====================");
        for (Annotator.Data data : result) {
            String st = "";
            for (int i = data.min; i <= data.max; ++i) {
                st = st + toks[i] + " ";
            }
            System.out.println("Title = "+data.title+" titleInd = "+data.titleInd+" min = "+data.min+
                    " max = "+data.max+" text = "+st+" score: "+data.score);
        }
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
