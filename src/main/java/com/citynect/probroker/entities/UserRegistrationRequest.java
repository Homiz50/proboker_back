package com.citynect.probroker.entities;

public class UserRegistrationRequest {

	private String companyName;
	private String name;
	private String number;
	private String email;
	private String address;
	private String password;
	private String ipAddress;
	private DeviceDetails deviceDetails; // Use a structured class for device details
	private String fingerprint;

	public static class DeviceDetails {
		private String browser;
		private String browserVersion;
		private String os;
		private String osVersion;
		private String platform;
		private String screenResolution;
		private String userAgent;

		public String getBrowser() {
			return browser;
		}

		public void setBrowser(String browser) {
			this.browser = browser;
		}

		public String getBrowserVersion() {
			return browserVersion;
		}

		public void setBrowserVersion(String browserVersion) {
			this.browserVersion = browserVersion;
		}

		public String getOs() {
			return os;
		}

		public void setOs(String os) {
			this.os = os;
		}

		public String getOsVersion() {
			return osVersion;
		}

		public void setOsVersion(String osVersion) {
			this.osVersion = osVersion;
		}

		public String getPlatform() {
			return platform;
		}

		public void setPlatform(String platform) {
			this.platform = platform;
		}

		public String getScreenResolution() {
			return screenResolution;
		}

		public void setScreenResolution(String screenResolution) {
			this.screenResolution = screenResolution;
		}

		public String getUserAgent() {
			return userAgent;
		}

		public void setUserAgent(String userAgent) {
			this.userAgent = userAgent;
		}
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
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

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public DeviceDetails getDeviceDetails() {
		return deviceDetails;
	}

	public void setDeviceDetails(DeviceDetails deviceDetails) {
		this.deviceDetails = deviceDetails;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

}
