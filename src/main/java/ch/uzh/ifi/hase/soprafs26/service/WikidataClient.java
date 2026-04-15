package ch.uzh.ifi.hase.soprafs26.service;

public interface WikidataClient {
    String executeSelectQuery(String sparqlQuery);
}
