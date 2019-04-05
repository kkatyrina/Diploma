package com.farman.annotator;

import org.apache.jena.atlas.json.*;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Катерина on 05.03.2019.
 */
public class IndexClass {
    private static String filename;
    private static Model model;
    private static OntModel ontModel;
    private static InfModel inf;

    private static JsonObject savedRelations = new JsonObject();
    private static List<String> MSCIndexes = new ArrayList<>();
    private static List<String> MSCCategories = new ArrayList<>();
    private static List<String> UDCIndexes = new ArrayList<>();

    private static String createNewSubject(String name) {
        return Constants.WWW + "#" + name;
    }

    public static void loadIndexes() {
        readMSCIndexes();
        setUpOntology();

        setMSCIndexes();
        setMSCRelations();
        setUDCIndexes();
        setUDCRelations();
    }

    public static void loadBindIndexArticles() {
        if (MSCIndexes.size() == 0) readMSCIndexes();
        if (model == null) setUpOntology();
        bindArticlesAndMSC();
    }

    private static void setUpOntology() {
        try {
            filename = PersonClass.class.getClassLoader().getResource("").getPath() + "/" + "MathOnt.rdf";
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

    public static void main(String args[]) throws FileNotFoundException, IOException {

        readMSCIndexes();
        setUpOntology();

        setMSCIndexes();
        setMSCRelations();
        setUDCIndexes();
        setUDCRelations();

//        bindArticlesAndMSC();
    }

    private static void readMSCIndexes() {
        Document xmlFileMSC = null;
        try {
            String filePath = IndexClass.class.getClassLoader().getResource("").getPath() + "/" + "11_MSC_Concepts.xml";
            xmlFileMSC = Jsoup.parse(new File(filePath), "UTF-8");
            Elements items = xmlFileMSC.getElementsByTag("taxon");
            for (Element item : items) {
                String index = item.getElementsByTag("code").first().ownText();
                MSCIndexes.add(index.replaceAll("--", "-").toUpperCase());

                String category = item.getElementsByTag("name").first().ownText();
                MSCCategories.add(category.replaceAll("�", ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setMSCIndexes() {

        Resource className = model.getResource(createNewSubject("Раздел_MSC"));
        System.out.println(className);

        for (int i = 0; i < MSCIndexes.size(); ++i) {
            String index = MSCIndexes.get(i);
            String category = MSCCategories.get(i);

//            System.out.println("index: " + index);
//            System.out.println("category: " + category);

            String indexURI = createNewSubject(index);
            Resource indexResource = model.createResource(indexURI);

            Property commentProperty = model.getProperty(Constants.COMMENT.toString());
            Statement commentStatement = model.createStatement(indexResource, commentProperty, category);
            model.add(commentStatement);

            Property typeProperty = model.getProperty(Constants.ELEMENT.toString());
            Statement typeStatement = model.createStatement(indexResource, typeProperty, className);
            model.add(typeStatement);
        }
        try {
            System.out.println(filename);
            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setMSCRelations() {
        Document xmlFile = null;
        try {
            String filePath = IndexClass.class.getClassLoader().getResource("").getPath() + "/" + "13_MSC_Relations.xml";
            xmlFile = Jsoup.parse(new File(filePath), "UTF-8");

            String classURI = createNewSubject("Раздел_MSC");
            Elements items = xmlFile.getElementsByTag("taxon");

            for (Element item : items) {
                String index = item.attr("uri");
                index = index.replaceAll("http://libmeta.ru/taxon/msc#", "");
                index = index.replaceAll("--", "-").toUpperCase();
                String indexURI = createNewSubject(index);

                if (ifInstance(classURI, indexURI)) {

                    Elements parents = item.getElementsByTag("parent");
                    if (!parents.isEmpty()) {
                        String parent = parents.first().ownText();
                        index = index.replaceAll("http://libmeta.ru/taxon/msc#", "");
                        parent = parent.replaceAll("http://libmeta.ru/taxon/msc#", "");

                        System.out.println("index: " + index);
                        System.out.println("parent: " + parent);
                        String parentURI = createNewSubject(parent);

                        if (ifInstance(classURI, indexURI) && ifInstance(classURI, parentURI)) {
                            Resource indexResource = model.getResource(indexURI);
                            Resource parentResource = model.getResource(parentURI);
                            Property indexProperty = model.getProperty(Constants.PROPERTY.toString() + "#Подраздел_MSC");
                            Statement indexStatement = model.createStatement(indexResource, indexProperty, parentResource);
                            model.add(indexStatement);
                        }
                    }

                    Elements related = item.getElementsByTag("related");
                    if (!related.isEmpty()) {
                        String relatedOne = related.first().ownText();
                        relatedOne = relatedOne.replaceAll("http://libmeta.ru/taxon/msc#", "");

                        System.out.println("index: " + index);
                        System.out.println("related: " + relatedOne);
                        String relatedURI = createNewSubject(relatedOne);

                        System.out.println(ifRelated(indexURI, relatedURI, "MSC_MSC"));
                        System.out.println(ifSaved(index, relatedOne));

                        if (ifInstance(classURI, relatedURI) && !ifRelated(indexURI, relatedURI, "MSC_MSC")
                                && !ifSaved(index, relatedOne)) {
                            //                    if (ifInstance(classURI, relatedURI) && !ifSaved(index, relatedOne)) {
                            Resource indexResource = model.getResource(indexURI);
                            Resource relatedResource = model.getResource(relatedURI);
                            Property relateProperty = model.getProperty(Constants.PROPERTY.toString() + "#Связан_с_разделом_MSC_MSC");
                            Statement relateStatement = model.createStatement(indexResource, relateProperty, relatedResource);
                            model.add(relateStatement);
                            saveRelation(index, relatedOne);
                        }
                    }
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

    private static void setUDCIndexes() {
        Document xmlFile = null;
        try {
            String filePath = IndexClass.class.getClassLoader().getResource("").getPath() + "/" + "12_UDC_Concepts.xml";
            xmlFile = Jsoup.parse(new File(filePath), "UTF-8");


            Elements items = xmlFile.getElementsByTag("taxon");
            String classUri = createNewSubject("Раздел_УДК");
            Resource className = model.getResource(classUri);

            for (Element item : items) {
                String index = item.getElementsByTag("code").first().ownText();
                String category = item.getElementsByTag("name").first().ownText();
    //            category = category.replaceAll("�", "");

    //            System.out.println("index: " + index);
    //            System.out.println("category: " + category);

                String indexURI = createNewSubject(index);
                Resource indexResource = model.createResource(indexURI);

                Property commentProperty = model.getProperty(Constants.COMMENT.toString());
                Statement commentStatement = model.createStatement(indexResource, commentProperty, category);
                model.add(commentStatement);

                Property typeProperty = model.getProperty(Constants.ELEMENT.toString());
                Statement typeStatement = model.createStatement(indexResource, typeProperty, className);
                model.add(typeStatement);
            }

            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setUDCRelations() {
        Document xmlFile = null;
        try {
            String filePath = IndexClass.class.getClassLoader().getResource("").getPath() + "/" + "14_UDC_Relations.xml";
            xmlFile = Jsoup.parse(new File(filePath), "UTF-8");


            String classURI = createNewSubject("Раздел_УДК");
            Elements items = xmlFile.getElementsByTag("taxon");

            for (Element item : items) {
                String index = item.attr("uri");
                index = index.replaceAll("http://libmeta.ru/taxon/udc#", "");
                String indexURI = createNewSubject(index);

                if (ifInstance(classURI, indexURI)) {

                    Elements parents = item.getElementsByTag("parent");
                    if (! parents.isEmpty()) {
                        String parent = parents.first().ownText();
                        parent = parent.replaceAll("http://libmeta.ru/taxon/udc#", "");

//                        System.out.println("index: " + index);
//                        System.out.println("parent: " + parent);

                        String parentURI = createNewSubject(parent);

                        if (ifInstance(classURI, parentURI)) {
                            Resource indexResource = model.getResource(indexURI);
                            Resource parentResource = model.getResource(parentURI);
                            Property indexProperty = model.getProperty(Constants.PROPERTY.toString() + "#Подраздел_УДК");
                            Statement indexStatement = model.createStatement(indexResource, indexProperty, parentResource);
                            model.add(indexStatement);
                        }
                    }

                    Elements related = item.getElementsByTag("related");
                    if (! related.isEmpty()) {
                        String relatedOne = related.first().ownText();
                        relatedOne = relatedOne.replaceAll("http://libmeta.ru/taxon/udc#", "");

                        System.out.println("index: " + index);
                        System.out.println("related: " + relatedOne);
                        String relatedURI = createNewSubject(relatedOne);

    //                    System.out.println(ifRelated(indexURI, relatedURI));
    //                    System.out.println(ifSaved(index, relatedOne));

                        if (ifInstance(classURI, relatedURI) && !ifRelated(indexURI, relatedURI, "УДК_УДК")
                                && !ifSaved(index, relatedOne)) {
    //                    if (ifInstance(classURI, relatedURI) && !ifSaved(index, relatedOne)) {
                            Resource indexResource = model.getResource(indexURI);
                            Resource relatedResource = model.getResource(relatedURI);
                            Property relateProperty = model.getProperty(Constants.PROPERTY.toString() + "#Связан_с_разделом_УДК_УДК");
                            Statement relateStatement = model.createStatement(indexResource, relateProperty, relatedResource);
                            model.add(relateStatement);
                            saveRelation(index, relatedOne);
                        }
                    }
                }
            }

            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean ifInstance(String classURI, String instanceURI) {

        String query = "SELECT ?s {<" + instanceURI + "> ?s <" + classURI +">}";

//        System.out.println("SparqlQuery for InstanceCheck: "+query);

        Query jenaquery = QueryFactory.create(query) ;
        QueryExecution qexec = QueryExecutionFactory.create(jenaquery, inf);
        ResultSet jenaresults = qexec.execSelect();
        if (jenaresults.hasNext()) {
            String result = jenaresults.next().toString();
//            System.out.println("result: " + result);
            if (result.equalsIgnoreCase("( ?s = rdf:type )")) {
                return true;
            }
        }
        return false;
    }

    private static boolean ifRelated(String firstURI, String secondURI, String param) {
        String forwardQuery = "SELECT ?s {<" + firstURI + "> ?s <" + secondURI +">}";
        String backQuery = "SELECT ?s {<" + secondURI + "> ?s <" + firstURI +">}";

//        System.out.println("SparqlQuery1 for RelatedCheck: " + forwardQuery);
//        System.out.println("SparqlQuery2 for RelatedCheck: " + backQuery);

        Query jenaForwardQuery = QueryFactory.create(forwardQuery);
        Query jenaBackQuery = QueryFactory.create(backQuery);

        QueryExecution forwardQexec = QueryExecutionFactory.create(jenaForwardQuery, inf);
        QueryExecution backQexec = QueryExecutionFactory.create(jenaBackQuery, inf);

        ResultSet forwardResults = forwardQexec.execSelect();
        ResultSet backResults = backQexec.execSelect();

        if (forwardResults.hasNext()) {
            String result = forwardResults.next().toString();
//            System.out.println("forward result: " + result);
            if (result.equalsIgnoreCase("( ?s = <http://www.mathEnc.ru#Связан_с_разделом_" + param + "> )")) {
                return true;
            }
        }

        if (backResults.hasNext()) {
            String result = backResults.next().toString();
//            System.out.println("back result: " + result);
            if (result.equalsIgnoreCase("( ?s = <http://www.mathEnc.ru#Связан_с_разделом_" + param + "> )")) {
                return true;
            }
        }
        return false;
    }

    private static boolean ifSaved(String first, String second) {
        if (savedRelations.hasKey(first)) {
            JsonValue JsonValues = savedRelations.get(first);
            JsonArray array = JsonValues.getAsArray();
//            System.out.println(first +" first value = "+ array.get(0));
            JsonString jsonString = new JsonString(second);
            if (array.contains(jsonString)) return true;
        }

        if (savedRelations.hasKey(second)) {
            JsonValue JsonValues = savedRelations.get(second);
            JsonArray array = JsonValues.getAsArray();
//            System.out.println(second+" first value = "+ array.get(0));
            JsonString jsonString = new JsonString(first);
            if (array.contains(jsonString)) return true;
        }
        return false;
    }

    private static void saveRelation(String first, String second) {
        if (savedRelations.hasKey(first)) {
            JsonValue JsonValues = savedRelations.get(first);
            JsonArray array = JsonValues.getAsArray();
            array.add(second);

            savedRelations.remove(first);
            savedRelations.put(first, array);

//            System.out.println("saved value = "+allValues);
            return;
        }
        if (savedRelations.hasKey(second)) {
            JsonValue JsonValues = savedRelations.get(second);
            JsonArray array = JsonValues.getAsArray();
            array.add(second);

            savedRelations.remove(second);
            savedRelations.put(second, array);

//            System.out.println("saved value = "+allValues);
            return;
        }
        JsonArray array = new JsonArray();
        array.add(second);
        savedRelations.put(first, array);
//        JsonValue savedValues = savedRelations.get(first);
//        System.out.println("saved value = "+savedValues.toString());
    }

    private static List<String> getSearchResults(String queryString) {

        Query jenaquery = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(jenaquery, inf);
        ResultSet jenaresults = qexec.execSelect();
        ArrayList<String> results = new ArrayList<String>();

        while(jenaresults.hasNext()) {
            String soln = jenaresults.next().toString();
            System.out.println(soln);
            if ((soln.contains("#")) && (soln.contains(">")))
                soln = soln.substring(soln.indexOf('#')+1, soln.indexOf('>'));
            if (soln.contains("\""))
                soln = soln.substring(soln.indexOf('"') + 1, soln.lastIndexOf('"'));
            if (soln.contains("<") && soln.contains(">"))
                soln = soln.substring(soln.indexOf('<')+1, soln.indexOf('>'));
            results.add(soln);
        }
        return results;
    }

    private static String findNearest(String index) {
        if (MSCIndexes.contains(index)) return index;
        index = index.replaceAll("--", "-");
        String first = index.substring(0, 3) + "XX";
        if (MSCIndexes.contains(first)) return first;
        String second = index.substring(0, 2) + "-" + index.substring(3, 5);
        if (MSCIndexes.contains(second)) return second;
        String third = index.substring(0, 2) + "-XX";
        if (MSCIndexes.contains(third)) return third;
        return "";
    }

    private static void bindArticlesAndMSC() {
        String filePath = IndexClass.class.getClassLoader().getResource("").getPath() + "/" + "englishArticlesMSC3.json";
        JsonObject object = JSON.readAny(filePath).getAsObject();
        Set<String> keys = object.keys();
        for (String key: keys) {
            JsonArray indexes = object.get(key).getAsArray();
            for (JsonValue indexValue: indexes) {
                String index = indexValue.toString();
                index = index.replaceAll("\"", "").toUpperCase();
//                if (index.length() == 3) index = index + "XX";
//                if (index.length() == 4) index = index.substring(0, 2) + "-" + index.substring(2, 4);
//                System.out.println(index);
                String nearest = findNearest(index);

                String title = key.replaceAll("\"", "");
                title = "Article_" + title.replaceAll("\"", "").replaceAll(" ", "_");

                String articleClassURI = createNewSubject("Статья_мат_энциклопедии_англ");
                String indexClassURI = createNewSubject("Раздел_MSC");
                String indexURI = createNewSubject(nearest.toUpperCase());
                String articleURI = createNewSubject(title);

                if (ifInstance(articleClassURI, articleURI) && ifInstance(indexClassURI, indexURI)) {
                    Resource articleResource = model.getResource(articleURI);
                    Resource indexResource = model.getResource(indexURI);
                    Property bindProperty = model.getProperty(Constants.WWW + "#" + "Относится_к_разделу");
                    Statement bindStatement = model.createStatement(articleResource, bindProperty, indexResource);
                    model.add(bindStatement);
                }
            }
        }
        try {
            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
