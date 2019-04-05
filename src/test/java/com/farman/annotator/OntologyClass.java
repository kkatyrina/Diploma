package com.farman.annotator;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;

public class OntologyClass {
    private static String filename;
    private static Model model;
    private static OntModel ontModel;
    private static InfModel inf;

    public static void main(String args[]) {
        PersonClass.loadPersons();
        TermClass.loadTermsAndArticles();
        IndexClass.loadIndexes();

        IndexClass.loadBindIndexArticles();
    }
}
