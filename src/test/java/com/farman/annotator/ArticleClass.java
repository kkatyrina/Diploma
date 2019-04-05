package com.farman.annotator;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sound.sampled.*;

/**
 * Created by Катерина on 07.03.2019.
 */

public class ArticleClass {
    static final String englishTitlesPath = Constants.HOME + "englishTitles.txt";
    static final String englishTitlesRuPath = Constants.HOME + "englishTitlesRu.txt";
    static final String russianTitlesPath = Constants.HOME + "russianTitles.txt";
    static final String englishArticlesPath1 = Constants.HOME + "MathItemsEN1.xml";
    static final String englishArticlesPath2 = Constants.HOME + "MathItemsEN2.xml";
    static final String englishArticlesRuPath = Constants.HOME + "math\\" + "englishTranslationSaved2.txt";
    static final String russianArticlesPath = Constants.HOME + "math\\" + "russianText.txt";
    static final String russianArticlesXMLPath = Constants.HOME + "MathItemsRU.xml";
    static final String russianArticlesToMapPath = Constants.HOME + "russianToMap.txt";
    static final String englishRuArticlesToMapPath = Constants.HOME + "englishRuToMap.txt";
    static String basePath;

    static List<String> titlesEn = new ArrayList<>();
    static List<String> titlesRu = new ArrayList<>();
    static List<String> titlesEnRu = new ArrayList<>();
    static List<String> titlesEnRuTok = new ArrayList<>();
    static List<String> titlesRuTok = new ArrayList<>();
    static List<String> articlesEnRu = new ArrayList<>();
    static List<String> articlesRu = new ArrayList<>();
    static List<String> articlesEnRuTok = new ArrayList<>();
    static List<String> articlesRuTok = new ArrayList<>();

    private static JsonObject MSC = new JsonObject();

    private static List<String> keys = new ArrayList<>(Arrays.asList(
            "trnsl.1.1.20190313T095920Z.f86b19b4ab9d5866.a1e440162ec0d18e3a15f06ba4be9b6d541fcb71",
            "trnsl.1.1.20190321T090956Z.76d75fa3ae6b5baf.4aec2a9e6f1c3e029447d5dd519f5d2670e83ebd",
            "trnsl.1.1.20190321T140647Z.c1d8b3c10b0b3586.0dfc2fad5c98ac15b2e09a1cc44d786a9be9db32",
            "trnsl.1.1.20190321T140832Z.4487c2e5039ff531.bc714af7649913422cb9f226fd2df2be7b77246b",
            "trnsl.1.1.20190321T150929Z.926aec47f7650219.320ec5a379f48ba8be859c584fa220d460635983"
    ));
//    //kkaty.rina@yandex.ru
//    keys.add("trnsl.1.1.20190313T095920Z.f86b19b4ab9d5866.a1e440162ec0d18e3a15f06ba4be9b6d541fcb71");
//    //kkkaty.rina@yandex.ru
//    keys.add("trnsl.1.1.20190321T090956Z.76d75fa3ae6b5baf.4aec2a9e6f1c3e029447d5dd519f5d2670e83ebd");
//    //esinelnikovaa@yandex.ru
//    keys.add("trnsl.1.1.20190321T140647Z.c1d8b3c10b0b3586.0dfc2fad5c98ac15b2e09a1cc44d786a9be9db32");
//    //igor.sinelnikow@yandex.ru
//    keys.add("trnsl.1.1.20190321T140832Z.4487c2e5039ff531.bc714af7649913422cb9f226fd2df2be7b77246b");
//    //gogaigor13@yandex.ru
//    keys.add("trnsl.1.1.20190321T150929Z.926aec47f7650219.320ec5a379f48ba8be859c584fa220d460635983");

    static int foundTitle = 0;
    static int englishCount = 0;

