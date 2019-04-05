package com.farman.annotator;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MappingTest {
    static List<String> queryString = new ArrayList<>();
    static List<String> not_tokenized = new ArrayList<>();
    static int docs = 0;
    static final String RUSSIAN = "russian";
    static final String ENGLISH = "english";
    static List<String> titlesEn = new ArrayList<>();
    static List<String> titlesRu = new ArrayList<>();
    static List<String> titlesRuTokenized = new ArrayList<>();

    public static void main(String args[]) throws IOException{
        not_tokenized = getDocuments(ENGLISH, false);
        queryString = getDocuments(RUSSIAN, true);
//        String filePath1 = AnnotationTest.class.getClassLoader().getResource("math").getPath() + "/" + "not_tok.txt";
//        String filePath2 = AnnotationTest.class.getClassLoader().getResource("math").getPath() + "/" + "queryString.txt";
//        FileWriter file1 = new FileWriter(filePath1);
//        file1.write(not_tokenized.get(1));
//        file1.flush();
//        file1.close();
//        System.out.println("done");
//        FileWriter file2 = new FileWriter(filePath2);
//        file2.write(queryString.get(1));
//        file2.flush();
//        file2.close();
//        System.out.println("done yet");

//        Set<String> titles = new HashSet<>();
//        for (int i = 0; i < titlesEn.size(); i++)
//            if (!not_tokenized.get(i).contains("#REDIRECT"))
//                titles.add(Mapper.getTokenized2(titlesEn.get(i).toLowerCase()));
//        for (String str: titlesRu)
//            titlesRuTokenized.add(Mapper.getTokenized2(str));


        JsonObject articles = new JsonObject();
        JsonArray array = new JsonArray();
        String filePath = AnnotationTest.class.getClassLoader().getResource("math").getPath() + "/" + "articleMap.json";
//        FileWriter prefile = new FileWriter(filePath);
//        prefile.flush();
//        prefile.close();
        List<String> rus = new ArrayList<>();
        List<String> en = new ArrayList<>();
        String ruFile = AnnotationTest.class.getClassLoader().getResource("math").getPath() + "/" + "ru.txt";
        String enFile = AnnotationTest.class.getClassLoader().getResource("math").getPath() + "/" + "en.txt";
        for (int i = 0; i < titlesRu.size(); ++i) {
            rus.add(titlesRu.get(i)+" --:-- "+queryString.get(i));
        }
        for (int i = 0; i < titlesEn.size(); ++i) {
            en.add(titlesEn.get(i)+" --:-- "+not_tokenized.get(i));
        }

        float [] parts = new float[]{/*0.05f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f,*/ 0.8f/*, 0.9f, 0.99f*/};
        String result = "";
        for(float part: parts) {
//            List<Integer> res = Mapper.map(queryString, not_tokenized, part);
            List<Integer> res = Mapper.map(titlesRu, titlesEn, part);
            for (Integer i : res) System.out.println("res: "+ i);
            int containing = 0;
            int found = 0;
            int ignored = 0;
//            for (int i = 0; i < queryString.size(); i++) {
            for (int i = 0; i < titlesRu.size(); i++) {
//                if (titles.contains(Mapper.getTokenized2(titlesRu.get(i).toLowerCase())) &&
//                        !queryString.get(i).trim().startsWith("см.") &&
//                        !GeneralUtils.containsTwise(titlesRuTokenized, Mapper.getTokenized2(titlesRu.get(i)))) {
                    containing++;
                    if (res.get(i) != -1) {
//                        if (Mapper.getTokenized2(titlesRu.get(i)).equalsIgnoreCase(Mapper.getTokenized2(titlesEn.get(res.get(i))))) {
                            found++;
                            System.out.println("ru: "+titlesRu.get(i)+"\nen: "+titlesEn.get(res.get(i)));
                            JsonObject pair = new JsonObject();
                            pair.put("ru", titlesRu.get(i).toLowerCase());
                            pair.put("en", titlesEn.get(res.get(i)).toLowerCase());
                            array.add(pair);
                            articles.put("articles", array);
                            try {
                                FileWriter file = new FileWriter(filePath);
                                file.write(articles.toString());
                                file.flush();
                                file.close();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            rus.set(i, "");
                            en.set(res.get(i), "");
//                        }
                    }
                    else {
                        System.out.println("!");
//                        rus.add(titlesRu.get(i)+queryString.get(i));
//                        en.add(titlesEn.get(res.get(i))+not_tokenized.get(res.get(i)));
                        ignored++;
                    }
//                }
//                else {
//                    if (res.get(i) != -1) {
//                        found++;
//                        containing++;
//                        System.out.println("ru1: " + titlesRu.get(i) + "\nen1: " + titlesEn.get(res.get(i)));
//                        JsonObject pair = new JsonObject();
//                        pair.put("ru", titlesRu.get(i).toLowerCase());
//                        pair.put("en", titlesEn.get(res.get(i)).toLowerCase());
//                        array.add(pair);
//                        articles.put("articles", array);
//                        try {
//                            FileWriter file = new FileWriter(filePath);
//                            file.write(articles.toString());
//                            file.flush();
//                            file.close();
//                        }
//                        catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    else ignored++;
//                }
            }

            try {
                FileWriter file1 = new FileWriter(ruFile);
                for (String article : rus) {
//                    System.out.println("ru: "+article);
                    if (article.length() > 0) file1.write(article+"\n");
                }
                file1.flush();
                file1.close();
                FileWriter file2 = new FileWriter(enFile);
                for (String article : en) {
//                    System.out.println("en: "+article);
                    if (article.length() > 0) file2.write(article+"\n");
                }
                file2.flush();
                file2.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Containing = " + containing);
            System.out.println("Found = " + found);
            System.out.println("Точность = " + 100.0f * found / (containing - ignored));
            System.out.println("Полнота = " + 100.0f * found / (containing));
            result += part + ";" +  (1.0f * found / (containing - ignored)) + ";" + (1.0f * found / (containing)) + "\n";
        }
        System.out.println("result: "+result);
//        System.out.println(articles.toString());

    }

    public static List<String> getDocuments(String lang, boolean isQuery) {
        List<String> result = new ArrayList<>();
        try {
            String filePath = AnnotationTest.class.getClassLoader().getResource("math").getPath() + "/";
            if (lang.equals(RUSSIAN))
                filePath += "russianText.txt";
            else
                filePath += "englishTranslationSaved2.txt";

            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String line;
//            System.out.println(in);
            while ((line = in.readLine()) != null) {
                String [] parts = line.split(" --:-- ");
                if (lang.equals(ENGLISH))
                    titlesEn.add(parts[0]);
                else
                    titlesRu.add(parts[0]);
                result.add(parts[1]);
            }
//            System.out.println(result);
            if (!isQuery) {
                docs = result.size();
                GeneralUtils.docs = result.size();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
