package com.citynect.probroker.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.UserPropertyRemark;

public interface UserPropertyRemarkRepository extends MongoRepository<UserPropertyRemark, String> {

	Optional<UserPropertyRemark> findByPropIdAndUserId(String propId, String userId);

	List<UserPropertyRemark> findByUserIdAndPropIdIn(String id, List<String> collect);

}
