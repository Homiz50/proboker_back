package com.citynect.probroker.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.citynect.probroker.dao.UserSessionRepository;
import com.citynect.probroker.entities.UserRegistrationRequest;
import com.citynect.probroker.entities.UserSession;

@Service
public class UserSessionService {

	@Autowired
	private UserSessionRepository userSessionRepository;

//	public void trackUserSession(String userId, UserRegistrationRequest registrationRequest) {
//		UserSession session = new UserSession();
//		session.setUserId(userId);
//		session.setIpAddress(registrationRequest.getIpAddress());
//		session.setDeviceDetails(registrationRequest.getDeviceDetails());
//		session.setFingerprint(registrationRequest.getFingerprint());
//		session.setLoginTime(LocalDateTime.now());
//
//		userSessionRepository.save(session);
//	}

	public void trackUserSession(String userId, UserRegistrationRequest registrationRequest) {
		// Check if a session with the same userId, ipAddress, deviceDetails, and
		// fingerprint already exists
		UserSession existingSession = userSessionRepository.findByUserIdAndIpAddressAndDeviceDetailsAndFingerprint(
				userId, registrationRequest.getIpAddress(), registrationRequest.getDeviceDetails(),
				registrationRequest.getFingerprint());

		if (existingSession != null) {
			// If session exists, append the new login time to the loginTimes list
			existingSession.getLoginTimes().add(LocalDateTime.now());
			userSessionRepository.save(existingSession);
		} else {
			// If it's a new device, create a new session
			UserSession newSession = new UserSession();
			newSession.setUserId(userId);
			newSession.setIpAddress(registrationRequest.getIpAddress());
			newSession.setDeviceDetails(registrationRequest.getDeviceDetails());
			newSession.setFingerprint(registrationRequest.getFingerprint());
			// Add the current login time to the new session
			newSession.getLoginTimes().add(LocalDateTime.now());
			newSession.setCreatedOn(LocalDateTime.now());

			userSessionRepository.save(newSession);
		}
	}

}
