package com.farman.annotator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Катерина on 16.03.2019.
 */
public class AnnotationClass {

    public static void main(String[] args) {
        String testText = "статистическая оценка, значения к-рой суть точки во множестве значений оцениваемой величины. Оценка статистическая оценка Пусть по реализации случайного вектора принимающего значения в выборочном пространстве надлежит оценить неизвестный параметр (или некоторую функцию). Тогда любая статистика Т n=Т п (Х), осуществляющая отображение множества в (или в множество значений функции наз. точечной оценкой параметра (оцениваемой функции Важными характеристиками Т. о. Т п являются ее математич. ожидание и дисперсионная матрица (ковариационная матрица) Вектор наз. вектором ошибок Т. о. Т п. Если - нулевой вектор при всех то говорят, что Т п является несмещенной оценкой функции или что Т п лишена систематич. ошибки, в противном случае Т. о. Т п наз. смещенной, а вектор - смещением или систематической ошибкой Т.";
//        List<String> tokenizedTestText = new ArrayList<>();
        String tokenizedTestText = GeneralUtils.getTokenized(testText);
        String[] toks = tokenizedTestText.split(" ");
        List<String> testTitles = new ArrayList<>();
//        testTitles.add("Вектор");
        testTitles.add("Статистический оценка");
        testTitles.add("Оценка");
        List<String> defTest = new ArrayList<>();
//        defTest.add("Вектор вектор");
        defTest.add("Статистический оценка");
        defTest.add("Оценка оценка");

//        List<Annotator.Data> result = Annotator.annotate(tokenizedTestText, testTitles, defTest);
        List<Annotator.Data> result = Annotator.annotate(tokenizedTestText, testTitles, defTest, "", new HashMap<>(), -0.1f, 1f, 2,
                2, 0, 1f, 0.01f, 150, 0.99f);
        System.out.println("====================RESULTS=====================");
        for (Annotator.Data data : result) {
            String st = "";
            for (int i = data.min; i <= data.max; ++i) {
                st = st + toks[i] + " ";
            }
            System.out.println("Title = "+data.title+" titleInd = "+data.titleInd+" min = "+data.min+" max = "+data.max+" text = "+st);
        }
    }
}
