package com.citynect.probroker.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "User")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

	@Id
	private String id;
	private String companyName;
	private String name;
	private String number;
	private String email;
	private String address;
	private String password;
	private int wrongPassLimit;
	private int limit;
	private int totalCount;
	private ActivePlanDetails activePlanDetails;
	private int isPremium;
	// New fields
	private List<String> savedPropertyIds = new ArrayList<>();
	private List<String> contactedPropertyIds = new ArrayList<>();
	private String remark;
	private String followupDate;
	private LocalDateTime createdOn;

	@Data
	public static class ActivePlanDetails {
		private String orderId;
		private String amount;
		private LocalDate expiredOn;
		private LocalDate paidOn;

		public String getOrderId() {
			return orderId;
		}

		public void setOrderId(String orderId) {
			this.orderId = orderId;
		}

		public String getAmount() {
			return amount;
		}

		public void setAmount(String amount) {
			this.amount = amount;
		}

		public LocalDate getExpiredOn() {
			return expiredOn;
		}

		public void setExpiredOn(LocalDate expiredOn) {
			this.expiredOn = expiredOn;
		}

		public LocalDate getPaidOn() {
			return paidOn;
		}

		public void setPaidOn(LocalDate paidOn) {
			this.paidOn = paidOn;
		}

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getWrongPassLimit() {
		return wrongPassLimit;
	}

	public void setWrongPassLimit(int wrongPassLimit) {
		this.wrongPassLimit = wrongPassLimit;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public ActivePlanDetails getActivePlanDetails() {
		return activePlanDetails;
	}

	public void setActivePlanDetails(ActivePlanDetails activePlanDetails) {
		this.activePlanDetails = activePlanDetails;
	}

	public int getIsPremium() {
		return isPremium;
	}

	public void setIsPremium(int isPremium) {
		this.isPremium = isPremium;
	}

	public List<String> getSavedPropertyIds() {
		return savedPropertyIds;
	}

	public void setSavedPropertyIds(List<String> savedPropertyIds) {
		this.savedPropertyIds = savedPropertyIds;
	}

	public List<String> getContactedPropertyIds() {
		return contactedPropertyIds;
	}

	public void setContactedPropertyIds(List<String> contactedPropertyIds) {
		this.contactedPropertyIds = contactedPropertyIds;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getFollowupDate() {
		return followupDate;
	}

	public void setFollowupDate(String followupDate) {
		this.followupDate = followupDate;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

}