package com.citynect.probroker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.citynect.probroker.dao.DemoAccountsRepository;
import com.citynect.probroker.dao.PaidAccountsRepository;
import com.citynect.probroker.dao.PropertyDetailsRepository;
import com.citynect.probroker.dao.UpdatePasswordRequestRepository;
import com.citynect.probroker.dao.UserRepository;
import com.citynect.probroker.entities.DemoAccountDTO;
import com.citynect.probroker.entities.DemoAccounts;
import com.citynect.probroker.entities.PaidAccounts;
import com.citynect.probroker.entities.PaidAccountsDTO;
import com.citynect.probroker.entities.PropertyDetails;
import com.citynect.probroker.entities.UpdatePasswordRequest;
import com.citynect.probroker.entities.User;
import com.citynect.probroker.entities.UserRegistrationRequest;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PaidAccountsRepository paidAccountsRepository;

	@Autowired
	private UpdatePasswordRequestRepository updatePasswordRequestRepository;

	@Autowired
	private DemoAccountsRepository demoAccountsRepository;

	@Autowired
	private PropertyDetailsRepository propertyDetailsRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Scheduled(cron = "0 0 22 * * *") // Runs every day at 1:00 AM
	@Transactional
	public void expireDemoAccounts() {
		LocalDate currentDate = LocalDate.now();

		System.out.println("Demo account check API called");
		// Find all DemoAccounts where expiredDate has passed and the status is still
		// active
		List<DemoAccounts> expiredAccounts = demoAccountsRepository.findByExpiredDateBeforeAndStatus(currentDate,
				"Active");

		// Loop through each expired DemoAccount
		for (DemoAccounts demoAccount : expiredAccounts) {
			// Update the status to "Expired"
			demoAccount.setStatus("Expired");
			demoAccountsRepository.save(demoAccount);

			// Find the associated user and update their isPremium status to 0
			User user = userRepository.findById(demoAccount.getUserId()).orElse(null);
			if (user != null && user.getIsPremium() == 1) {
				user.setIsPremium(0); // Mark user as not premium
				userRepository.save(user);
			}
		}
	}

	@Scheduled(cron = "0 0 23 * * *") // Runs every day at midnight
	@Transactional
	public void expirePaidAccounts() {
		LocalDate currentDate = LocalDate.now();

		System.out.println("Premium account check API called");

		// Fetch all PaidAccounts where expiredDate has passed and status is "Active"
		List<PaidAccounts> expiredAccounts = paidAccountsRepository.findByExpiredDateBeforeAndStatus(currentDate,
				"Active");

		// Loop through each expired PaidAccount
		for (PaidAccounts paidAccount : expiredAccounts) {
			// Update PaidAccount status to "Expired"
			paidAccount.setStatus("Expired");
			paidAccountsRepository.save(paidAccount);

			// Find the associated user and update their isPremium status to 0
			User user = userRepository.findById(paidAccount.getUserId()).orElse(null);
			if (user != null && user.getIsPremium() == 1) {
				user.setIsPremium(0); // Mark user as not premium
				userRepository.save(user);
			}
		}
	}

	@Scheduled(cron = "0 0 21 * * *") // This runs every day at midnight
	public void resetLoginCount() {
		List<User> premiumUsers = userRepository.findByIsPremium(1);
		System.out.println("reset property count method running");

		String specificUserId = "67128ea2d6da233a1af20f30"; // Replace with the actual ID
		for (User user : premiumUsers) {
			if (user.getId().equals(specificUserId)) {
				user.setLimit(25); // Set limit to 25 for the specific user
				user.setWrongPassLimit(10);
			} else {
				user.setLimit(100); // Set limit to 100 for other users
				user.setWrongPassLimit(10);
			}
		}
		userRepository.saveAll(premiumUsers);
	}

	@Scheduled(cron = "0 0 0 * * *") // This runs every day at midnight
	public void resetWrongpassLimit() {
		System.out.println("reset wrong pass limit method running");

		Query query = new Query(); // Matches all User documents
		Update update = new Update().set("wrongPassLimit", 10);

		mongoTemplate.updateMulti(query, update, User.class);
	}

