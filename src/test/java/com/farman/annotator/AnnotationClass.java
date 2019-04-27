package com.farman.annotator;

import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import com.github.stagirs.lingvo.morph.model.Morph;
import com.github.stagirs.lingvo.syntax.SentenceExtractor;
import com.github.stagirs.lingvo.syntax.SentenceIterator;
import com.github.stagirs.lingvo.syntax.disambiguity.DisambiguityProcessor;
import com.github.stagirs.lingvo.syntax.model.Sentence;
import com.github.stagirs.lingvo.syntax.model.SyntaxItem;
import com.github.stagirs.lingvo.morph.MorphAnalyzer;
import com.github.stagirs.lingvo.syntax.model.items.AmbigSyntaxItem;
import com.github.stagirs.lingvo.syntax.model.items.WordSyntaxItem;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.apache.jena.query.*;

/**
 * Created by Катерина on 16.03.2019.
 */
public class AnnotationClass {

    static List<String> articlesToAnnotate = new ArrayList<>();
    static List<String> titlesForAnnotation = new ArrayList<>();
    static List<String> articlesForAnnotation = new ArrayList<>();
    static List<String> originalTitles = new ArrayList<>();
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
        String russianArticlesTokenizedShort = basePath + "russianArticlesTokenizedShort-2.txt";
        String russianArticlesParsed = basePath + "russianArticles.json";
        String russianArticlesContext = basePath + "russianArticlesTokenizedShort.txt";

        getArticlesToAnnotate(russianArticlesTokenizedShort);
        getTitlesForAnnotation(russianArticlesTokenizedShort);
        getArticlesForAnnotation(russianArticlesContext);
        getOriginalTitles(russianArticlesParsed);

        String resultPath = basePath + "annotator1.json";
        String expertPath = basePath + "expert.json";
        String texterraPath = basePath + "texterra.json";

//        findAnnotations(resultPath);
//        checkAnnotator(expertPath, resultPath);
        texterra(texterraPath);
    }

    private static void findAnnotations(String filePath) {
        JsonObject resultObject = new JsonObject();
//        JsonObject counter = new JsonObject();

        for (int articleIdx = 0; articleIdx < articlesToAnnotate.size(); articleIdx+=128) {
//        for (int articleIdx = 0; articleIdx < articlesToAnnotate.size(); articleIdx++) {
            JsonObject articleObject = new JsonObject();
            String rawArticle = articlesToAnnotate.get(articleIdx);
            String[] articleWords = rawArticle.split(" ");
            String numberedArticle = "";
            for (int wordIdx = 0; wordIdx < articleWords.length; ++wordIdx) {
//                articleWords[wordIdx] += ("№"+wordIdx);
                numberedArticle += (articleWords[wordIdx] + "(№" +wordIdx + ") ");
            }
            String articleTitle = originalTitles.get(articleIdx);
            List<Annotator.Data> result = Annotator.annotate(rawArticle, titlesForAnnotation, originalTitles, articlesForAnnotation,
                    originalTitles.get(articleIdx), new HashMap<>(), -0.1f, 1f, 20, 20, 0,
                    1f, 0.01f, 150, 0.99f, articlesForAnnotation.get(articleIdx));
            JsonArray annsArray = new JsonArray();
            for (Annotator.Data data: result) {
                JsonObject annObject = new JsonObject();
                boolean isWiki = false;
                String titleUpperCase = data.title.substring(0, 1).toUpperCase()+data.title.substring(1).toLowerCase();

                ParameterizedSparqlString qs = new ParameterizedSparqlString("" +
                        "select ?resource ?type where {\n" +
//                    " ?resource rdf:type dbo:Person.\n " +
//                    " ?resource rdfs:label ?label.\n" +
                        "{?resource ?type \"" + titleUpperCase + "\"@ru}\n" +
                        "union\n" +
                        "{?resource ?type \"" + data.title.toLowerCase() + "\"@ru}\n" +
//                    " <http://dbpedia.org/resource/" + name + "> dbo:wikiPageRedirects ?resource.\n" +
//                    " ?resource rdfs:label ?ru\n" +
                        "}");

//                System.out.println(qs);

                QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qs.asQuery());
                ResultSet results = ResultSetFactory.copyResults(exec.execSelect());

                if (results.hasNext()) {
                    isWiki = true;
                }

//                while (results.hasNext()) {
//                    String resource = results.next().get("resource").toString();
//                    System.out.println(resource);
//                }

                annObject.addProperty("title", data.title);
                annObject.addProperty("index", data.titleInd);
                annObject.addProperty("original", originalTitles.get(data.titleInd));
                annObject.addProperty("start", data.min);
                annObject.addProperty("end", data.max);
                annObject.addProperty("score", data.score);
                annObject.addProperty("isWiki", isWiki);
//                annObject.addProperty("text", data.text);
                annsArray.add(annObject);
            }
            articleObject.addProperty("text", numberedArticle);
            articleObject.add("annotations", annsArray);
            resultObject.add(articleTitle, articleObject);
            System.out.println(articleIdx);
        }