    public static void main(String args[]) throws IOException{
        basePath = ArticleClass.class.getClassLoader().getResource("").getPath() + "/";

//        Document enArts1 = null, enArts2 = null, ruArts = null;
//        try {
//            enArts1 = Jsoup.parse(new File(englishArticlesPath1), "UTF-8");
//            enArts2 = Jsoup.parse(new File(englishArticlesPath2), "UTF-8");
////            ruArts = Jsoup.parse(new File(russianArticlesXMLPath), "UTF-8");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        getMSCfromArticles();
//        getMSCFromFile();

        //Предобработка английских статей с сохранением в файл
//        getEnglishArticles();

        //Перевод английских статей с сохранением в файл
//        translateEnglishArticles();

        //Токенизация английских статей с сохранением в файл
//        tokenizeEnglishArticles();

//        parseYandexLemmas();

        //Выделяем английские заголовки и запоминаем
//        getEnglishTitles(enArts1);
//        getEnglishTitles(enArts2);

        //Разделяем русские и переведенные статьи на заголовок и текст, запоминаем
//        getEnglishRuArticles(englishArticlesRuPath);
        getRussianArticles();

        //Токенизация заголовков
//        for (String str: titlesRu)
//            titlesRuTok.add(Mapper.getTokenized2(str));
//        for (String str: titlesEnRu)
//            titlesEnRuTok.add(Mapper.getTokenized2(str));

        String russianArticles = basePath + "russianArticles.json";
        String russianArticlesLemmas = basePath + "russianArticlesRawLemmas.txt";
        lemmatizeArticles(russianArticles, russianArticlesLemmas);

        //Сопоставление по заголовкам
//        matchTitles();

        //Токенизация статей с сохранением в отдельные файлы
//        tokenizeEnRu();
//        tokenizeRu();

//        PlayMusic(Constants.HOME.toString()+"main_theme_cover_by_zack_kim.mid");
    }

    public static void getEnglishTitles(Document xml) throws  IOException {
        FileWriter file = new FileWriter(englishTitlesPath, true);

        Elements items = xml.getElementsByTag("item");
        for (Element item : items) {
//            System.out.println(item);
            String preTerm = item.getElementsByTag("title").first().ownText();
            String term = preTerm.substring(0, 1).toUpperCase() + preTerm.substring(1).toLowerCase();
//            file.write(term+"\n");
            titlesEn.add(term);
            ++englishCount;
        }
        file.close();
    }

    public static void getRussianArticlesXML(Document xml) throws  IOException {
        FileWriter file = new FileWriter(russianTitlesPath, true);
        Elements items = xml.getElementsByTag("item");
        int count = 0;
        for (Element item : items) {
            ++count;
//            System.out.println(item);
            String preTerm = item.getElementsByTag("title").first().ownText();
            String term = preTerm.substring(0, 1).toUpperCase() + preTerm.substring(1).toLowerCase();
            Element textElement = item.getElementsByTag("text").first();
            String text = textElement.text();
            text = text.replaceAll("<!\\[CDATA\\[", "");
            text = text.replaceAll("\\]\\]>", "");
//            file.write(term + "\n");
            titlesRu.set(count, term);
            articlesRu.set(count, text);
        }
        file.close();
    }

    public static void getEnglishRuArticles(String filePath) throws IOException{
        BufferedReader in = new BufferedReader(new FileReader(filePath));
        FileWriter file = new FileWriter(englishTitlesRuPath, true);
        String line;
        int count = 0;
//            System.out.println(in);
        while ((line = in.readLine()) != null) {

            String [] parts = line.split(" --:-- ");
            String preTerm = parts[0];
            String text = parts[1];
            String term = preTerm.substring(0, 1).toUpperCase() + preTerm.substring(1).toLowerCase();
//            file.write(term+"\n");
            titlesEnRu.add(term);
            articlesEnRu.add(text);
            count++;
        }
        file.close();
    }

    public static void getRussianArticles(String filePath) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filePath));
        FileWriter file = new FileWriter(russianTitlesPath, true);
        String line;
        int count = 0;
//            System.out.println(in);
        while ((line = in.readLine()) != null) {

            String [] parts = line.split(" --:-- ");
            String preTerm = parts[0];
            String text = parts[1];
            String term = preTerm.substring(0, 1).toUpperCase() + preTerm.substring(1).toLowerCase();
//            file.write(term+"\n");
            titlesRu.add(term);
            articlesRu.add(text);
            count++;
        }
        file.close();
    }

