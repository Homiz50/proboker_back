package com.citynect.probroker.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.PropertyEditHistory;

public interface PropertyEditHistoryRepository extends MongoRepository<PropertyEditHistory, String> {

}
