package com.citynect.probroker.entities;

import java.time.LocalDateTime;
import java.util.List;

public class PropertyFilter {

	private String userId;
	private String adminId;
	private String type;
	private List<String> furnishedTypes;
	private List<String> areas;
	private List<String> bhks;
	private String search;
	private List<String> subType;
	private Integer minsqFt;
	private Integer maxsqFt;
	private String status;
	private String listedBy;
	private int minRent;
	private int maxRent;
	private String listedOn;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getFurnishedTypes() {
		return furnishedTypes;
	}

	public void setFurnishedTypes(List<String> furnishedTypes) {
		this.furnishedTypes = furnishedTypes;
	}

	public List<String> getAreas() {
		return areas;
	}

	public void setAreas(List<String> areas) {
		this.areas = areas;
	}

	public List<String> getBhks() {
		return bhks;
	}

	public void setBhks(List<String> bhks) {
		this.bhks = bhks;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public List<String> getSubType() {
		return subType;
	}

	public void setSubType(List<String> subType) {
		this.subType = subType;
	}

	public String getStatus() {
		return status;
	}

	public Integer getMinsqFt() {
		return minsqFt;
	}

	public void setMinsqFt(Integer minsqFt) {
		this.minsqFt = minsqFt;
	}

	public Integer getMaxsqFt() {
		return maxsqFt;
	}

	public void setMaxsqFt(Integer maxsqFt) {
		this.maxsqFt = maxsqFt;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getListedBy() {
		return listedBy;
	}

	public void setListedBy(String listedBy) {
		this.listedBy = listedBy;
	}

	public int getMinRent() {
		return minRent;
	}

	public void setMinRent(int minRent) {
		this.minRent = minRent;
	}

	public int getMaxRent() {
		return maxRent;
	}

	public void setMaxRent(int maxRent) {
		this.maxRent = maxRent;
	}

	public String getListedOn() {
		return listedOn;
	}

	public void setListedOn(String listedOn) {
		this.listedOn = listedOn;
	}

}
