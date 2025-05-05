package com.citynect.probroker.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.AdminRemarkForUser;


public interface AdminRemarkForUserRepository extends MongoRepository<AdminRemarkForUser, String>{

	Optional<AdminRemarkForUser> findByUserId(String userId);

	List<AdminRemarkForUser> findByUserIdIn(List<String> userIds);

}
