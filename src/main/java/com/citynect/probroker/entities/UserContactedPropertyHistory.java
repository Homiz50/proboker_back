package com.citynect.probroker.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "user_contacted_property_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContactedPropertyHistory {

	@Id
	private String id;
	private String userId;
	private List<ContactHistoryEntry> contactHistory;
	private LocalDateTime createdOn;

	@Data
	public static class ContactHistoryEntry {
		private String propertyId;
		private LocalDateTime contactedDate;

		public String getPropertyId() {
			return propertyId;
		}

		public void setPropertyId(String propertyId) {
			this.propertyId = propertyId;
		}

		public LocalDateTime getContactedDate() {
			return contactedDate;
		}

		public void setContactedDate(LocalDateTime contactedDate) {
			this.contactedDate = contactedDate;
		}
	}

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

	public List<ContactHistoryEntry> getContactHistory() {
		return contactHistory;
	}

	public void setContactHistory(List<ContactHistoryEntry> contactHistory) {
		this.contactHistory = contactHistory;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

}