//        Set<String> keys = counter.keySet();
//        ArrayList<String> list = new ArrayList<>(keys);
//        list.sort((o1, o2) -> {
//            if (counter.get(o1).getAsInt() < counter.get(o2).getAsInt()) {
//                return 1;
//            }
//            if (counter.get(o1).getAsInt() > counter.get(o2).getAsInt()) {
//                return -1;
//            }
//            return 0;
//        });
//
//        JsonObject newCounter = new JsonObject();
//        for (String key: list) {
//            newCounter.addProperty(key, counter.get(key).getAsInt());
//        }
//
//        try {
//            FileWriter pageRank = new FileWriter(basePath +
//                    "pageRank.json");
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            String resultString = gson.toJson(newCounter);
////            System.out.println(resultString);
//            pageRank.write(resultString);
//            pageRank.flush();
//            pageRank.close();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            FileWriter file = new FileWriter(filePath, false);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String resultString = gson.toJson(resultObject);
//            System.out.println(resultString);
            file.write(resultString);
            file.flush();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkAnnotator(String expertPath, String annotatorPath) {
        JsonObject expert = new JsonObject();
        JsonObject annotator = new JsonObject();

        try {
            JsonReader reader = new JsonReader(new FileReader(expertPath));
            expert = new Gson().fromJson(reader, JsonObject.class);
            reader = new JsonReader(new FileReader(annotatorPath));
            annotator = new Gson().fromJson(reader, JsonObject.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        int expertAmountTotal = 0;
        int annotatorAmountTotal = 0;
        int correctTotal = 0, correctLocal = 0, expertOnly = 0, annotatorOnly = 0;
        for (String key: expert.keySet()) {
            correctLocal = 0;
//            System.out.println(key);
            JsonArray expertAnnotations = expert.get(key).getAsJsonObject().get("annotations").getAsJsonArray();
            JsonArray annotatorAnnotations = annotator.get(key).getAsJsonObject().get("annotations").getAsJsonArray();
            int expertAmount = expertAnnotations.size();
//            System.out.println("expertAmount " + expertAmount);
            int annotatorAmount = annotatorAnnotations.size();
//            System.out.println("annotatorAmount " + annotatorAmount);
            expertAmountTotal += expertAmount;
            annotatorAmountTotal += annotatorAmount;
            for (int i = 0; i < expertAmount; ++i) {
                JsonObject eAnnotation = expertAnnotations.get(i).getAsJsonObject();
//                if (eAnnotation.get("isWiki").getAsBoolean())
                for (int j = 0; j < annotatorAmount; ++j) {
                    JsonObject aAnnotation = annotatorAnnotations.get(j).getAsJsonObject();
                    if (eAnnotation.get("index").getAsInt() == aAnnotation.get("index").getAsInt() &&
                        eAnnotation.get("start").getAsInt() == aAnnotation.get("start").getAsInt() &&
                        eAnnotation.get("end").getAsInt() == aAnnotation.get("end").getAsInt()
//                        aAnnotation.get("isWiki").getAsBoolean()
                    ) {
                        ++correctLocal;
//                        System.out.println("index " + eAnnotation.get("index").getAsInt());
                    }
                }
            }
            if (expertAmount > correctLocal) {
                expertOnly += expertAmount - correctLocal;
            }
            if (annotatorAmount > correctLocal) {
                annotatorOnly += annotatorAmount - correctLocal;
            }
            correctTotal += correctLocal;
//            System.out.println("CorrectLocal: "+correctLocal);
//            System.out.println("CorrectTotal: "+correctTotal);
//            System.out.println("ExpertOnly: "+expertOnly);
//            System.out.println("AnnotatorOnly: "+annotatorOnly);
//            System.out.println("ExpertTotal: "+expertAmountTotal);
//            System.out.println("AnnotatorTotal: "+annotatorAmountTotal);
        }
        System.out.println("=====RESULT=====");
        System.out.println("CorrectTotal: "+correctTotal);
        System.out.println("ExpertOnly: "+expertOnly);
        System.out.println("AnnotatorOnly: "+annotatorOnly);
        System.out.println("ExpertTotal: "+expertAmountTotal);
        System.out.println("AnnotatorTotal: "+annotatorAmountTotal);
    }

    private static void texterra(String filePath) {
        JsonArray params = new JsonArray();
//        for (int i = 0; i < articlesToAnnotate.size(); i += 128) {
        for (int i = 0; i < 129; i += 128) {
            JsonObject text = new JsonObject();
            text.addProperty("text", articlesToAnnotate.get(i));
            params.add(text);
        }
        String stringParams = new Gson().toJson(params);
        String apikey = "460d320b763b5f90243a08d9a800949e933483a0";
        JsonObject resultObject = new JsonObject();

        try {
            String requestUrl = "http://api.ispras.ru/texterra/v1/nlp?targetType=frame&apikey="+ apikey;

            URL url = new URL(requestUrl);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.setRequestProperty("Accept", "application/json");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setRequestMethod("POST");

            OutputStream os = httpConnection.getOutputStream();
            os.write(stringParams.getBytes("UTF-8"));
            os.close();

            InputStream in = new BufferedInputStream(httpConnection.getInputStream());
            String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
            JsonArray responce = new Gson().fromJson(result, JsonArray.class);
            in.close();
            httpConnection.disconnect();

            for (int i = 0; i < responce.size(); ++i) {
                JsonObject annotations = responce.get(i).getAsJsonObject().get("annotations").getAsJsonObject();
                String text = responce.get(i).getAsJsonObject().get("text").getAsString();
                JsonArray resultAnnotations = new JsonArray();
                JsonObject resultArticle = new JsonObject();
                JsonArray frame = annotations.get("frame").getAsJsonArray();
//                String annotationsString = new Gson().toJson(annotations);
//                System.out.println(annotationsString);
                for (int j = 0; j < frame.size(); ++j) {
                    JsonObject annotation = frame.get(j).getAsJsonObject();
                    int start = annotation.get("start").getAsInt();
                    int end = annotation.get("end").getAsInt();
                    JsonArray values = annotation.get("value").getAsJsonArray();
                    float maxCommonness = 0.0f;
                    int bestIndex = 0;
                    for (int k = 0; k < values.size(); ++k) {
                        float commonness = values.get(k).getAsJsonObject().get("commonness").getAsFloat();
                        if (commonness > maxCommonness) {
                            bestIndex = k;
                            maxCommonness = commonness;
                        }
                    }
                    String[] textTokens = text.split(" ");
                    int startWord = 0, charSum = 0;
                    while (startWord < textTokens.length && charSum < start) {
                        charSum += textTokens[startWord].length() + 1;
                        startWord++;
                    }
                    int endWord = startWord;
                    charSum += textTokens[startWord].length();
                    while (endWord < textTokens.length && charSum < end) {
                        ++endWord;
                        charSum += textTokens[endWord].length() + 1;
                    }
                    System.out.println(startWord+"  "+endWord);
                    String valuesString = new Gson().toJson(values);
                    System.out.println(valuesString);
                    Thread.sleep(1000);
                    String title = texterraWiki(values.get(bestIndex).getAsJsonObject().get("meaning").getAsJsonObject()
                            .get("id").getAsInt());
                    System.out.println("title: "+title);
                    if (title.length() > 0) {

                        JsonObject resultAnnotation = new JsonObject();
                        resultAnnotation.addProperty("title", title);
                        resultAnnotation.addProperty("start", startWord);
                        resultAnnotation.addProperty("end", endWord);
                        resultAnnotations.add(resultAnnotation);
                    }
                }
                resultArticle.addProperty("text", text);
                resultArticle.add("annotations", resultAnnotations);
                resultObject.add(originalTitles.get(i*128), resultArticle);
            }
            FileWriter file = new FileWriter(filePath, false);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String resultString = gson.toJson(resultObject);
            file.write(resultString);
            file.flush();
            file.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static String texterraWiki(int index) {
        String apikey = "460d320b763b5f90243a08d9a800949e933483a0";
        String param = index + ":ruwiki";
        String title = "";
        try {
            String requestUrl = "http://api.ispras.ru/texterra/v1/walker/id="+param+"?attribute=title&apikey=" + apikey;

            URL url = new URL(requestUrl);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("Accept", "application/json");
            httpConnection.setDoInput(true);
            httpConnection.setRequestMethod("GET");

            InputStream in = new BufferedInputStream(httpConnection.getInputStream());
            String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
            JsonObject responce = new Gson().fromJson(result, JsonObject.class);
            in.close();
            httpConnection.disconnect();

            String responceString = new Gson().toJson(responce);
            System.out.println(responceString);
            JsonObject titleObject = responce.get(param).getAsJsonObject();
            if (!titleObject.isJsonNull() && titleObject.keySet().contains("title")) {
                title = titleObject.get("title").getAsString();
            }
            return title;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return title;
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

    private static void getOriginalTitles(String filePath) {
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);
            for (int i = 0; i < array.size(); ++i) {
                originalTitles.add(array.get(i).getAsJsonObject().get("title").getAsString());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
