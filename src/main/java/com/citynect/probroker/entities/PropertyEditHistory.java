package com.citynect.probroker.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "property edit history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyEditHistory {
	@Id
	private String id;
	private String propertyId; // ID of the property edited
	private String adminId; // Admin who made the edit
	private String changes; // A description of the changes made
	private LocalDateTime editedOn; // Timestamp of the edit

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public LocalDateTime getEditedOn() {
		return editedOn;
	}

	public void setEditedOn(LocalDateTime editedOn) {
		this.editedOn = editedOn;
	}

	public String getChanges() {
		return changes;
	}

	public void setChanges(String changes) {
		this.changes = changes;
	}

}
