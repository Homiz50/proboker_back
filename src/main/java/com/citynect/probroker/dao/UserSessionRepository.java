package com.citynect.probroker.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citynect.probroker.entities.UserRegistrationRequest.DeviceDetails;
import com.citynect.probroker.entities.UserSession;

public interface UserSessionRepository extends MongoRepository<UserSession, String> {

	UserSession findByUserIdAndIpAddressAndDeviceDetailsAndFingerprint(String userId, String ipAddress,
			DeviceDetails deviceDetails, String fingerprint);

}
