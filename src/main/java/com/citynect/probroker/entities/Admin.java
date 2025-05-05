package com.citynect.probroker.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "admin")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Admin {

	@Id
	private String id;
	private String name;
	private String number;
	private String password;
	private List<ReportedPropertyAction> reportedPropertyAction = new ArrayList<>();

	private LocalDateTime createdOn;

	public static class ReportedPropertyAction {
		private String reportId;
		private String actionType;
		private LocalDateTime reviewedDate;

		public String getReportId() {
			return reportId;
		}

		public void setReportId(String reportId) {
			this.reportId = reportId;
		}

		public String getActionType() {
			return actionType;
		}

		public void setActionType(String actionType) {
			this.actionType = actionType;
		}

		public LocalDateTime getReviewedDate() {
			return reviewedDate;
		}

		public void setReviewedDate(LocalDateTime reviewedDate) {
			this.reviewedDate = reviewedDate;
		}

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<ReportedPropertyAction> getReportedPropertyAction() {
		return reportedPropertyAction;
	}

	public void setReportedPropertyAction(List<ReportedPropertyAction> reportedPropertyAction) {
		this.reportedPropertyAction = reportedPropertyAction;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

}
