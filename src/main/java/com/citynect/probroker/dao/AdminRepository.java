package com.citynect.probroker.dao;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.Admin;

public interface AdminRepository extends MongoRepository<Admin, String> {

	Optional<Admin> findByNumber(String number);

}
