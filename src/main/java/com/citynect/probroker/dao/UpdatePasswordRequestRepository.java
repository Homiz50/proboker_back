package com.citynect.probroker.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.UpdatePasswordRequest;

public interface UpdatePasswordRequestRepository extends MongoRepository<UpdatePasswordRequest, String> {

}
