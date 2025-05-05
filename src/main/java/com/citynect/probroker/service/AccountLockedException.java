package com.citynect.probroker.service;

public class AccountLockedException extends RuntimeException {
	public AccountLockedException(String message) {
		super(message);
	}
}