//    public static void matchTitles() {
//        JsonObject articles = new JsonObject();
//        JsonArray array = new JsonArray();
//        String filePath = Constants.HOME + "titleMap.json";
//
//        for (int i = 0; i < titlesRuTok.size(); ++i) {
//            for (int j = 0; j < titlesEnRuTok.size(); ++j) {
//                if (titlesRuTok.get(i).equalsIgnoreCase(titlesEnRuTok.get(j))) {
//                    System.out.println("i = " + i + ", j = " + j);
//                    ++foundTitle;
//                    JsonObject pair = new JsonObject();
//                    String englishTitle = titlesEn.get(j);
//                    englishTitle = englishTitle.replaceAll("[ ]*\\(([^)]+)\\)", "");
//                    englishTitle = englishTitle.replaceAll(" ", "_");
//                    pair.put("ru", titlesRu.get(i));
//                    pair.put("en", englishTitle);
//                    array.add(pair);
//                    articlesRu.set(i, " ");
//                    articlesEnRu.set(j, " ");
//                }
//            }
//        }
//        articles.put("articles", array);
//        try {
//            FileWriter file = new FileWriter(filePath);
//            file.write(articles.toString());
//            file.flush();
//            file.close();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            FileWriter file1 = new FileWriter(russianArticlesToMapPath, true);
//            for (String article : articlesRu) {
////                    System.out.println("ru: "+article);
////                if (article.length() > 0) file1.write(article+"\n");
//                file1.write(article+"\n");
//            }
//            file1.flush();
//            file1.close();
//            FileWriter file2 = new FileWriter(englishRuArticlesToMapPath, true);
//            for (String article : articlesEnRu) {
////                    System.out.println("en: "+article);
////                if (article.length() > 0) file2.write(article+"\n");
//                file2.write(article+"\n");
//            }
//            file2.flush();
//            file2.close();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private static void getMSCfromArticles() {
//        String filePath = ArticleClass.class.getClassLoader().getResource("").getPath() + "/" + "soma.json";
//        JsonArray array = JSON.readAny(filePath).getAsArray();
//
//        for (int i = 0; i < array.size(); ++i) {
//            JsonObject object = array.get(i).getAsObject();
//            String title = object.get("name").toString();
//            if (title.charAt(0) == '"') title = title.substring(1);
//            if (title.charAt(title.length()-1) == '"') title = title.substring(0, title.length()-1);
//            title = title.replaceAll("_", " ");
//            title = title.replaceAll("\\\\u2013", "-");
//
//            String rawText = object.get("text").toString();
//            String newText = rawText.replaceAll("<[^>]*>", " ");
//            extractMSC(title, newText);
//        }
//        String result = ArticleClass.class.getClassLoader().getResource("").getPath() + "/" + "englishArticlesMSC1.json";
//        try {
//            FileWriter fileMSC = new FileWriter(result);
//            fileMSC.write(MSC.toString());
//            fileMSC.flush();
//            fileMSC.close();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private static void getMSCFromFile() {
////        System.out.println("1: " + MSC.keys().size());
//        String filePath = ArticleClass.class.getClassLoader().getResource("").getPath() +
//                "/" + "MathCategoriesEN.xml";
//        String result = ArticleClass.class.getClassLoader().getResource("").getPath() + "englishArticlesMSC1.json";
//        MSC = JSON.readAny(result).getAsObject();
////        System.out.println("2: " + MSC.keys().size());
//        Document indexesXML = null;
//
//        try {
//            indexesXML = Jsoup.parse(new File(filePath), "UTF-8");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (indexesXML == null) return;
//
//        Elements categories = indexesXML.getElementsByTag("category");
//        for (Element category: categories) {
//            Elements newIndexes = category.getElementsByTag("msc");
//
//            if (newIndexes.size() < 1) continue;
//
//            Elements articles = category.getElementsByTag("item");
//
//            for (Element article:articles) {
//                String title = article.text();
//                title = title.replaceAll("\\u2013", "-");
//                JsonArray indexes = new JsonArray();
//                if (MSC.hasKey(title)) {
//                    if (newIndexes.size() > 1) continue;
//                    indexes = MSC.get(title).getAsArray();
//                    MSC.remove(title);
//                }
//
//                for (Element indexElement : newIndexes) {
//                    String code = indexElement.ownText().replaceAll("--", "-").toUpperCase();
//                    if (code.length() == 3) code = code + "XX";
//                    if (code.length() == 4)
//                        code = code.substring(0, 2) + "-" + code.substring(2, 4);
//                    if (!indexes.contains(new JsonString(code)) && !isParent(indexes, code)) {
//                        indexes.add(code);
//                        String parent = findParent(indexes, code);
//                        if (parent.length() > 0) indexes.remove(new JsonString(parent));
//                    }
//                }
//
//                MSC.put(title, indexes);
//            }
//        }
//        result = ArticleClass.class.getClassLoader().getResource("").getPath() + "englishArticlesMSC3.json";
//        try {
//            FileWriter fileMSC = new FileWriter(result);
//            fileMSC.write(MSC.toString());
//            fileMSC.flush();
//            fileMSC.close();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public static void getEnglishArticles() {
//        String filePath = ArticleClass.class.getClassLoader().getResource("").getPath() + "/" +"soma.json";
//
//        JsonArray array = JSON.readAny(filePath).getAsArray();
//        String articles = "";
//
//        for (int i = 0; i < array.size(); ++i) {
//            JsonObject object = array.get(i).getAsObject();
//            String title = object.get("name").toString();
//            if (title.charAt(0) == '"') title = title.substring(1);
//            if (title.charAt(title.length()-1) == '"') title = title.substring(0, title.length()-1);
//            title = title.replaceAll("_", " ");
//            title = title.replaceAll("\\\\u2013", "-");
////            titlesEn.set(i, title);
////            System.out.println(title);
//
//            String rawText = object.get("text").toString();
////            System.out.println(rawText);
//            String newText = rawText.replaceAll("<[^>]*>", " ");
////            System.out.println(newText);
//
//            newText = newText.replaceAll("\\\\n", "");
//            if (newText.charAt(0) == '"') newText = newText.substring(1);
//            if (newText.charAt(newText.length()-1) == '"') newText = newText.substring(0, newText.length()-1);
//
//            newText = removeBetween("Contents", "References", newText, new ArrayList<String>(), 0, false);
//            newText = removeBetween("Contents", "Literature", newText, new ArrayList<String>(), 0, false);
////            System.out.println(newText);
//
//            int refIndex = newText.indexOf("References");
//            int citeIndex = newText.indexOf("How to Cite This Entry");
//            int comIndex = newText.indexOf("Comments");
//            int min = minIndex(refIndex, minIndex(citeIndex, comIndex));
//            if (min > -1) newText = newText.substring(0, min);
////            System.out.println(newText);
//
//            newText = removeBetween("begin{equation}", "end{equation}", newText, new ArrayList<String>(), 0, false);
////            System.out.println(newText);
//
//            newText = removeBetween("2010 Mathematics Subject Classification", "ZBL", newText, new ArrayList<String>(), 0, false);
////            System.out.println(newText);
//
////            newText = newText.replaceAll("(?s)References.+", " "); //remove everything after References
//            newText = newText.replaceAll("\\[[^\\]]+\\]", " "); //remove []
////            System.out.println(newText);
//
//            newText = newText.replaceAll("[\\$]+[^\\$]*[\\$]+", " "); //remove formulas
////            System.out.println(newText);
//            newText = newText.replaceAll("[ ]+", " "); //remove multiple spaces
////            newText = newText.replaceAll("begin\\{equation\\}[^(end)]*end\\{equation\\}", "#"); //remove formulas
//
//            newText = newText.substring(0, minIndex(newText.length(), 2000));
////            System.out.println(newText);
//
////            System.out.println(newText);
//
//            articles = articles + title + " --:--" + newText + "\n";
//            System.out.println(i);
//        }
//        try {
//            FileWriter file = new FileWriter(Constants.HOME.toString()+"englishArticles.txt", true);
////            file.write(articles);
//            file.flush();
//            file.close();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private static void getRussianArticles() {
        try {
            String filePath = ArticleClass.class.getClassLoader().getResource("").getPath() + "/" + "oldick.json";
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);
            String articles = "";
            JsonArray result = new JsonArray();



            for (int i = 4; i < 5; ++i) {
    //            JsonObject object = array.get(i).getAsObject();
                JsonObject object = array.get(i).getAsJsonObject();
                String title = object.get("name").getAsString();
                if (title.charAt(0) == '"') title = title.substring(1);
                if (title.charAt(title.length()-1) == '"') title = title.substring(0, title.length()-1);
                title = title.replaceAll("_", " ");
                title = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();
    //            titlesEn.set(i, title);
    //            System.out.println(title);

                String rawText = object.get("text").getAsString();
    //            System.out.println(rawText);

                String newText = rawText;
                List<String> cut = new ArrayList<>();
    //            newText = removeBetween("(adsbygoogle", "push({});", newText, " ");
    //            newText = removeBetween("<!-- ***", "<!-- ***", newText, " ");
                newText = removeBetween("<!-- *** native", "push({});", newText, cut, cut.size(), false);
                newText = removeBetween("<!-- *** buf", "<!-- *** text *** -->", newText, cut, cut.size(), false);
                newText = removeBetween("<", ">", newText, cut, cut.size(), true);
    //            System.out.println("cut size: " + cut.size());
    //            newText = newText.replaceAll("<[^>]*>", " ");
//                System.out.println(newText);


                newText = newText.replaceAll("\n", " newline ");
                newText = newText.replaceAll("\t", " tabulation ");
                newText = newText.replaceAll("\r", " return ");
                System.out.println(newText);

    //            newText = newText.replaceAll("\\\\r", "");
                if (newText.charAt(0) == '"') newText = newText.substring(1);

                if (newText.charAt(newText.length()-1) == '"') newText = newText.substring(0, newText.length()-1);


    //            newText = removeBetween("Contents", "References", newText, " ");
    //            newText = removeBetween("Contents", "Literature", newText, " ");
    //            System.out.println(newText);

    //            int refIndex = newText.indexOf("References");
    //            int citeIndex = newText.indexOf("How to Cite This Entry");
    //            int comIndex = newText.indexOf("Comments");
    //            int min = minIndex(refIndex, minIndex(citeIndex, comIndex));
    //            if (min > -1) newText = newText.substring(0, min);
    //            System.out.println(newText);

    //            newText = removeBetween("begin{equation}", "end{equation}", newText, " ");
    //            System.out.println(newText);



    //            System.out.println(newText);

                newText = newText.replaceAll("(?s)Лит.+", ""); //remove everything after References
                newText = newText.replaceAll("\\[[^\\]]+\\]", " "); //remove []
                newText = newText.replaceAll("[ ]+", " "); //remove multiple spaces
    //            if (newText.charAt(0) == ' ') newText = newText.substring(1);
    //            if (newText.charAt(0) == '-') newText = newText.substring(1);
                while (newText.charAt(0) == ' ' || newText.charAt(0) == '-')
                    newText = newText.substring(1);
    //            System.out.println(newText);

    //            newText = newText.replaceAll("[\\$]+[^\\$]*[\\$]+", " "); //remove formulas
    //            System.out.println(newText);

    //            newText = newText.replaceAll("begin\\{equation\\}[^(end)]*end\\{equation\\}", "#"); //remove formulas

    //            newText = newText.substring(0, minIndex(newText.length(), 2000));
    //            System.out.println(newText);

    //            System.out.println(newText);
                JsonArray jsonCut = new JsonArray();
                String cuts = "";
                for (String item: cut) {
//                    System.out.println(item);
                    jsonCut.add(new JsonPrimitive(item));
    //                cuts += item + "\n";
                }

                for (JsonElement item:jsonCut) {
//                    System.out.println(item.getAsString());
                }

                JsonObject article = new JsonObject();

                article.add("title", new JsonPrimitive(title));
                article.add("id", new JsonPrimitive(i));
                article.add("text", new JsonPrimitive(newText));
                article.add("cut", jsonCut);
                result.add(article);

                articles = articles + title + " --:-- " + newText + "\n";
                System.out.println(i);
            }

            String path = ArticleClass.class.getClassLoader().getResource("").getPath() + "/" +"russianArticles.json";
            FileWriter file = new FileWriter(path, false);
