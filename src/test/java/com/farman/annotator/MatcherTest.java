package com.farman.annotator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Катерина on 28.04.2019.
 */

public class MatcherTest {

    static List<String> russian = new ArrayList<>();
    static List<String> english = new ArrayList<>();
    static List<String> ruTitles = new ArrayList<>();
    static List<String> enRuTitles = new ArrayList<>();
    static List<String> enTitles = new ArrayList<>();
    static List<String> ruOriginal = new ArrayList<>();
    static List<Integer> matchTitlesIndexes = new ArrayList<>();
    static String basePath;
//    static JsonObject titleMatch = new JsonObject();

    public static void main(String[] args) {
        basePath = MatcherTest.class.getClassLoader().getResource("").getPath() + "/";

        String russianPath = basePath + "russianArticlesTokenizedShort-2.txt";
        String ruOriginalPath = basePath + "newRussianArticles.json";
        String englishPath = basePath + "englishArticlesTokenized.txt";
        String enOriginalPath = basePath + "englishArticles.txt";

        String titleExpert = basePath + "titleExpert.json";
        String expertPath = basePath + "matchExpert.json";
        String articlePath = basePath + "testMatch2.json";

        getRussianTokenized(russianPath);
        getRussianOriginal(ruOriginalPath);
        getEnglishTokenized(englishPath);
        getEnglishOriginal(enOriginalPath);
//        matchTitles(titleExpert);
        matchArticles(articlePath);
        matchTest(expertPath, articlePath);
        ArticleClass.PlayMusic(basePath+"main_theme_cover_by_zack_kim.mid");
    }

