package com.citynect.probroker.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.ApiLog;

public interface ApiLogRepository extends MongoRepository<ApiLog, String> {

}