//            file.write(articles);
//            file.write(result.getAsString());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String resultString = gson.toJson(result);
//            System.out.println(resultString);
            file.write(resultString);
            file.flush();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void lemmatizeArticles(String sourcePath, String resultPath) {
//        System.out.println(sourcePath);
//        System.out.println(resultPath);
        try {
            BufferedReader in = new BufferedReader(new FileReader(sourcePath));
            Gson gson = new Gson();
            JsonArray array = gson.fromJson(in, JsonArray.class);

//            String line;
            String inputTitlePath = basePath + "title_input.txt";
            String inputTextPath = basePath + "text_input.txt";
            String outputTitlePath = basePath + "title_output.txt";
            String outputTextPath = basePath + "text_output.txt";
//            System.out.println(inputTitlePath);
//            System.out.println(inputTextPath);
//            System.out.println(outputTitlePath);
//            System.out.println(outputTextPath);

            File titleOutput = new File(outputTitlePath);//.substring(1).replaceAll("[/]+", "\\\\"));
            File textOutput = new File(outputTextPath);//.substring(1).replaceAll("[/]+", "\\\\"));
            File titleInput = new File(inputTitlePath);
            File textInput = new File(inputTextPath);

            boolean success = titleOutput.createNewFile();
//            System.out.println(success);
            success = textOutput.createNewFile();
//            System.out.println(success);

            String titleCommand = "D:/mystem -l " + //inputTitlePath + " " + outputTitlePath;
                    inputTitlePath.substring(1).replaceAll("[/]+", "/") + " " +
                    outputTitlePath.substring(1).replaceAll("[/]+", "/");
            String textCommand = "D:/mystem -l " + //inputTextPath + " " + outputTextPath;
                    inputTextPath.substring(1).replaceAll("[/]+", "/") + " " +
                    outputTextPath.substring(1).replaceAll("[/]+", "/");

//            System.out.println(titleCommand);
//            System.out.println(textCommand);

//            while ((line = in.readLine()) != null) {
            for (int i = 0; i < array.size(); ++i) {
                JsonObject object = array.get(i).getAsJsonObject();

//                System.out.println(line);
//                String[] parts = line.split(" --:-- ");
//                String title = parts[0], text = parts[1];
                String title = object.get("title").getAsString();
                String text = object.get("text").getAsString();
                System.out.println(title);
                System.out.println(text);

                FileWriter title_input = new FileWriter(inputTitlePath);
                title_input.write(title);
                title_input.close();
                Process proc1 = Runtime.getRuntime().exec(titleCommand);
                proc1.waitFor();
                proc1.destroy();
//                while (titleOutput.length() < 1) Thread.sleep(100);

                FileWriter text_input = new FileWriter(inputTextPath);
                text_input.write(text);
                text_input.close();
                Process proc2 = Runtime.getRuntime().exec(textCommand);
                proc2.waitFor();
                proc2.destroy();
//                while (textOutput.length() < 1) Thread.sleep(100);

                BufferedReader title_output = new BufferedReader(new FileReader(outputTitlePath));
//                System.out.println(titleOutput.exists());
//                System.out.println(titleOutput.length());
                String titleLemmas = title_output.readLine();
//                System.out.println(titleLemmas);

                BufferedReader text_output = new BufferedReader(new FileReader(outputTextPath));
                String textLemmas = text_output.readLine();
//                System.out.println(textLemmas);

                FileWriter global_output = new FileWriter(resultPath, true);
                String lemmas = titleLemmas + " --:-- " + textLemmas + "\n";
                global_output.write(lemmas);

                title_output.close();
                text_output.close();
                global_output.close();
            }
            success = titleInput.delete();
            success = textInput.delete();
            success = titleOutput.delete();
            success = textOutput.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

//    private static void extractMSC(String title, String text) {
//        Pattern pattern = Pattern.compile("Mathematics Subject Classification[^\\[]+\\[");
//        Matcher mathcer = pattern.matcher(text);
//
//        JsonArray indexes = new JsonArray();
//        if (MSC.hasKey(title)) {
//            indexes = MSC.get(title).getAsArray();
//            MSC.remove(title);
//        }
//
//        while (mathcer.find()) {
//            String group = mathcer.group();
//            group = group.replaceAll("\\:", "");
//            group = group.replaceAll("\\[", "");
//            group = group.replaceAll("Mathematics Subject Classification", " ");
//            group = group.replaceAll("Primary", " ");
//            group = group.replaceAll("Secondary", " ");
//            String[] parts = group.split("[ ]+");
//            for (String index: parts) {
////                System.out.println(index);
//                String[] splitted = index.split(",");
//                for (String code: splitted) {
//                    if (code.length() > 0) {
//                        code = code.replaceAll("--", "-").toUpperCase();
//                        if (code.length() == 3) code = code + "XX";
//                        if (code.length() == 4)
//                            code = code.substring(0, 2) + "-" + code.substring(2, 4);
//                        if (! indexes.contains(new JsonString(code)) && !isParent(indexes, code)) {
//                            indexes.add(code);
//                            String parent = findParent(indexes, code);
//                            if (parent.length() > 0) indexes.remove(new JsonString(parent));
//                        }
//                    }
//                }
//            }
//        }
//        if (indexes.size() > 0) MSC.put(title, indexes);
//    }

//    private static boolean isParent(JsonArray array, String newItem) {
////        System.out.println("CHECK PARENT");
//        for (JsonValue value: array) {
//            String index = value.toString().replaceAll("\"", "");
////            System.out.println("existing: "+index+" new: "+newItem);
//            if (newItem.equals(index.substring(0, 3) + "XX")) return true;
//            if (newItem.equals(index.substring(0, 2) + "-XX")) return true;
//            if (newItem.equals(index.substring(0, 2) + "-" + index.substring(3, 5))) return true;
//        }
//        return false;
//    }

//    private static String findParent(JsonArray array, String newItem) {
////        System.out.println("FIND PARENT");
//        for (JsonValue value: array) {
//            String index = value.toString().replaceAll("\"", "");
////            System.out.println("existing: "+index+" new: "+newItem);
//            if (!index.equals(newItem) && index.equals(newItem.substring(0, 3) + "XX")) return index;
//            if (!index.equals(newItem) && index.equals(newItem.substring(0, 2) + "-XX")) return index;
//            if (!index.equals(newItem) && index.equals(newItem.substring(0, 2) + "-" + index.substring(3, 5))) return index;
//        }
//        return "";
//    }

//    public static void extractMSC2() {
//        String filePath = Constants.HOME.toString() + "MathCategoriesEN.xml";
//        String result = Constants.HOME.toString() + "englishArticlesMSC.json";
//        JsonArray array = JSON.readAny(result).getAsArray();
//        Document indexesXML = null;
//        int count = 0;
//        try {
//            indexesXML = Jsoup.parse(new File(filePath), "UTF-8");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (indexesXML == null) return;
//        Elements categories = indexesXML.getElementsByTag("category");
//        for (Element category: categories) {
//            Elements indexes = category.getElementsByTag("msc");
//            if (indexes.size() != 1) continue;
//            JsonArray index = new JsonArray();
//            index.add(indexes.first().text());
//            ++count;
//            Elements articles = category.getElementsByTag("item");
//            for (Element article:articles) {
//                String title = article.text();
//                JsonObject object = new JsonObject();
//                object.put("title", title);
//                object.put("MSC", index);
//                object.put("id", -1);
//                array.add(object);
//            }
//        }
//        System.out.println(count);
//        try {
//            FileWriter fileMSC = new FileWriter(result);
//            fileMSC.write(array.toString());
//            fileMSC.flush();
//            fileMSC.close();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private static int minIndex(int a, int b) {
        if (a == -1) return b;
        if (b == -1) return a;
        if (a > b) return b;
        return a;
    }

    private static String removeBetween(String sub1, String sub2, String source,
                                        List<String> cut, int count, boolean toSave) {
//        System.out.println(source);
        int sub1Index = source.indexOf(sub1);
        String result = source;
        String current = source;
        while (sub1Index > -1) {
            int sub2Index = current.indexOf(sub2, sub1Index);
//            System.out.println(sub1Index+" "+sub2Index);
//            System.out.println(current.charAt(sub1Index)+ " "+current.charAt(sub2Index));
//            System.out.println(current.substring(sub1Index, sub2Index));
            if (sub2Index > -1) {
                count++;
                if (toSave) {
                    result = current.substring(0, sub1Index) + " йод " + current.substring(sub2Index + sub2.length());
//                    System.out.println(current.substring(sub1Index, sub2Index)+sub2);
                    cut.add(current.substring(sub1Index, sub2Index)+sub2);
                }
                else {
                    result = current.substring(0, sub1Index) + " " + current.substring(sub2Index + sub2.length());
                }
            }
            else break;
//            System.out.println(result);

            sub1Index = result.indexOf(sub1);
            current = result;
        }
        return result;
    }

//    public static void translateEnglishArticles() {
//        String filePath = Constants.HOME.toString()+"englishArticles.txt";
//        List<String> articles = new ArrayList<>();
//        try {
//            BufferedReader in = new BufferedReader(new FileReader(filePath));
//            String article;
//            //            System.out.println(in);
//            while ((article = in.readLine()) != null) {
//                articles.add(article);
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        String resultFile = Constants.HOME.toString()+"englishArticlesTranslate-1.txt";
//        String translatedArticles = "";
//        int keyIndex = 0;
//        int currentIndex = 5200;
//        int endIndex = articles.size();
//        try {
//            FileWriter file = new FileWriter(resultFile, true);
//            int tries = 1;
//            while (currentIndex < endIndex && tries <= keys.size()) {
//                String key = keys.get(keyIndex);
//                String article = articles.get(currentIndex);
//                String[] parts = article.split("--:--");
//                String title = yandexTranslate(parts[0], key);
//                String text = yandexTranslate(parts[1], key);
//                if (title.equals("") || text.equals("")) {
//                    System.out.println("not translated: " + currentIndex + " with " + keyIndex);
////                        if (tries == keys.size()) break;
//                    ++tries;
//                    keyIndex = (keyIndex + 1) % keys.size();
//                } else {
//                    System.out.println(currentIndex + " with key " + keyIndex);
//                    translatedArticles += (title + " --:-- " + text + "\n");
//                    currentIndex++;
//                    tries = 1;
//                }
//            }
////                article = title+"\n"+text;
//            file.write(translatedArticles);
//            file.flush();
//            file.close();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

//    private static String yandexTranslate(String requestText, String key) {
//        boolean flag = false;
//        try {
////                    ++tries;
//            String requestUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?key="
//                    + key + "&text=" + URLEncoder.encode(requestText, "UTF-8") + "&lang=en-ru";
//
//            URL url = new URL(requestUrl);
//            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
//            httpConnection.connect();
//            int rc = httpConnection.getResponseCode();
//
//            if (rc == 200) {
//                String line = null;
//                BufferedReader buffReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
//                StringBuilder strBuilder = new StringBuilder();
//                while ((line = buffReader.readLine()) != null) {
//                    strBuilder.append(line);
//                }
//                String translation = strBuilder.toString();
//                JsonObject object = JSON.parse(translation);
//
//                StringBuilder sb = new StringBuilder();
//                JsonArray array = (JsonArray) object.get("text");
//                for (Object s : array) {
//                    sb.append(s.toString());
//                }
//                translation = sb.toString();
//                translation = translation.replaceAll("\"", "");
//                return translation;
////                System.out.println(currentIndex+" with key "+keyIndex);
////                translatedArticles += (translation + "\n");
////                currentIndex++;
////                tries = 1;
//            } else {
//                return "";
////                System.out.println("not translated: "+currentIndex+" with "+keyIndex+" code = "+rc);
//////                        if (tries == keys.size()) break;
////                ++tries;
////                keyIndex = (keyIndex + 1) % keys.size();
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

    private static void PlayMusic(String fileName) {
        try {
            File file = new File(fileName);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.setFramePosition(0);
            clip.start();
            Thread.sleep(5950);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tokenizeEnglishArticles() {
        String filePath = Constants.HOME.toString()+"englishArticlesTranslate.txt";
        List<String> articles = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String article;
            //            System.out.println(in);
            while ((article = in.readLine()) != null) {
                articles.add(article);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String resultFile = Constants.HOME.toString()+"englishArticlesTokenized.txt";
        try {

            int startIndex = 600;
            int endIndex = 1100;
            for (int i = startIndex; i < endIndex; ++i) {
                System.out.println("article: "+i);
                String article = articles.get(i);
                String[] parts = article.split("--:--");
                String title = parts[0];
                if (parts.length > 1) article = parts[1];
                String tokTitle = Mapper.getTokenized(title);
                String tokArticle = Mapper.getTokenized(article);
                try {
                    FileWriter file = new FileWriter(resultFile, true);
                    file.write(tokTitle+" --:-- "+tokArticle+"\n");
                    file.flush();
                    file.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseYandexLemmas() {
        String filePath = basePath + "englishArticlesRawLemmas.txt";
        List<String> articles = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String article;
            //            System.out.println(in);
            while ((article = in.readLine()) != null) {
                articles.add(article);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String resultFile = basePath + "englishArticlesLemmas.txt";
        try {

            int startIndex = 0;
            int endIndex = articles.size();
            for (int i = startIndex; i < endIndex; ++i) {
                System.out.println(i);
                String article = articles.get(i);

                String[] parts = article.split(" --:-- ");
                String title_lemmas = parts[0];
                String text_lemmas = parts[1];
                String title = extract(title_lemmas);
                String text = extract(text_lemmas);
                try {
                    FileWriter file = new FileWriter(resultFile, true);
                    file.write(title+" --:-- "+text+"\n");
                    file.flush();
                    file.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static String extract(String source) {
        Pattern lemmasPattern = Pattern.compile("\\{[^\\}]+\\}");
        Matcher lemmasMatcher = lemmasPattern.matcher(source);
        String result = "";
        while (lemmasMatcher.find()) {
            String group = lemmasMatcher.group();
//            System.out.println(group);
            group = group.replaceAll("[\\{\\}]", "");
//            System.out.println(group);
            group = group.replaceAll("\\?*", "");
//            System.out.println(group);
            String[] parts = group.split("\\|");
//            System.out.println("part0: "+parts[0]);
            result += parts[0] + " ";
        }
        return result;
    }
}
