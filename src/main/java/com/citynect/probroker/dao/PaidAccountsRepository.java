package com.citynect.probroker.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.PaidAccounts;

public interface PaidAccountsRepository extends MongoRepository<PaidAccounts, String> {

	List<PaidAccounts> findByUserId(String userId);

	List<PaidAccounts> findByExpiredDateBeforeAndStatus(LocalDate currentDate, String string);

	List<PaidAccounts> findAllByOrderByCreatedOnDesc();

}
