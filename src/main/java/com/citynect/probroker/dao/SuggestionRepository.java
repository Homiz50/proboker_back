package com.citynect.probroker.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.Suggestion;

public interface SuggestionRepository extends MongoRepository<Suggestion, String> {

}
