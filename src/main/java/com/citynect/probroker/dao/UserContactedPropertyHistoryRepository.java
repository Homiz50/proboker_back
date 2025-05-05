package com.citynect.probroker.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.UserContactedPropertyHistory;

public interface UserContactedPropertyHistoryRepository extends MongoRepository<UserContactedPropertyHistory, String> {

	UserContactedPropertyHistory findByUserId(String userId);

}