    private static void matchArticles(String filePath) {
        int foundArticle = 0;
        JsonObject match = new JsonObject();
//        String line = "";
//        List<String> russianToMatch = new ArrayList<>();
//        List<Integer> articleIndexes = new ArrayList<>();
//        int step = matchTitlesIndexes.size() / 100;
//        for (int i = 0; i < matchTitlesIndexes.size(); i++) {
//            String article = russian.get(matchTitlesIndexes.get(i));
//            String title = ruOriginal.get(matchTitlesIndexes.get(i));
////            System.out.println("title: "+title);
////            int bound = article.length() > 100? 100 : article.length();
////            System.out.println("    article: "+article.substring(0, bound));
//            russianToMatch.add(article);
//            articleIndexes.add(matchTitlesIndexes.get(i));
//        }

//        GeneralUtils.docs = russianToMatch.size();
        GeneralUtils.docs = russian.size();
//            System.out.println(titlesRu);
        float [] parts = new float[]{/*0.05f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f,*/ 0.8f/*, 0.9f, 0.99f*/};
//        List<Integer> res = Mapper.map(russianToMatch, english, 0.8f);
        List<Integer> res = Mapper.map(russian, english, 1f);
//            System.out.println(res);
//        for (int i = 0; i < russianToMatch.size(); ++i) {
        for (int i = 0; i < russian.size(); ++i) {
//            String article = russianToMatch.get(i);
//            int bound1 = article.length() > 100? 100 : article.length();
//            System.out.println("match this: "+article.substring(0, bound1));
//            System.out.println("result: "+res.get(i));
            String matcher = "";
            if (res.get(i) != -1) {
                foundArticle++;
                matcher = enTitles.get(res.get(i));
                match.addProperty(ruOriginal.get(i), matcher);
            }
//            match.addProperty(ruOriginal.get(articleIndexes.get(i)), matcher);
//            match.addProperty(ruOriginal.get(i), matcher);
        }

        System.out.println("found: "+ foundArticle);
        try {
            FileWriter file = new FileWriter(filePath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            file.write(gson.toJson(match));
            file.flush();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void matchTest(String expertPath, String systemPath) {
        JsonObject expert = new JsonObject();
        JsonObject system = new JsonObject();

        try {
            JsonReader reader = new JsonReader(new FileReader(expertPath));
            expert = new Gson().fromJson(reader, JsonObject.class);
            reader = new JsonReader(new FileReader(systemPath));
            system = new Gson().fromJson(reader, JsonObject.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        int correct = 0, expertOnly = 0, systemOnly = 0;
        int expertTotal = 0, systemTotal = system.keySet().size();
        for (String key:expert.keySet()) {
            String matching = expert.get(key).getAsString();
            if (matching.length() > 0) {
                expertTotal++;
                if (system.keySet().contains(key)) {
                    String answer = system.get(key).getAsString();
                    systemTotal++;
                    if (matching.equalsIgnoreCase(answer)) {
                        ++correct;
                    }
                    else {
                        expertOnly++;
                    }
                }
                else {
                    expertOnly++;
                }
            }
            else {
                if (system.keySet().contains(key)) {
//                    systemTotal++;
                    systemOnly++;
//                    System.out.println(key);
                }
            }
        }
        System.out.println("=====RESULT=====");
        System.out.println("CorrectTotal: "+correct);
        System.out.println("ExpertOnly: "+expertOnly);
        System.out.println("SystemOnly: "+ systemOnly);
        System.out.println("ExpertTotal: "+expertTotal);
//        System.out.println("TexterraDistinct: " + foundDistinct);
//        System.out.println("Not math: "+notMath);
        System.out.println("SystemTotal: "+systemTotal);
        float precision = correct / (1f * (correct+systemOnly));
        float recall = correct / (1f * (correct+expertOnly));
        float f = 2f * ((precision * recall) / (precision + recall));
        System.out.println("Precision: "+precision);
        System.out.println("Recall: "+recall);
        System.out.println("F: "+f);
    }

    private static void matchTitles(String filePath) {
        JsonObject match = new JsonObject();
        int foundTitle = 0;
        for (int i = 0; i < ruTitles.size(); ++i) {
            for (int j = 0; j < enRuTitles.size(); ++j) {
                String russianTitle = ruTitles.get(i);
                russianTitle = russianTitle.replaceAll("[()0-9]+", "");
                if (russianTitle.equalsIgnoreCase(enRuTitles.get(j))) {
//                    System.out.println("i = " + i + ", j = " + j);
                    ++foundTitle;
                    if (!matchTitlesIndexes.contains(i)) {
                        matchTitlesIndexes.add(i);
                    }
                    JsonArray values = new JsonArray();
//                        englishTitle = englishTitle.replaceAll("[ ]*\\(([^)]+)\\)", "");
//                        englishTitle = englishTitle.replaceAll(" ", "_");
                    if (match.keySet().contains(ruOriginal.get(i))) {
                        values = match.get(ruOriginal.get(i)).getAsJsonArray();
                    }
                    values.add(enTitles.get(j));
                    match.add(ruOriginal.get(i), values);
//                    articlesRu.set(i, " ");
//                    articlesEnRu.set(j, " ");
                }
            }
        }
        System.out.println("Found titles: "+ foundTitle);

        try {
            FileWriter file = new FileWriter(filePath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            file.write(gson.toJson(match));
            file.flush();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getRussianTokenized(String filePath) {
        try {
            if (filePath.contains("json")) {
                JsonReader reader = new JsonReader(new FileReader(filePath));
                JsonArray array = new Gson().fromJson(reader, JsonArray.class);
                for (int i = 0; i < array.size(); ++i) {
                    String rawText = array.get(i).getAsJsonObject().get("text").getAsString();
                    rawText = rawText.replaceAll("\\$", "");
                    rawText = rawText.replaceAll("newline", "");
                    rawText = rawText.replaceAll("return", "");
                    russian.add(rawText);

                    String title = array.get(i).getAsJsonObject().get("title").getAsString();
                    ruTitles.add(title);
                }
            }
            else {
                BufferedReader in = new BufferedReader(new FileReader(filePath));
                String line;
                int id = 0;
                while ((line = in.readLine()) != null) {
                    String[] parts = line.split("[ ]+--:--[ ]+");
                    String preTerm = parts[0];
                    String text = parts[1];
                    text = text.replaceAll("[^А-Яа-я ]+", "");
                    ruTitles.add(preTerm);
                    russian.add(text);
                    ++id;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getRussianOriginal(String filePath) {
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);
            for (int i = 0; i < array.size(); ++i) {
                String title = array.get(i).getAsJsonObject().get("title").getAsString();
                int id = array.get(i).getAsJsonObject().get("id").getAsInt();
                ruOriginal.add(title+id);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getEnglishTokenized(String filePath) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String line;
            int id = 0;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("[ ]+--:--[ ]+");
                String preTerm = parts[0];
                String text = parts[1];
                enRuTitles.add(preTerm);
                english.add(text);
                ++id;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getEnglishOriginal(String filePath) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String line;
            int id = 0;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("[ ]+--:--[ ]+");
                String preTerm = parts[0];
                enTitles.add(preTerm+id);
                ++id;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
