package com.citynect.probroker.dao;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.Numbers;

public interface NumbersRepository extends MongoRepository<Numbers, String> {

//	Optional<Numbers> findByType(String type);

	Optional<Numbers> findByTypeAndStatus(String type, String status);

}
