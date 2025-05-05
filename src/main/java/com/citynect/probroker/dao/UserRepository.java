package com.citynect.probroker.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.User;

public interface UserRepository extends MongoRepository<User, String> {

	Optional<User> findByNumber(String number);

	List<User> findAllByOrderByCreatedOnDesc();

	List<User> findByIsPremium(int i);
}
