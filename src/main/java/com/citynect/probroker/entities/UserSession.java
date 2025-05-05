package com.citynect.probroker.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.citynect.probroker.entities.UserRegistrationRequest.DeviceDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "User Session")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {

	@Id
	private String id;
	private String userId; // Reference to the User
	private String ipAddress;
	private DeviceDetails deviceDetails; // Use a structured class for device details
	private String fingerprint;
	private List<LocalDateTime> loginTimes = new ArrayList<>(); // List to store multiple login times
	private LocalDateTime createdOn;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public DeviceDetails getDeviceDetails() {
		return deviceDetails;
	}

	public void setDeviceDetails(DeviceDetails deviceDetails) {
		this.deviceDetails = deviceDetails;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public List<LocalDateTime> getLoginTimes() {
		return loginTimes;
	}

	public void setLoginTimes(List<LocalDateTime> loginTimes) {
		this.loginTimes = loginTimes;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

}
