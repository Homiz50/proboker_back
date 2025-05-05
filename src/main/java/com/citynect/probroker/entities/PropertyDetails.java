package com.citynect.probroker.entities;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "Proeprty-Details")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyDetails {

	@Id
	private String id;
	private String title;
	private Date listedDate;
	private String type;
	private String rent;
	private int rentValue;
	private String bhk;
	private String furnishedType;
	private String squareFt;
	private Integer sqFt;
	private String address;
	private String area;
	private String city;
	private String status;
	private String amenities;
	private String bathrooms;
	private String description;
	private String userType;
	private String unitType;
	private String propertyCurrentStatus;
	private String description1;
	private String key;
	private String name;
	private String number;
	private int isDeleted;
	private int isSaved;
	private String remark;
	private LocalDateTime createdOn;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getListedDate() {
		return listedDate;
	}

	public void setListedDate(Date listedDate) {
		this.listedDate = listedDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRent() {
		return rent;
	}

	public void setRent(String rent) {
		this.rent = rent;
	}

	public int getRentValue() {
		return rentValue;
	}

	public void setRentValue(int rentValue) {
		this.rentValue = rentValue;
	}

	public String getBhk() {
		return bhk;
	}

	public void setBhk(String bhk) {
		this.bhk = bhk;
	}

	public String getFurnishedType() {
		return furnishedType;
	}

	public void setFurnishedType(String furnishedType) {
		this.furnishedType = furnishedType;
	}

	public String getSquareFt() {
		return squareFt;
	}

	public void setSquareFt(String squareFt) {
		this.squareFt = squareFt;
	}

	public Integer getSqFt() {
		return sqFt;
	}

	public void setSqFt(Integer sqFt) {
		this.sqFt = sqFt;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription1() {
		return description1;
	}

	public void setDescription1(String description1) {
		this.description1 = description1;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
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

	public int getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}

	public int getIsSaved() {
		return isSaved;
	}

	public void setIsSaved(int isSaved) {
		this.isSaved = isSaved;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	public String getAmenities() {
		return amenities;
	}

	public void setAmenities(String amenities) {
		this.amenities = amenities;
	}

	public String getBathrooms() {
		return bathrooms;
	}

	public void setBathrooms(String bathrooms) {
		this.bathrooms = bathrooms;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	public String getPropertyCurrentStatus() {
		return propertyCurrentStatus;
	}

	public void setPropertyCurrentStatus(String propertyCurrentStatus) {
		this.propertyCurrentStatus = propertyCurrentStatus;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
