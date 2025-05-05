package com.citynect.probroker.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.citynect.probroker.entities.DemoAccounts;

public interface DemoAccountsRepository extends MongoRepository<DemoAccounts, String> {

	List<DemoAccounts> findByExpiredDateBeforeAndStatus(LocalDate currentDate, String string);

	List<DemoAccounts> findAllByOrderByCreatedOnDesc();

	List<DemoAccounts> findByPaymentStatusOrderByCreatedOnDesc(String string);

//	Optional<DemoAccounts> findByNumber(String number);

	List<DemoAccounts> findByNumber(String number);

}