//	public User loginUser(String number, String password) {
//		try {
//			User admin = userRepository.findByNumber(number);
//			if (admin != null && BCrypt.checkpw(password, admin.getPassword())) {
//				return admin; // Existing user with correct password, return the user
//			} else {
//				throw new RuntimeException("Invalid credentials"); // Password mismatch
//			}
//		} catch (Exception ex) {
//			// Log the error
//			throw new RuntimeException("Failed to authenticate admin: " + ex.getMessage(), ex);
//		}
//	}

	public User loginUser(String number, String password) {
		try {

			User user = userRepository.findByNumber(number)
					.orElseThrow(() -> new IllegalArgumentException("User with this number is not registered"));

			if (user.getWrongPassLimit() <= 0) {
				throw new AccountLockedException("Account is locked. Please try again later.");
			}

			if (user != null && BCrypt.checkpw(password, user.getPassword())) {
				return user; // Existing user with correct password, return the user
			} else {
				user.setWrongPassLimit(user.getWrongPassLimit() - 1);
				userRepository.save(user);
				throw new IllegalArgumentException("Incorrect password");

			}

		} catch (IllegalArgumentException | AccountLockedException e) {
			// Log specific user error
			throw e;
		} catch (Exception ex) {
			// Log the error
			throw new RuntimeException("Failed to authenticate user: " + ex.getMessage(), ex);
		}
	}

