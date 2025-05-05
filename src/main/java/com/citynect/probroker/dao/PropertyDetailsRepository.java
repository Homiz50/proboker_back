package com.citynect.probroker.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.citynect.probroker.entities.PropertyDetails;

public interface PropertyDetailsRepository extends MongoRepository<PropertyDetails, String> {

	List<PropertyDetails> findByFurnishedType(String string);

	boolean existsByNumberAndAddress(String number, String address);

	List<PropertyDetails> findByIdIn(List<String> propertyIds);

	@Aggregation(pipeline = {
			"{ $match: { $expr: { $regexMatch: { input: { $replaceAll: { input: '$number', find: '^\\\\+91[-]?', replacement: '' } }, regex: '^(\\\\d+)$', options: 'i' } } } }",
			"{ $group: { _id: '$number', count: { $sum: 1 } } }", "{ $match: { count: { $gt: 1 } } }" })
	List<PropertyDetails> findPropertiesWithDuplicateNumbers();

	@Query("{ 'number': { $in: ?0 } }")
	List<PropertyDetails> findPropertiesByPhoneNumbers(List<String> phoneNumbers);

	void deleteByIdIn(List<String> propertyIds);

	boolean existsByNumber(String number);

	boolean existsByNumberAndType(String number, String type);

	boolean existsByNumberAndTypeAndUnitTypeAndTitle(String number, String type, String unitType, String title);

	List<PropertyDetails> findByIsDeleted(int i);

//	@Query("{'type': {$in: ?0}, 'furnishedType': {$in: ?1}, 'area': {$in: ?2}, 'bhk': {$in: ?3}, 'rent': {$gte: ?4, $lte: ?5}}")
//	List<PropertyDetails> findPropertiesByFilter(List<String> types, List<String> furnishedTypes, List<String> areas,
//			List<String> bhks, Integer minRent, Integer maxRent);

//	Page<PropertyDetails> findPropertiesByFilter(String type, List<String> furnishedTypes, List<String> areas,
//			List<String> bhks, int i, int j, PageRequest of);
}
