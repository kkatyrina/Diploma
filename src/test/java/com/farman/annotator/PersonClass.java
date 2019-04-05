package com.farman.annotator;

/**
 * Created by Катерина on 26.02.2019.
 */

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
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
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;


public class PersonClass {

    private static String filename;
    private static Model model;
    private static OntModel ontModel;
    private static InfModel inf;
    private static List<String> englishNames = new ArrayList<>();
    private static List<String> russianNames = new ArrayList<>();
    private static int count = 0;
    private static int found = 0;
    private static int nameIdx = 0;

    private static String createNewSubject(String name) {
        return Constants.WWW + "#" + name;
    }

    public static void loadPersons() {
        readPersons();
        translatePersons(englishNames);
        prepareNames();
        setUpOntology();
        try {
            setPersons();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    public static void main(String args[]) throws IOException, NullPointerException {

//        readPersons();

//        translatePersons(englishNames);
//        prepareNames();
//        printResults();
//        setPersons();
//        getPersons();
        setUpOntology();
        tryLabel("Абрахам_де_Муавр");
    }

    private static void readPersons() {
        Document htmlFile = null;

        try {
            String filePath = PersonClass.class.getClassLoader().getResource("").getPath() + "/" + "CategoryBiographical.htm";
            htmlFile = Jsoup.parse(new File(filePath), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Element div = htmlFile.select("div.mw-category").first();
        Elements names = div.getElementsByTag("a");

        String newName = "";
        for (Element link : names) {
            String name = link.attr("title");
            String[] parts = name.split(", ");
            if (parts.length > 1) {
                newName = parts[1] + " " + parts[0];
            }
            else newName = name;
            englishNames.add(newName.replaceAll(" ", "_"));
//            englishNames.set(nameIdx, newName.replaceAll(" ", "_"));
//            ++nameIdx;
//            System.out.println(name);
        }
    }

    public static void setPersons() throws IOException, NullPointerException {
        String classUri = createNewSubject("Персона");
        Resource className = model.getResource(classUri);
//        System.out.println(className.toString());

        for (int i = 0; i < englishNames.size(); ++i) {
            String englishName = englishNames.get(i);
            String russianName = russianNames.get(i);

//            String enNameURI = createNewSubject(englishName);
            String ruNameURI = createNewSubject(russianName);

//            Resource enResource = model.createResource(enNameURI);
            Resource ruResource = model.createResource(ruNameURI);
//            System.out.println(resource.toString());

            //SET LITERALS
            Literal enLiteral = ResourceFactory.createLangLiteral(englishName, "en");
            Literal ruLiteral = ResourceFactory.createLangLiteral(russianName, "ru");
            Property property = model.getProperty(Constants.LABEL.toString());
            model.addLiteral(ruResource, property, enLiteral);
            model.addLiteral(ruResource, property, ruLiteral);

            //SET INSTANCES
            Property instanceProperty = model.getProperty(Constants.ELEMENT.toString());
//            System.out.println(property.getURI());
//            Statement enInstanceStatement = model.createStatement(enResource, instanceProperty, className);
            Statement ruInstanceStatement = model.createStatement(ruResource, instanceProperty, className);
//            model.add(enInstanceStatement);
            model.add(ruInstanceStatement);

            //SET SAMEAS
//            Property sameAsProperty = model.getProperty(Constants.SAMEAS.toString());
//            Statement sameAsStatement = model.createStatement(enResource, sameAsProperty, ruResource);
//            model.add(sameAsStatement);

        }
        OutputStream out = new FileOutputStream(filename);
        model.write(out);
        out.close();
    }

    private static void translatePersons(List<String> names) {
        List<String> not_found = new ArrayList<>();

        for (int i = 0; i < names.size(); ++i) {
            russianNames.add("");
            not_found.add("");
            String name = names.get(i);
            name = name.replaceAll("Károly_", "Karl_");
            ++count;
//            System.out.print(name+"  ");
            ParameterizedSparqlString qs = new ParameterizedSparqlString("" +
                    "prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n" +
//                    "prefix dbo:     <http://dbpedia.org/ontology/>\n" +
//                    "prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                    "\n" +
                    "select ?ru where {\n" +
//                    " ?resource rdf:type dbo:Person.\n " +
//                    " ?resource rdfs:label ?label.\n" +
                    " <http://dbpedia.org/resource/" + name + "> " + " rdfs:label ?ru\n" +
//                    " <http://dbpedia.org/resource/" + name + "> dbo:wikiPageRedirects ?resource.\n" +
//                    " ?resource rdfs:label ?ru\n" +
                    "FILTER(langMatches(lang(?ru), \"ru\"))" +
                    "}");

//            Literal en = ResourceFactory.createLangLiteral(name, "en");
//            qs.setParam("label", en);

//            System.out.println(qs);

            QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qs.asQuery());
            ResultSet results = ResultSetFactory.copyResults(exec.execSelect());

            if (!results.hasNext()) not_found.set(i, name);
            while (results.hasNext()) {
                ++found;
                String russianName = results.next().get("ru").toString();
                russianName = russianName.replaceAll("@ru", "");
//                System.out.print(russianName+" ");
                russianNames.set(i, russianName);
                englishNames.set(i, name);
            }
//            System.out.print("\n");
        }

        System.out.println("count = "+count+" found = "+found+"\n\n\n");
        if (found < count) translateRedirectPersons(not_found);
    }

    private static void tryLabel(String name) throws FileNotFoundException {
        String query =
                "select ?ru where {\n" +
                "<" + Constants.WWW.toString() + "#" + name + "> <" + Constants.LABEL.toString() + "> ?ru\n" +
                "FILTER(langMatches(lang(?ru), \"ru\"))" +
                "}";
        System.out.println(query);

        Query jenaQuery = QueryFactory.create(query) ;
        QueryExecution qexec = QueryExecutionFactory.create(jenaQuery, inf);
        ResultSet results = qexec.execSelect();

        while (results.hasNext()) {
            ++found;
            String russianName = results.next().get("ru").toString();
            russianName = russianName.replaceAll("@ru", "");
            System.out.print(russianName+" ");
        }
    }

    private static void translateRedirectPersons(List<String> names) {
        List<String> not_found = new ArrayList<>();
        for (int i = 0; i < names.size(); ++i) {
            not_found.add("");
//            name = name.replaceAll("_", " ");
            String name = names.get(i);
            if (name.length() < 1) continue;

//            System.out.print(name+"  ");

            name = name.replaceAll("Pafnutii", "Pafnuty");
            name = name.replaceAll("Nicolaus", "Nicolas");
            name = name.replaceAll("Aleksander_Aleksandrovich_", "");
            name = name.replaceAll("Concorcet", "condorcet");
            name = name.replaceAll("é", "e");
            name = name.replaceAll("Rogerius_Josephus", "Roger");
            name = name.replaceAll("_Buffon", "");
            name = name.replaceAll("DAlembert", "Dalambert");
            name = name.replaceAll("_Pylypovych_", "_Pylypovich_");
            name = name.replaceAll("Johann_Gregor_", "Gregor_Johann_");
            name = name.replaceAll("_Federigo_Samaso_", "_Federico_Damaso_");
            name = name.replaceAll("Evgenii_", "Evgeny_");
            name = name.replaceAll("_Waddel_", "_");

//            System.out.print(name+"  ");
            ParameterizedSparqlString qs = new ParameterizedSparqlString("" +
                    "prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "prefix dbo:     <http://dbpedia.org/ontology/>\n" +
//                    "prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                    "\n" +
                    "select ?ru ?en where {\n" +
//                    " ?resource rdf:type dbo:Person.\n " +
//                    " ?resource rdfs:label ?label.\n" +
//                    " <http://dbpedia.org/resource/" + name + "> " + " rdfs:label ?ru\n" +
                    " <http://dbpedia.org/resource/" + name + "> dbo:wikiPageRedirects ?resource.\n" +
                    " ?resource rdfs:label ?ru.\n" +
                    " ?resource rdfs:label ?en\n" +
                    "FILTER(langMatches(lang(?ru), \"ru\"))" +
                    "FILTER(langMatches(lang(?en), \"en\"))" +
                    "}");

//            System.out.println(qs);

            QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qs.asQuery());
            ResultSet results = ResultSetFactory.copyResults(exec.execSelect());

            if (!results.hasNext()) not_found.set(i, name);
            while (results.hasNext()) {
                ++found;
                QuerySolution qsol = results.next();
                String russianName = qsol.get("ru").toString();
                russianName = russianName.replaceAll("@ru", "");
                String englishName = qsol.get("en").toString();
                englishName = englishName.replaceAll("@en", "");
//                System.out.print(russianName+" ");
                russianNames.set(i, russianName);
                englishNames.set(i, englishName);
            }
//            System.out.print("\n");
        }

        System.out.println("count = "+count+" found = "+found+"\n\n\n");
        if (found < count) translateYandexPersons(not_found);
    }

    private static void translateYandexPersons(List<String> names) {
        String apiKey = "trnsl.1.1.20190313T095920Z.f86b19b4ab9d5866.a1e440162ec0d18e3a15f06ba4be9b6d541fcb71";
        for (int i = 0; i < names.size(); ++i) {
            String name = names.get(i);
            if (name.length() < 1) continue;
            name = name.replaceAll("_", " ");
//            System.out.print(name+"  ");

            try {
                String requestUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?key="
                        + apiKey  + "&text=" + URLEncoder.encode(name, "UTF-8")  + "&lang=en-ru";

                URL url = new URL(requestUrl);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.connect();
                int rc = httpConnection.getResponseCode();

                if (rc == 200) {
                    String line = null;
                    BufferedReader buffReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                    StringBuilder strBuilder = new StringBuilder();
                    while ((line = buffReader.readLine()) != null) {
                        strBuilder.append(line);
                    }
                    String text = strBuilder.toString();
                    JsonObject object = JSON.parse(text);

                    StringBuilder sb = new StringBuilder();
                    JsonArray array = (JsonArray) object.get("text");
                    for (Object s : array) {
                        sb.append(s.toString());
                    }
                    text = sb.toString();
                    text = text.replaceAll(" ", "_");
                    text = text.replaceAll("\"", "");
                    if (text.charAt(text.length()-1) == '_') text = text.substring(0, text.length()-1);
//                    System.out.print(text);
                    russianNames.set(i, text);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
//            System.out.print("\n");
        }
    }

    private static void prepareNames() {
        for (int i = 0; i < englishNames.size(); ++i) {
            String englishName = englishNames.get(i);
            String russianName = russianNames.get(i);
            englishName = englishName.replaceAll(" ", "_");
            if (russianName.contains(",")) {
                String [] parts = russianName.split(", ");
                russianName = parts[1] + " " + parts[0];
            }
            russianName = russianName.replaceAll(" ", "_");
            russianName = russianName.replaceAll("\\(", "");
            russianName = russianName.replaceAll("\\)", "");

            englishNames.set(i, englishName);
            russianNames.set(i, russianName);
        }
    }

    public static void printResults() {
        System.out.println("================RESULTS================");
        for (int i = 0; i < englishNames.size(); ++i) {
            System.out.println(englishNames.get(i)+"  "+russianNames.get(i));
        }
    }
}
