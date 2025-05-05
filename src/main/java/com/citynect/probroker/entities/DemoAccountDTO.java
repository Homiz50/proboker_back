package com.citynect.probroker.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemoAccountDTO {

	private String companyName;
	private String name;
	private String number;
	private String email;
	private String address;
	private String password;
	private String activatedBy;
	private String repeatDemo;
	private int activeDays;

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

	public String getActivatedBy() {
		return activatedBy;
	}

	public void setActivatedBy(String activatedBy) {
		this.activatedBy = activatedBy;
	}

	public String getRepeatDemo() {
		return repeatDemo;
	}

	public void setRepeatDemo(String repeatDemo) {
		this.repeatDemo = repeatDemo;
	}

	public int getActiveDays() {
		return activeDays;
	}

	public void setActiveDays(int activeDays) {
		this.activeDays = activeDays;
	}
}
