package com.citynect.probroker.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.citynect.probroker.entities.UserPropertyStatus;

public interface UserPropertyStatusRepository extends MongoRepository<UserPropertyStatus, String> {

	Optional<UserPropertyStatus> findByUserIdAndPropId(String changedByUserId, String propertyId);

	List<UserPropertyStatus> findByUserIdAndPropIdIn(String id, List<String> collect);

	@Query("{ 'status': { $in: ['Sell out', 'Rent out', 'Broker' ,'Duplicate' , 'Data Mismatch'] }, 'isVerified': 0 }")
	List<UserPropertyStatus> findUnverifiedPropertiesWithSpecificStatuses();
}
