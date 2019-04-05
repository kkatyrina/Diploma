package com.farman.annotator;

/**
 * Created by Катерина on 05.03.2019.
 */
public enum Constants {
    CLASS("http://www.w3.org/2002/07/owl#Class"),
    SUBCLASS("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
    ELEMENT("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
    LANGSTRING("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"),
    OBJECTPROPERTY("http://www.w3.org/2002/07/owl#ObjectProperty"),
    PROPERTY("http://www.mathEnc.ru"),
    RDFS("http://www.w3.org/2000/01/rdf-schema"),
    LABEL("http://www.w3.org/2000/01/rdf-schema#label"),
    OWL("http://www.w3.org/2002/07/owl"),
    HOME("D:\\disser1\\src\\test\\resources\\"),
    COMMENT("http://www.w3.org/2000/01/rdf-schema#comment"),
    ISDEFINEDBY("http://www.w3.org/2000/01/rdf-schema#isDefinedBy"),
    SAMEAS("http://www.w3.org/2002/07/owl#sameAs"),
    WWW("http://www.mathEnc.ru");

    private String value;

    Constants(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
