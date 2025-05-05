//package com.citynect.probroker.entities;
//
//import java.time.LocalDateTime;
//
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Document(collection = "Property Status Log")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class PropertyStatusChangeLog {
//
//	@Id
//	private String id;
//	private String propertyId;
//	private String changedByUserId;
//	private String previousStatus;
//	private String newStatus;
//	private LocalDateTime changeTimestamp;
//
//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}
//
//	public String getPropertyId() {
//		return propertyId;
//	}
//
//	public void setPropertyId(String propertyId) {
//		this.propertyId = propertyId;
//	}
//
//	public String getChangedByUserId() {
//		return changedByUserId;
//	}
//
//	public void setChangedByUserId(String changedByUserId) {
//		this.changedByUserId = changedByUserId;
//	}
//
//	public String getPreviousStatus() {
//		return previousStatus;
//	}
//
//	public void setPreviousStatus(String previousStatus) {
//		this.previousStatus = previousStatus;
//	}
//
//	public String getNewStatus() {
//		return newStatus;
//	}
//
//	public void setNewStatus(String newStatus) {
//		this.newStatus = newStatus;
//	}
//
//	public LocalDateTime getChangeTimestamp() {
//		return changeTimestamp;
//	}
//
//	public void setChangeTimestamp(LocalDateTime changeTimestamp) {
//		this.changeTimestamp = changeTimestamp;
//	}
//}
