package com.citynect.probroker.entities;

public class PaidAccountsDTO {

	private String companyName;
	private String address;
	private String password;
	private String email;
	private String orderId;
	private String name;
	private String number;
	private String agentName;
	private String transferTO;
	private String amount;
	private Integer durationInMonth;
	private String paymentMode;
	private Boolean settlementStatus; // Indicates whether the question is saved
	private String adminId;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
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

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public Integer getDurationInMonth() {
		return durationInMonth;
	}

	public void setDurationInMonth(Integer durationInMonth) {
		this.durationInMonth = durationInMonth;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public Boolean getSettlementStatus() {
		return settlementStatus;
	}

	public void setSettlementStatus(Boolean settlementStatus) {
		this.settlementStatus = settlementStatus;
	}

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
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

	public String getTransferTO() {
		return transferTO;
	}

	public void setTransferTO(String transferTO) {
		this.transferTO = transferTO;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
