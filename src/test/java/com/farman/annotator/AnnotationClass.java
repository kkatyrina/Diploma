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
import org.apache.jena.base.Sys;
import org.apache.jena.query.*;

/**
 * Created by Катерина on 16.03.2019.
 */
public class AnnotationClass {

    static List<String> articlesToAnnotate = new ArrayList<>();
    static List<String> titlesForAnnotation = new ArrayList<>();
    static List<String> articlesForAnnotation = new ArrayList<>();
    static List<String> originalTitles = new ArrayList<>();
    static List<String> originalArticles = new ArrayList<>();
    static String basePath;
    static float minScore = 0.12f;

    public static void main(String[] args) {
        basePath = AnnotationClass.class.getClassLoader().getResource("").getPath() + "/";

        String russianArticlesTokenizedShort = basePath + "russianArticlesTokenizedShort-2.txt";
        String russianArticlesTokenizedFull = basePath + "russianArticlesTokenizedFull-2.json";
        String russianArticlesParsed = basePath + "newRussianArticles.json";
        String russianArticlesContext = basePath + "russianArticlesTokenizedShort.txt";

        getArticlesToAnnotate(russianArticlesTokenizedFull);
        getTitlesForAnnotation(russianArticlesTokenizedShort);
        getArticlesForAnnotation(russianArticlesContext);
        getOriginal(russianArticlesParsed);

        String resultPath = basePath + "annotator11.json";
        String expertPath = basePath + "expert.json";
        String texterraPath = basePath + "texterra.json";

        findAnnotations(resultPath);
        checkAnnotator(expertPath, resultPath);
//        texterra(texterraPath);
//        checkTexterra(expertPath, texterraPath);
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
//                String cleanWord = articleWords[wordIdx].replaceAll("\\$", "");
//                if (cleanWord.length() > 0) {
//                    articleWords[wordIdx] = cleanWord;
//                }
//                articleWords[wordIdx] += ("№"+wordIdx);
                numberedArticle += (articleWords[wordIdx] + "(№" +wordIdx + ") ");
            }
            String articleTitle = originalTitles.get(articleIdx);
            List<Annotator.Data> result = Annotator.annotate(rawArticle, titlesForAnnotation, originalTitles, articlesForAnnotation,
                    originalTitles.get(articleIdx), new HashMap<>(), minScore, 1f, 20, 20, 0,
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
        int differentPlace = 0;
        for (String key: expert.keySet()) {
            correctLocal = 0;
            System.out.println("key = "+key);
            JsonArray expertAnnotations = expert.get(key).getAsJsonObject().get("annotations").getAsJsonArray();
            JsonArray annotatorAnnotations = annotator.get(key).getAsJsonObject().get("annotations").getAsJsonArray();
            int expertAmount = expertAnnotations.size();
//            System.out.println("expertAmount " + expertAmount);
            int annotatorAmount = annotatorAnnotations.size();
//            System.out.println("annotatorAmount " + annotatorAmount);
            expertAmountTotal += expertAmount;
            annotatorAmountTotal += annotatorAmount;

            for (int i = 0; i < expertAmount; ++i) {
                boolean flag = false;
                JsonObject eAnnotation = expertAnnotations.get(i).getAsJsonObject();
//                if (eAnnotation.get("isWiki").getAsBoolean())
                for (int j = 0; j < annotatorAmount; ++j) {
                    JsonObject aAnnotation = annotatorAnnotations.get(j).getAsJsonObject();
                    if (eAnnotation.get("index").getAsInt() == aAnnotation.get("index").getAsInt()) {
                        if (eAnnotation.get("start").getAsInt() == aAnnotation.get("start").getAsInt() &&
                                eAnnotation.get("end").getAsInt() == aAnnotation.get("end").getAsInt()) {
                            ++correctLocal;
                            flag = true;
//                        System.out.println("index " + eAnnotation.get("index").getAsInt());
                        }
                        else {
                            ++differentPlace;
                            ++correctLocal;
                            flag = true;
                        }
                    }
                }
//                if (!flag) {
//                    System.out.println("not found: "+eAnnotation.get("title").getAsString()+ " " +
//                            eAnnotation.get("start").getAsInt());
//                }
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
//        correctTotal += differentPlace;
        System.out.println("=====RESULT=====");
        System.out.println("CorrectTotal: "+correctTotal);
        System.out.println("DifferentPlace: "+differentPlace);
        System.out.println("ExpertOnly: "+expertOnly);
        System.out.println("AnnotatorOnly: "+annotatorOnly);
        System.out.println("ExpertTotal: "+expertAmountTotal);
        System.out.println("AnnotatorTotal: "+annotatorAmountTotal);
        float precision = correctTotal / ((correctTotal+annotatorOnly)*1f);
        float recall = correctTotal / ((correctTotal+expertOnly)*1f);
        System.out.println("Precision: "+precision);
        System.out.println("Recall: "+recall);
        System.out.println("F: "+2f*(precision*recall/(precision+recall)));
    }

    private static void texterra(String filePath) {
        JsonArray params = new JsonArray();
        for (int i = 0; i < articlesToAnnotate.size(); i += 128) {
//        for (int i = 0; i < 129; i += 128) {
            JsonObject text = new JsonObject();
            text.addProperty("text", originalArticles.get(i));
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
                String respText = responce.get(i).getAsJsonObject().get("text").getAsString();
                String text = originalArticles.get(i*128);
                if (!respText.equalsIgnoreCase(text)) {
                    System.out.println("FFUUUUUUUUUUUCKK");
                    System.out.println(text);
                    System.out.println(respText);
                    continue;
                }
                String[] articleWords = text.split(" ");
                String numberedArticle = "";
                for (int wordIdx = 0; wordIdx < articleWords.length; ++wordIdx) {
//                articleWords[wordIdx] += ("№"+wordIdx);
                    numberedArticle += (articleWords[wordIdx] + "(№" +wordIdx + ") ");
                }
                JsonArray resultAnnotations = new JsonArray();
                JsonObject resultArticle = new JsonObject();
                JsonArray frame = annotations.get("frame").getAsJsonArray();
//                String annotationsString = new Gson().toJson(annotations);
//                System.out.println(annotationsString);
                for (int j = 0; j < frame.size(); ++j) {
                    JsonObject annotation = frame.get(j).getAsJsonObject();
                    int start = annotation.get("start").getAsInt();
                    int end = annotation.get("end").getAsInt();
                    System.out.println(start+" txtr "+end);
                    System.out.println(respText.substring(start, end));
                    JsonArray values = annotation.get("value").getAsJsonArray();
                    float maxCommonness = 0.0f;
                    int bestIndex = 0;
                    for (int k = 0; k < values.size(); ++k) {
                        float commonness = values.get(k).getAsJsonObject().get("commonness").getAsFloat();
//                        System.out.println(k+") "+commonness);
                        if (commonness > maxCommonness) {
                            bestIndex = k;
                            maxCommonness = commonness;
                        }
                    }
//                    System.out.println("bestIdx: "+bestIndex+" maxCom: "+maxCommonness);
                    String[] textTokens = text.split(" ");
                    int startWord = 0, charSum = 0;
                    while (startWord < textTokens.length && charSum < start) {
                        charSum += textTokens[startWord].length() + 1;
                        startWord++;
                    }
                    System.out.println("charSum1: "+charSum);
                    int endWord = startWord;
                    charSum += textTokens[startWord].length();
                    while (endWord < textTokens.length && charSum < end) {
                        ++endWord;
                        charSum += textTokens[endWord].length() + 1;
                    }
                    System.out.println("charSum2: "+charSum);
                    System.out.println(startWord+"  "+endWord);
//                    String valuesString = new Gson().toJson(values);
//                    System.out.println(valuesString);
                    Thread.sleep(2000);
                    String title = texterraWiki(values.get(bestIndex).getAsJsonObject().get("meaning").getAsJsonObject()
                            .get("id").getAsInt());
                    System.out.println("title: "+title);
                    if (title.length() > 0) {
                        JsonObject resultAnnotation = new JsonObject();
                        resultAnnotation.addProperty("title", title);
                        resultAnnotation.addProperty("start", startWord);
                        resultAnnotation.addProperty("end", endWord);

                        String title1 = title.replaceAll("\\([^)]+\\)", "");
                        if (title1.endsWith(" ")) {
                            title1 = title1.substring(0, title1.length()-1);
                        }
//                        System.out.println("title1: "+title1);
                        String title2 = title.replaceAll("ё", "е");
                        boolean isMath = false;
                        if (originalTitles.contains(title) || originalTitles.contains(title1)
                                || originalTitles.contains(title2)) {
                            isMath = true;
                        }
                        resultAnnotation.addProperty("isMath", isMath);
                        resultAnnotations.add(resultAnnotation);
                    }
                }
                resultArticle.addProperty("text", numberedArticle);
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
//            System.out.println(responceString);
            JsonObject titleObject = responce.get(param).getAsJsonObject();
            if (!titleObject.get("title").toString().equals("null")) {
                title = titleObject.get("title").getAsString();
            }
            return title;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return title;
    }

    private static void checkTexterra(String expertPath, String texterraPath) {
        JsonObject expert = new JsonObject();
        JsonObject texterra = new JsonObject();

        try {
            JsonReader reader = new JsonReader(new FileReader(expertPath));
            expert = new Gson().fromJson(reader, JsonObject.class);
            reader = new JsonReader(new FileReader(texterraPath));
            texterra = new Gson().fromJson(reader, JsonObject.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        int expertAmountTotal = 0;
        int texterraTotal = 0;
        int correctTotal = 0, correctLocal = 0, expertOnly = 0, texterraOnly = 0;
        int notMath = 0, repeated = 0;
        int foundDistinct = 0;
        int differentPlace = 0;
        boolean askExpert = true;
        for (String key: expert.keySet()) {
            String s = new Gson().toJson(texterra);
//            System.out.println(s);
            correctLocal = 0;
            List<String> texterraFound = new ArrayList<>();
//            System.out.println("key = "+key);
            JsonArray expertAnnotations = expert.get(key).getAsJsonObject().get("annotations").getAsJsonArray();
            JsonArray texterraAnnotations = texterra.get(key).getAsJsonObject().get("annotations").getAsJsonArray();

            int expertAmount = expertAnnotations.size();

//            System.out.println("expertAmount " + expertAmount);
            int texterraAmount = texterraAnnotations.size();
//            System.out.println("annotatorAmount " + annotatorAmount);
            expertAmountTotal += expertAmount;
            texterraTotal += texterraAmount;

            for (int i = 0; i < expertAmount; ++i) {
                JsonObject eAnnotation = expertAnnotations.get(i).getAsJsonObject();
//                System.out.println("i = "+i);
                boolean flag = false;
                int j = 0;
                while (j < texterraAmount && !flag) {
//                    System.out.println(j);
                    JsonObject tAnnotation = texterraAnnotations.get(j).getAsJsonObject();
                    String title = tAnnotation.get("title").getAsString().replaceAll("ё", "е");
                    String title1 = title.replaceAll("\\([^)]+\\)", "");
                    if (title1.endsWith(" ")) {
                        title1 = title1.substring(0, title1.length() - 1);
                    }
                    if (!texterraFound.contains(title) &&
                            tAnnotation.get("isMath").getAsBoolean()) {
                        texterraFound.add(title);
//                        System.out.println(title);
                    }
                    if (eAnnotation.get("title").getAsString().equalsIgnoreCase(title) ||
                            eAnnotation.get("title").getAsString().equalsIgnoreCase(title1)) {
                        if (eAnnotation.get("start").getAsInt() == tAnnotation.get("start").getAsInt() &&
                                eAnnotation.get("end").getAsInt() == tAnnotation.get("end").getAsInt()) {
                            ++correctLocal;
                            flag = true;
//                            System.out.println("accepted: "+title);

                        }
                        else {
                            ++differentPlace;
                            ++correctLocal;
                            flag = true;
//                            System.out.println("accepted: "+title);
                        }
                    }
                    else {
                        if (askExpert) {
                            try {
                                System.out.println(eAnnotation.get("title").getAsString() + " --- " + title);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                                String sscore = reader.readLine();
                                int score = Integer.parseInt(sscore);
                                if (score == 1) {
                                    ++correctTotal;
                                    flag = true;
                                }
                                if (score == -1) {
                                    askExpert = false;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
//                    else {
//                        if (i == 0 && !tAnnotation.get("isMath").getAsBoolean()) {
//                            notMath++;
//                        }
//                        else {
//                            if (i == 0) {
//                                texterraOnly++;
//                                System.out.println(tAnnotation.get("title").getAsString()+" "+
//                                        tAnnotation.get("start").getAsInt()+" "+
//                                        tAnnotation.get("end").getAsInt());
////                            texterraFound.add(tAnnotation.get("title").getAsString());
//                            }
//                        }
////                        if ()
//                    }
                    ++j;
                }
                if (!flag && eAnnotation.get("isWiki").getAsBoolean()) {
//                    System.out.println(eAnnotation.get("title").getAsString()+" "+
//                                        eAnnotation.get("start").getAsInt()+" "+
//                                        eAnnotation.get("end").getAsInt());
                    expertOnly++;
//                    System.out.println("not found: "+eAnnotation.get("title").getAsString());
                }
            }



//            for (int j = 0; j < texterraAmount; ++j) {
//                JsonObject tAnnotation = texterraAnnotations.get(j).getAsJsonObject();
//
//            }

//            if (expertAmount > correctLocal) {
//                expertOnly += expertAmount - correctLocal;
//            }
//            if (annotatorAmount > correctLocal) {
//                annotatorOnly += annotatorAmount - correctLocal;
//            }
            correctTotal += correctLocal;
            foundDistinct += texterraFound.size();
//            System.out.println("CorrectLocal: "+correctLocal);
//            System.out.println("CorrectTotal: "+correctTotal);
//            System.out.println("ExpertOnly: "+expertOnly);
//            System.out.println("AnnotatorOnly: "+annotatorOnly);
//            System.out.println("ExpertTotal: "+expertAmountTotal);
//            System.out.println("AnnotatorTotal: "+annotatorAmountTotal);
        }


        texterraOnly = foundDistinct - correctTotal;
        System.out.println("=====RESULT=====");
        System.out.println("CorrectTotal: "+correctTotal);
        System.out.println("DifferentPlace: "+differentPlace);
        System.out.println("ExpertOnly: "+expertOnly);
        System.out.println("TexterraOnly: "+ texterraOnly);
        System.out.println("ExpertTotal: "+expertAmountTotal);
        System.out.println("TexterraDistinct: " + foundDistinct);
//        System.out.println("Not math: "+notMath);
        System.out.println("TexterraTotal: "+texterraTotal);
        float precision = correctTotal / (1f * (correctTotal+texterraOnly));
        float recall = correctTotal / (1f * (correctTotal+expertOnly));
        float f = 2f * ((precision * recall) / (precision + recall));
        System.out.println("Precision: "+precision);
        System.out.println("Recall: "+recall);
        System.out.println("F: "+f);
    }

    private static void getArticlesToAnnotate(String filePath) {
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);
            for (int i = 0; i < array.size(); ++i) {
                String rawText = array.get(i).getAsJsonObject().get("text").getAsString();
                rawText = rawText.replaceAll("\\$", "");
                rawText = rawText.replaceAll("newline", "");
                rawText = rawText.replaceAll("return", "");
                articlesToAnnotate.add(rawText);
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

    private static void getOriginal(String filePath) {
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);
            for (int i = 0; i < array.size(); ++i) {
                originalTitles.add(array.get(i).getAsJsonObject().get("title").getAsString());
                String rawText = array.get(i).getAsJsonObject().get("text").getAsString();
                rawText = rawText.replaceAll("\\$", "");
                rawText = rawText.replaceAll("newline", "");
                rawText = rawText.replaceAll("return", "");
                originalArticles.add(rawText);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
