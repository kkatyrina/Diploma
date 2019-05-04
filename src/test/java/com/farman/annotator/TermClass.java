package com.farman.annotator;

/**
 * Created by Катерина on 28.02.2019.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

public class TermClass {
    private static String filename;
    private static Model model;
    private static OntModel ontModel;
    private static InfModel inf;
    private static List<String> termsEnglish = new ArrayList<>();
    private static List<String> articlesEnglish = new ArrayList<>();
    private static List<String> termsRussian = new ArrayList<>();
    private static List<String> articlesRussian = new ArrayList<>();
    private static String basePath = "";

    private static String createNewSubject(String name) {
        return Constants.WWW + "#" + name;
    }

    public static void loadTermsAndArticles() {
        basePath = TermClass.class.getClassLoader().getResource("").getPath() + "/";
//        readTermsAndArticlesEnglish();
        String russianArticlesAnnotated = basePath + "russianAnnotations.json";
//        AnnotationClass.annotate(russianArticlesAnnotated);
//        readTermsAndArticlesRussian(russianArticlesAnnotated);
        setUpOntology();
//        setTermsAndArticlesEnglish();
//        setTermsAndArticlesRussian();
        setRelationsTermsArticles();
    }

    public static void main(String args[]) {
        loadTermsAndArticles();
    }

    private static void setUpOntology() {
        try {
            filename = basePath + "MathOnt.rdf";
            model = ModelFactory.createDefaultModel();
            InputStream in = new FileInputStream(filename);
            model = model.read(in, null);
            inf = ModelFactory.createRDFSModel(model);
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readTermsAndArticlesEnglish() {
        String filePath = basePath + "soma.json";
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);

            for (int i = 0; i < array.size(); ++i) {
                JsonObject object = array.get(i).getAsJsonObject();
                String title = object.get("name").toString();
                if (title.charAt(0) == '"') title = title.substring(1);
                if (title.charAt(title.length() - 1) == '"') title = title.substring(0, title.length() - 1);
                title = title.replaceAll("_", " ");
                title = title.replaceAll("\\\\u2013", "-");
                int number = 2;
                String newTitle = title;
                while (termsEnglish.contains(newTitle)) {
                    newTitle = title + "(" + number + ")";
                    number++;
                }

                String rawText = object.get("text").toString();
                termsEnglish.add(title);
                articlesEnglish.add(rawText);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readTermsAndArticlesRussian(String filePath) {
//        String filePath = basePath + "russian.json";
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);
            for (int i = 0; i < array.size(); ++i) {
                JsonObject object = array.get(i).getAsJsonObject();
                String title = object.get("title").getAsString();
//                if (title.charAt(0) == '"') title = title.substring(1);
//                if (title.charAt(title.length() - 1) == '"') title = title.substring(0, title.length() - 1);
//                title = title.replaceAll("_", " ");
//                title = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();
                int number = 2;
                String newTitle = title;
                while (termsRussian.contains(newTitle)) {
                    newTitle = title + "(" + number + ")";
                    number++;
                }

                String rawText = object.get("text").getAsString();
//                System.out.println(rawText);
//                String newText = rawText;
//                newText = ArticleClass.removeBetween("<!-- *** native", "push({});", newText, cut, cut.size(), false);
//                newText = removeBetween("<!-- *** buf", "<!-- *** text *** -->", newText, cut, cut.size(), false);
                termsRussian.add(title);
                articlesRussian.add(rawText);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setTermsAndArticlesEnglish() {
        String termClassUri = createNewSubject("Термин");
        Resource termClassName = model.getResource(termClassUri);
        String artClassUri = createNewSubject("Статья_мат_энциклопедии_англ");
        Resource artClassName = model.getResource(artClassUri);

        for (int i = 0; i < termsEnglish.size(); ++i) {
            String term = termsEnglish.get(i);
            String article = articlesEnglish.get(i);
            String comment = "";
//            Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(term);
//            while(m.find()) {
//                comment = m.group(1);
//            }

//            term = term.replaceAll("[ ]*\\(([^)]+)\\)", "");
            term = term.replaceAll(" ", "_");

            String termURI = createNewSubject(term);
            Resource termResource = model.createResource(termURI);
            String articleURI = createNewSubject("Article_"+term);
            Resource articleResource = model.createResource(articleURI);

            //SET Comment to Term and Article classes
            if (comment.length() > 0) {
                Property commentProperty = model.getProperty(Constants.COMMENT.toString());
                Statement commentStatement = model.createStatement(termResource, commentProperty, comment);
                model.add(commentStatement);
                commentStatement = model.createStatement(articleResource, commentProperty, comment);
                model.add(commentStatement);
            }

            //SET Article text
            Property textProperty = model.getProperty(Constants.ISDEFINEDBY.toString());
            Statement textStatement = model.createStatement(articleResource, textProperty, article);
            model.add(textStatement);

            //SET individuals of Term and Article classes
            Property elementProperty = model.getProperty(Constants.ELEMENT.toString());
            Statement elementStatement = model.createStatement(termResource, elementProperty, termClassName);
            model.add(elementStatement);
            elementStatement = model.createStatement(articleResource, elementProperty, artClassName);
            model.add(elementStatement);

            //SET property between Term and Article
            Property artTermProperty = model.getProperty(Constants.PROPERTY.toString() + "#Содержится_в_названии_терм_англ");
            Statement artTermStatement = model.createStatement(termResource, artTermProperty, articleResource);
            model.add(artTermStatement);

        }
        try {
            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void setTermsAndArticlesRussian() {
        String termClassUri = createNewSubject("Термин");
        Resource termClassName = model.getResource(termClassUri);
        String artClassUri = createNewSubject("Статья_мат_энциклопедии_рус");
        Resource artClassName = model.getResource(artClassUri);
        String title = "";
        try {
            for (int i = 0; i < termsRussian.size(); ++i) {
                System.out.println(i);
                String term = termsRussian.get(i);
//            title = term;
                String article = articlesRussian.get(i);
//                article = article.replaceAll("\\\\u001a","");
//                System.out.println(article.length());
//                article = article.substring(10979, 10980);
//                article = article.substring(10940, 11000);
                title = article;
                String comment = "";
//            Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(term);
//            while(m.find()) {
//                comment = m.group(1);
//            }

//            term = term.replaceAll("[ ]*\\(([^)]+)\\)", "");
                term = term.replaceAll(" ", "_");

                String termURI = createNewSubject(term);
                Resource termResource = model.createResource(termURI);
                String articleURI = createNewSubject("Статья_" + term);
                Resource articleResource = model.createResource(articleURI);

                //SET Comment to Term and Article classes
                if (comment.length() > 0) {
                    Property commentProperty = model.getProperty(Constants.COMMENT.toString());
                    Statement commentStatement = model.createStatement(termResource, commentProperty, comment);
                    model.add(commentStatement);
                    commentStatement = model.createStatement(articleResource, commentProperty, comment);
                    model.add(commentStatement);
                }

                //SET Article text
                Property textProperty = model.getProperty(Constants.ISDEFINEDBY.toString());
                Statement textStatement = model.createStatement(articleResource, textProperty, article);
                model.add(textStatement);

                //SET individuals of Term and Article classes
                Property elementProperty = model.getProperty(Constants.ELEMENT.toString());
                Statement elementStatement = model.createStatement(termResource, elementProperty, termClassName);
                model.add(elementStatement);
                elementStatement = model.createStatement(articleResource, elementProperty, artClassName);
                model.add(elementStatement);

                //SET property between Term and Article
                Property artTermProperty = model.getProperty(Constants.PROPERTY.toString() + "#Содержится_в_названии_терм_рус");
                Statement artTermStatement = model.createStatement(termResource, artTermProperty, articleResource);
                model.add(artTermStatement);



            }
            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        }
//        try {
//            OutputStream out = new FileOutputStream(filename);
//            model.write(out);
//            out.close();
//        }
        catch(Exception e) {
            System.out.println(title);
            e.printStackTrace();
            ArticleClass.PlayMusic();
        }
    }

    private static void setRelationsTermsArticles() {
        String filePath = basePath + "russianAnnotations.json";
        try {
//            String termClassUri = createNewSubject("Термин");
//            Resource termClassName = model.getResource(termClassUri);
//            String artClassUri = createNewSubject("Статья_мат_энциклопедии_рус");
//            Resource artClassName = model.getResource(artClassUri);
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);
            for (int i = 0; i < array.size(); ++i) {
                JsonObject articleObject = array.get(i).getAsJsonObject();
                String title = articleObject.get("title").getAsString();
                String article = "Статья_" + title;
                String articleURI = createNewSubject(article);
                Resource articleResource = model.createResource(articleURI);
                JsonArray bindArray = articleObject.get("bindTitles").getAsJsonArray();

                for (int k = 0; k < bindArray.size(); ++k) {
                    String bindTitle = bindArray.get(k).getAsString();
                    String termURI = createNewSubject(bindTitle);
                    Resource termResource = model.createResource(termURI);
                    Property artTermProperty = model.getProperty(Constants.PROPERTY.toString() + "#Упоминается_в_тексте_терм_рус");
                    Statement artTermStatement = model.createStatement(termResource, artTermProperty, articleResource);
                    model.add(artTermStatement);
                }
            }
            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void Oldmain(String args[]) throws FileNotFoundException, IOException{

        filename = Constants.HOME + "MathOnt.rdf";
        model = ModelFactory.createDefaultModel();//.read(FileManager.get().open(filename), "");
        InputStream in = new FileInputStream(filename);
        model = model.read(in, null);
        inf = ModelFactory.createRDFSModel(model);
        ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);

        String termClassUri = createNewSubject("Термин");
        Resource termClassName = model.getResource(termClassUri);
        String artClassUri = createNewSubject("Статья_мат_энциклопедии_англ");
        Resource artClassName = model.getResource(artClassUri);

        Document xmlFile = null;
        try {
            xmlFile = Jsoup.parse(new File(Constants.HOME + "MathItemsEN2.xml"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String filePath = AnnotationTest.class.getClassLoader().getResource("math").getPath() + "/" + "2english.txt";
        FileWriter file = new FileWriter(filePath);

        Elements items = xmlFile.getElementsByTag("item");
        List<String> terms = new ArrayList<>();
        for (Element item : items) {
//            System.out.println(item);
            String preTerm = item.getElementsByTag("title").first().ownText();
            String term = preTerm.substring(0, 1).toUpperCase() + preTerm.substring(1).toLowerCase();

            Element textElement = item.getElementsByTag("text").first();
//            textElement = textElement.removeClass("align");
//            Elements imgs = textElement.getElementsByTag("img");
//            for (Element img : imgs) img.remove();

//            String text = item.getElementsByTag("text").first().ownText();
            String text = textElement.text();
//            terms.add(term.replaceAll(" ", "_"));
//              terms.add(term);
            System.out.println(term);

            String comment = "";
            Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(term);
            while(m.find()) {
                comment = m.group(1);
            }

            term = term.replaceAll("[ ]*\\(([^)]+)\\)", "");
            term = term.replaceAll(" ", "_");

//            System.out.println(term + "             comment: " + comment);

            text = text.replaceAll("<!\\[CDATA\\[", "");
            text = text.replaceAll("\\]\\]>", "");
//            System.out.println(text);

            //Перезапись в текстовый файл!!!
//            String newText = text.replaceAll("\\<([^)]+)\\>", "|");
//            String newText = text.replaceAll("<[^>]*>", "|");
//            file.write(term+" --:-- "+newText+"\n");
//            file.flush();


//            String art = "Article_" + term;
//            String artURI = createNewSubject(art);
//            Resource artResource = model.createResource(artURI);
//
//            String termURI = createNewSubject(term);
//            Resource resource = model.createResource(termURI);
//
//            if (comment.length() > 0) {
//                Property commentProperty = model.getProperty(Constants.COMMENT.toString());
//                Statement commentStatement = model.createStatement(resource, commentProperty, comment);
//                model.add(commentStatement);
//                commentStatement = model.createStatement(artResource, commentProperty, comment);
//                model.add(commentStatement);
//            }
//
//            Property labelProperty = model.getProperty(Constants.PROPERTY.toString()+"#label");
////            System.out.println(property.getURI());
//            Statement labelStatement = model.createStatement(resource, labelProperty, "eng");
//            model.add(labelStatement);
//
//            Property property = model.getProperty(Constants.ELEMENT.toString());
////            System.out.println(property.getURI());
//            Statement statement = model.createStatement(resource, property, termClassName);
//            model.add(statement);
//
//            Statement artStatement = model.createStatement(artResource, property, artClassName);
//            model.add(artStatement);
//
//            Property textProperty = model.getProperty(Constants.ISDEFINEDBY.toString());
//            Statement textStatement = model.createStatement(artResource, textProperty, text);
//            model.add(textStatement);
//
//            Property artTermProperty = model.getProperty(Constants.PROPERTY.toString() + "#Содержится_в_названии_терм_англ");
//            Statement artTermStatement = model.createStatement(resource, artTermProperty, artResource);
//            model.add(artTermStatement);
        }
//        OutputStream out = new FileOutputStream(filename);
//        model.write(out);
//        out.close();

        file.close();
    }
}