//	public User registerUser(User loginRequest) {
//		try {
//			User admin = userRepository.findByNumber(loginRequest.getNumber());
//
//			if (admin != null) {
//				throw new IllegalArgumentException("User with this number already exists");
//			}
//
//			String hashedPassword = BCrypt.hashpw(loginRequest.getPassword(), BCrypt.gensalt());
//			User newUser = new User();
//			newUser.setName(loginRequest.getName());
//			newUser.setNumber(loginRequest.getNumber());
//			newUser.setPassword(hashedPassword);
//			newUser.setIsPremium(0);
//			newUser.setCreatedOn(LocalDateTime.now());
//
//			User savedUser = userRepository.save(newUser);
//			return savedUser; // New user created and saved
//
//		} catch (Exception ex) {
//			// Log the error
//			throw new RuntimeException("Failed to authenticate admin: " + ex.getMessage(), ex);
//		}
//	}

	public User registerUser(User loginRequest) {
		try {
			Optional<User> existingUser = userRepository.findByNumber(loginRequest.getNumber());

			if (existingUser.isPresent()) {
				throw new UserAlreadyExistsException("User with this number already exists");
			}

			String hashedPassword = BCrypt.hashpw(loginRequest.getPassword().trim(), BCrypt.gensalt());
			User newUser = new User();
			newUser.setCompanyName(loginRequest.getCompanyName().trim());
			newUser.setName(loginRequest.getName().trim());
			newUser.setNumber(loginRequest.getNumber().trim());
			newUser.setEmail(loginRequest.getEmail().trim());
			newUser.setAddress(loginRequest.getAddress().trim());
			newUser.setPassword(hashedPassword);
			newUser.setIsPremium(0);
			newUser.setLimit(0);
			newUser.setCreatedOn(LocalDateTime.now());

			return userRepository.save(newUser);

		} catch (UserAlreadyExistsException ex) {
			throw ex; // Re-throwing the specific exception
		} catch (IllegalArgumentException ex) {
			throw ex; // Re-throwing any validation errors
		} catch (Exception ex) {
			// Log the error
			throw new RuntimeException("Failed to register user: " + ex.getMessage(), ex);
		}
	}

	public String savePropertyToUser(String userId, String propId) {
		if (userId == null || userId.trim().isEmpty() || propId == null || propId.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid user ID or property ID!");
		}
		Optional<User> userOptional = userRepository.findById(userId);

		if (userOptional.isPresent()) {
			User user = userOptional.get();

			if (user.getSavedPropertyIds() == null) {
				user.setSavedPropertyIds(new ArrayList<>());
			}

			// Check if propertyId is already saved
			if (user.getSavedPropertyIds().contains(propId)) {
				user.getSavedPropertyIds().remove(propId);
				userRepository.save(user);
				return "Property removed from saved list successfully.";
			} else {
				user.getSavedPropertyIds().add(propId);
				userRepository.save(user);
				return "Property added to saved list successfully.";
			}
		} else {
			throw new RuntimeException("User not found!");
		}
	}

	public User registerUser(UserRegistrationRequest loginRequest) {
		try {
			Optional<User> existingUser = userRepository.findByNumber(loginRequest.getNumber());

			if (existingUser.isPresent()) {
				throw new UserAlreadyExistsException("User with this number already exists");
			}

			String hashedPassword = BCrypt.hashpw(loginRequest.getPassword().trim(), BCrypt.gensalt());
			User newUser = new User();
			newUser.setCompanyName(loginRequest.getCompanyName().trim());
			newUser.setName(loginRequest.getName().trim());
			newUser.setNumber(loginRequest.getNumber().trim());
			newUser.setEmail(loginRequest.getEmail().trim());
			newUser.setAddress(loginRequest.getAddress().trim());
			newUser.setPassword(hashedPassword);
			newUser.setIsPremium(0);
			newUser.setLimit(0);
			newUser.setWrongPassLimit(10);
			newUser.setCreatedOn(LocalDateTime.now());

			return userRepository.save(newUser);

		} catch (UserAlreadyExistsException ex) {
			throw ex; // Re-throwing the specific exception
		} catch (IllegalArgumentException ex) {
			throw ex; // Re-throwing any validation errors
		} catch (Exception ex) {
			// Log the error
			throw new RuntimeException("Failed to register user: " + ex.getMessage(), ex);
		}
	}

	public User getUserById(String userId) {
		return userRepository.findById(userId).orElse(null);
	}

	public int updateSquareFtField() {
		List<PropertyDetails> properties = propertyDetailsRepository.findAll();
		int modifiedCount = 0; // Counter to track modified documents

		for (PropertyDetails property : properties) {
			String squareFtString = property.getSquareFt();
			if (squareFtString != null && !squareFtString.isEmpty()) {
				try {
					// Try converting the string to an integer
					Integer squareFtInt = Integer.parseInt(squareFtString);
					property.setSqFt(squareFtInt); // Update the property entity

					// Save the updated property back to MongoDB
					propertyDetailsRepository.save(property);
					modifiedCount++; // Increment the counter when a document is updated
				} catch (NumberFormatException e) {
					// Handle conversion error
					System.err.println("Failed to convert squareFt for property ID: " + property.getId());
				}
			}
		}

		// Return the number of modified documents
		return modifiedCount;
	}

	@Transactional
	public String registerOrActivateDemoAccount(DemoAccountDTO demoAccountDTO) {
		try {

			// Check if the user already exists by their phone number
			Optional<User> existingUserOptional = userRepository.findByNumber(demoAccountDTO.getNumber());
			User user;

			if (existingUserOptional.isPresent()) {
				// User exists, update the isPremium flag
				user = existingUserOptional.get();
				user.setIsPremium(1); // Mark the user as premium
				user.setLimit(50);
				user.setWrongPassLimit(10);
				userRepository.save(user);
			} else {
				// User does not exist, create a new user
				user = new User();
				user.setCompanyName(demoAccountDTO.getCompanyName());
				user.setName(demoAccountDTO.getName());
				user.setNumber(demoAccountDTO.getNumber());
				user.setEmail(demoAccountDTO.getEmail());
				user.setAddress(demoAccountDTO.getAddress());
				String hashedPassword = BCrypt.hashpw(demoAccountDTO.getPassword().trim(), BCrypt.gensalt());
				user.setLimit(50);
//				String hashedPassword = BCrypt.hashpw(demoAccountDTO.getPassword(), BCrypt.gensalt());
				user.setPassword(hashedPassword); // Remember to handle password encryption
				user.setIsPremium(1); // Set the user as premium
				user.setWrongPassLimit(10);
				user.setCreatedOn(LocalDateTime.now());
				userRepository.save(user);
			}

			// Now create a new DemoAccount for the user
			DemoAccounts demoAccount = new DemoAccounts();
			demoAccount.setUserId(user.getId()); // Set the user ID from the User entity
			demoAccount.setName(demoAccountDTO.getName());
			demoAccount.setNumber(demoAccountDTO.getNumber());
			demoAccount.setActivatedBy(demoAccountDTO.getActivatedBy());
			demoAccount.setActiveDays(demoAccountDTO.getActiveDays());
			demoAccount.setStatus("Active");
			demoAccount.setPaymentStatus("Pending");
			demoAccount.setCreatedOn(LocalDate.now());

			// Calculate the expired date by adding activeDays to the current date
			LocalDate expiredDate = LocalDate.now().plusDays(demoAccountDTO.getActiveDays());
			demoAccount.setExpiredDate(expiredDate);

			// Save the demo account to the repository
			demoAccountsRepository.save(demoAccount);

			return "User " + user.getName() + " is now active as a demo account.";
		} catch (Exception e) {
			// Log the error (if you have a logger) or print the stack trace
			System.err.println("Error registering or activating demo account: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to register or activate demo account: " + e.getMessage());
		}
	}

	@Transactional
	public String registerOrActivateDemoAccountV2(DemoAccountDTO demoAccountDTO) {
		try {
			String repeatDemo = demoAccountDTO.getRepeatDemo(); // Check if it's "Repeat Demo" or "No Repeat Demo"
			String number = demoAccountDTO.getNumber();

			List<DemoAccounts> demoAccountsList = demoAccountsRepository.findByNumber(demoAccountDTO.getNumber());

			if ("No Repeat Demo".equalsIgnoreCase(repeatDemo)) {
				if (!demoAccountsList.isEmpty()) {
					throw new IllegalStateException("Demo account already exists for this number.");
				}
			}

			Optional<User> existingUserOptional = userRepository.findByNumber(number);
			User user;

			if (existingUserOptional.isPresent()) {
				user = existingUserOptional.get();
				user.setIsPremium(1); // Mark the user as premium
				user.setLimit(50);
				user.setWrongPassLimit(10);
				String hashedPassword = BCrypt.hashpw(demoAccountDTO.getPassword().trim(), BCrypt.gensalt());
				user.setPassword(hashedPassword);
				userRepository.save(user);
			} else {
				// If user doesn't exist, create a new user
				user = new User();
				user.setCompanyName(demoAccountDTO.getCompanyName());
				user.setName(demoAccountDTO.getName());
				user.setNumber(number);
				user.setEmail(demoAccountDTO.getEmail());
				user.setAddress(demoAccountDTO.getAddress());
				String hashedPassword = BCrypt.hashpw(demoAccountDTO.getPassword().trim(), BCrypt.gensalt());
				user.setPassword(hashedPassword);
				user.setLimit(100);
				user.setIsPremium(1); // Set the user as premium
				user.setWrongPassLimit(10);
				user.setCreatedOn(LocalDateTime.now());
				userRepository.save(user);
			}

			DemoAccounts demoAccount = new DemoAccounts();

			// Update or create a demo account
			demoAccount.setUserId(user.getId());
			demoAccount.setName(demoAccountDTO.getName());
			demoAccount.setNumber(number);
			demoAccount.setActivatedBy(demoAccountDTO.getActivatedBy());
			demoAccount.setActiveDays(demoAccountDTO.getActiveDays());
			demoAccount.setStatus("Active");
			demoAccount.setPaymentStatus("Pending");
			demoAccount.setCreatedOn(LocalDate.now());

			// Calculate expiration date
			LocalDate expiredDate = LocalDate.now().plusDays(demoAccountDTO.getActiveDays());
			demoAccount.setExpiredDate(expiredDate);

			demoAccountsRepository.save(demoAccount);

			return "User " + user.getName() + " is now active as a demo account.";
		} catch (Exception e) {
			// Log the error and rethrow it
			System.err.println("Error registering or activating demo account: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	@Transactional
	public String ActivatePremium(PaidAccountsDTO paidAccountsDTO) {
		try {

			Optional<User> existingUserOptional = userRepository.findByNumber(paidAccountsDTO.getNumber());
			User user;
			String orderId = generateOrderId();

			if (existingUserOptional.isPresent()) {
				// User exists, update the isPremium flag
				user = existingUserOptional.get();
				user.setLimit(50);
				user.setIsPremium(1); // Mark the user as premium
				user.setWrongPassLimit(10);

				User.ActivePlanDetails activePlanDetails = new User.ActivePlanDetails();
				activePlanDetails.setOrderId(orderId);
				activePlanDetails.setAmount(paidAccountsDTO.getAmount());
				activePlanDetails.setPaidOn(LocalDate.now()); // Set the payment date to today

				// Calculate the expired date
				LocalDate expiredDate = LocalDate.now().plusMonths(paidAccountsDTO.getDurationInMonth());
				activePlanDetails.setExpiredOn(expiredDate);

				// Save active plan details in the user
				user.setActivePlanDetails(activePlanDetails);

				userRepository.save(user);
			} else {
				// If the user doesn't exist, throw an exception
				throw new IllegalArgumentException("User with the provided number does not exist.");
			}

			List<DemoAccounts> demoAccountsList = demoAccountsRepository.findByNumber(paidAccountsDTO.getNumber());

			if (demoAccountsList.isEmpty()) {
				// Log the absence of demo accounts but proceed
				System.out.println("No demo accounts found for the provided number: " + paidAccountsDTO.getNumber());
			} else {
				for (DemoAccounts demoAccount : demoAccountsList) {
					demoAccount.setPaymentStatus("Success");
					demoAccount.setStatus("Expired");
					demoAccountsRepository.save(demoAccount);
				}
			}

			PaidAccounts paidAccounts = new PaidAccounts();
			paidAccounts.setUserId(user.getId()); // Set the user ID from the User entity
			paidAccounts.setName(user.getName());
			paidAccounts.setNumber(paidAccountsDTO.getNumber());
			paidAccounts.setAgentName(paidAccountsDTO.getAgentName());
			paidAccounts.setDurationInMonth(paidAccountsDTO.getDurationInMonth());
			paidAccounts.setOrderId(orderId);
			paidAccounts.setAmount(paidAccountsDTO.getAmount());
			paidAccounts.setPaymentMode(paidAccountsDTO.getPaymentMode());
			paidAccounts.setStatus("Active");
			paidAccounts.setSettlementStatus(paidAccountsDTO.getSettlementStatus());
			if (paidAccountsDTO.getSettlementStatus()) {
				paidAccounts.setUpdatedBy(paidAccountsDTO.getAdminId()); // Admin who made the update
				paidAccounts.setUpdatedOn(LocalDate.now()); // Set the current timestamp
			}
			paidAccounts.setCreatedOn(LocalDate.now());

			// Calculate the expired date by adding activeDays to the current date
			LocalDate expiredDate = LocalDate.now().plusMonths(paidAccountsDTO.getDurationInMonth());
			paidAccounts.setExpiredDate(expiredDate);

			// Save the demo account to the repository
			paidAccountsRepository.save(paidAccounts);

			return "User " + user.getName() + " has been upgraded to premium and the paid account is active.";
		} catch (IllegalArgumentException e) {
			// Log specific errors (user not found or invalid arguments)
			throw e; // Re-throw the exception to propagate it to the caller
		} catch (Exception e) {
			// Log and handle any other unexpected errors
			System.err.println("Error activating premium account: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to activate premium account: " + e.getMessage());
		}
	}

	@Transactional
	public String ActivatePremiumV2(PaidAccountsDTO paidAccountsDTO) {
		try {

			Optional<User> existingUserOptional = userRepository.findByNumber(paidAccountsDTO.getNumber());
			User user;
			String orderId = generateOrderId();

			if (existingUserOptional.isPresent()) {
				// User exists, update the isPremium flag
				user = existingUserOptional.get();

			} else {
				user = new User();
				user.setNumber(paidAccountsDTO.getNumber());
				user.setName(paidAccountsDTO.getName()); // Assuming name is part of PaidAccountsDTO
				user.setCompanyName(paidAccountsDTO.getCompanyName());
				user.setEmail(paidAccountsDTO.getEmail()); // Assuming email is part of PaidAccountsDTO
				user.setAddress(paidAccountsDTO.getAddress());
				String hashedPassword = BCrypt.hashpw(paidAccountsDTO.getPassword().trim(), BCrypt.gensalt());
				user.setPassword(hashedPassword);
				user.setCreatedOn(LocalDateTime.now()); // Set creation date
			}

			user.setLimit(50);
			user.setIsPremium(1); // Mark the user as premium
			user.setWrongPassLimit(10);
			User.ActivePlanDetails activePlanDetails = new User.ActivePlanDetails();
			activePlanDetails.setOrderId(orderId);
			activePlanDetails.setAmount(paidAccountsDTO.getAmount());
			activePlanDetails.setPaidOn(LocalDate.now()); // Set the payment date to today

			// Calculate the expired date
			LocalDate expiredDate = LocalDate.now().plusMonths(paidAccountsDTO.getDurationInMonth());
			activePlanDetails.setExpiredOn(expiredDate);

			// Save active plan details in the user
			user.setActivePlanDetails(activePlanDetails);

			userRepository.save(user);

			List<DemoAccounts> demoAccountsList = demoAccountsRepository.findByNumber(paidAccountsDTO.getNumber());

			if (demoAccountsList.isEmpty()) {
				// Log the absence of demo accounts but proceed
				System.out.println("No demo accounts found for the provided number: " + paidAccountsDTO.getNumber());
			} else {
				for (DemoAccounts demoAccount : demoAccountsList) {
					demoAccount.setPaymentStatus("Success");
					demoAccount.setStatus("Expired");
					demoAccountsRepository.save(demoAccount);
				}
			}

			PaidAccounts paidAccounts = new PaidAccounts();
			paidAccounts.setUserId(user.getId()); // Set the user ID from the User entity
			paidAccounts.setName(user.getName());
			paidAccounts.setNumber(paidAccountsDTO.getNumber());
			paidAccounts.setAgentName(paidAccountsDTO.getAgentName());
			paidAccounts.setDurationInMonth(paidAccountsDTO.getDurationInMonth());
			paidAccounts.setOrderId(orderId);
			paidAccounts.setPaidTo(paidAccountsDTO.getTransferTO());
			paidAccounts.setAmount(paidAccountsDTO.getAmount());
			paidAccounts.setPaymentMode(paidAccountsDTO.getPaymentMode());
			paidAccounts.setStatus("Active");
			paidAccounts.setSettlementStatus(paidAccountsDTO.getSettlementStatus());
			if (paidAccountsDTO.getSettlementStatus()) {
				paidAccounts.setUpdatedBy(paidAccountsDTO.getAdminId()); // Admin who made the update
				paidAccounts.setUpdatedOn(LocalDate.now()); // Set the current timestamp
			}
			paidAccounts.setCreatedOn(LocalDate.now());

			// Calculate the expired date by adding activeDays to the current date
			LocalDate expiredDate1 = LocalDate.now().plusMonths(paidAccountsDTO.getDurationInMonth());
			paidAccounts.setExpiredDate(expiredDate1);

			// Save the demo account to the repository
			paidAccountsRepository.save(paidAccounts);

			return "User " + user.getName() + " has been upgraded to premium and the paid account is active.";
		} catch (

		IllegalArgumentException e) {
			// Log specific errors (user not found or invalid arguments)
			throw e; // Re-throw the exception to propagate it to the caller
		} catch (Exception e) {
			// Log and handle any other unexpected errors
			System.err.println("Error activating premium account: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to activate premium account: " + e.getMessage());
		}
	}

	public String generateOrderId() {
		return "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
	}

	public String updatePassword(String number, String newPassword) {
		// Find user by phone number
		Optional<User> userOptional = userRepository.findByNumber(number);

		if (!userOptional.isPresent()) {
			throw new RuntimeException("User with this number not found");
		}

		User user = userOptional.get();
		// Hash the new password using BCrypt
		String hashedPassword = BCrypt.hashpw(newPassword.trim(), BCrypt.gensalt());

		// Update the password in the database
		user.setPassword(hashedPassword);
		userRepository.save(user);

		return "Password updated successfully for user with number: " + number;
	}

	public List<PropertyDetails> exportContactedPropertiesToJson(String userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

		List<String> contactedPropertyIds = user.getContactedPropertyIds();
		if (contactedPropertyIds == null || contactedPropertyIds.isEmpty()) {
			throw new IllegalArgumentException("No contacted properties found for this user");
		}

		// Fetch and return property details based on contacted property IDs
		return propertyDetailsRepository.findAllById(contactedPropertyIds);
	}

	public String updatePassword(UpdatePasswordRequest updatePasswordRequest) {
		String number = updatePasswordRequest.getNumber();

		// Find user by ID
		Optional<User> userOptional = userRepository.findByNumber(number);
		if (!userOptional.isPresent()) {
			throw new RuntimeException("User not found with ID: " + number);
		}

		User user = userOptional.get();

		// Hash the new password using BCrypt
		String hashedPassword = BCrypt.hashpw(updatePasswordRequest.getPassword().trim(), BCrypt.gensalt());

		// Update the user's password
		user.setPassword(hashedPassword);
		user.setWrongPassLimit(10);
		userRepository.save(user);

		// Record the password change in history
		UpdatePasswordRequest passwordChangeHistory = new UpdatePasswordRequest();
		passwordChangeHistory.setUserId(user.getId());
		passwordChangeHistory.setNumber(number);
		passwordChangeHistory.setAdminId(updatePasswordRequest.getAdminId());
		passwordChangeHistory.setReason(updatePasswordRequest.getReason().trim());
		passwordChangeHistory.setCreatedOn(LocalDateTime.now());

		updatePasswordRequestRepository.save(passwordChangeHistory);

		return "Password updated successfully for user with " + number;
	}
}