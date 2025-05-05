package com.citynect.probroker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.citynect.probroker.dao.AdminRemarkForUserRepository;
import com.citynect.probroker.dao.AdminRepository;
import com.citynect.probroker.dao.DemoAccountsRepository;
import com.citynect.probroker.dao.PaidAccountsRepository;
import com.citynect.probroker.dao.PropertyDetailsRepository;
import com.citynect.probroker.dao.PropertyEditHistoryRepository;
import com.citynect.probroker.dao.UserPropertyStatusRepository;
import com.citynect.probroker.dao.UserRepository;
import com.citynect.probroker.entities.Admin;
import com.citynect.probroker.entities.AdminRemarkForUser;
import com.citynect.probroker.entities.DemoAccounts;
import com.citynect.probroker.entities.PaidAccounts;
import com.citynect.probroker.entities.PropertyDetails;
import com.citynect.probroker.entities.PropertyEditHistory;
import com.citynect.probroker.entities.PropertyFilter;
import com.citynect.probroker.entities.User;
import com.citynect.probroker.entities.UserPropertyRemark;
import com.citynect.probroker.entities.UserPropertyStatus;

@Service
public class AdminService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserPropertyStatusRepository userPropertyStatusRepository;

	@Autowired
	private PropertyEditHistoryRepository propertyEditHistoryRepository;

	@Autowired
	private AdminRemarkForUserRepository adminRemarkForUserRepository;

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private PaidAccountsRepository paidAccountsRepository;

	@Autowired
	private DemoAccountsRepository demoAccountsRepository;

	@Autowired
	private PropertyDetailsRepository propertyDetailsRepository;

	public Admin loginAdmin(String number, String password) {
		try {

			Admin admin = adminRepository.findByNumber(number)
					.orElseThrow(() -> new IllegalArgumentException("Admin with this number is not registered"));
			if (!BCrypt.checkpw(password, admin.getPassword())) {
				throw new IllegalArgumentException("Incorrect password");
			}
			return admin; // User exists and password matches, return the user
		} catch (IllegalArgumentException e) {
			// Log specific user error
			throw e;
		} catch (Exception ex) {
			// Log the error
			throw new RuntimeException("Failed to authenticate user: " + ex.getMessage(), ex);
		}
	}

	// Method to get all demo accounts sorted by newest first
	public List<DemoAccounts> getAllDemoAccountsSortedByNewest() {
		// Fetch demo accounts with "Pending" payment status
		List<DemoAccounts> demoAccounts = demoAccountsRepository.findByPaymentStatusOrderByCreatedOnDesc("Pending");

		// Fetch all relevant remarks for the user IDs in the demo accounts
		List<String> userIds = demoAccounts.stream().map(DemoAccounts::getUserId).filter(Objects::nonNull).distinct()
				.collect(Collectors.toList());

		// Fetch remarks and follow-up dates for these user IDs
		List<AdminRemarkForUser> remarks = adminRemarkForUserRepository.findByUserIdIn(userIds);

		// Map remarks and follow-up dates by userId for quick lookup
		Map<String, AdminRemarkForUser> userRemarksMap = remarks.stream()
				.collect(Collectors.toMap(AdminRemarkForUser::getUserId, remark -> remark, // Map the entire entity
						(r1, r2) -> r1)); // Handle duplicates by keeping the first one

		// Set remarks and follow-up dates in the demo accounts
		demoAccounts.forEach(account -> {
			AdminRemarkForUser remarkDetails = userRemarksMap.get(account.getUserId());
			if (remarkDetails != null) {
				account.setRemark(remarkDetails.getRemark()); // Assuming DemoAccounts has a `remark` field
				account.setFollowupDate(remarkDetails.getFollowupDate()); // Assuming DemoAccounts has a `followUpDate`
																			// field
			} else {
				account.setRemark(""); // Default to empty if no remark found
				account.setFollowupDate(null); // Default to null if no follow-up date found
			}
		});

		return demoAccounts;
	}

	public Admin registerAdmin(Admin loginRequest) {
		try {
			Optional<Admin> existingUser = adminRepository.findByNumber(loginRequest.getNumber());

			if (existingUser.isPresent()) {
				throw new UserAlreadyExistsException("User with this number already exists");
			}

			String hashedPassword = BCrypt.hashpw(loginRequest.getPassword(), BCrypt.gensalt());
			Admin newUser = new Admin();
			newUser.setName(loginRequest.getName().trim());
			newUser.setNumber(loginRequest.getNumber().trim());
			newUser.setPassword(hashedPassword);
			newUser.setCreatedOn(LocalDateTime.now());

			return adminRepository.save(newUser);

		} catch (UserAlreadyExistsException ex) {
			throw ex; // Re-throwing the specific exception
		} catch (IllegalArgumentException ex) {
			throw ex; // Re-throwing any validation errors
		} catch (Exception ex) {
			// Log the error
			throw new RuntimeException("Failed to register user: " + ex.getMessage(), ex);
		}
	}

//	public List<PaidAccounts> getAllPaidAccountsSortedByNewest() {
//		// Fetch and return all demo accounts sorted by createdOn (newest first)
//		return paidAccountsRepository.findAllByOrderByCreatedOnDesc();
//	}

	public List<PaidAccounts> getAllPaidAccountsSortedByNewest() {
		// Fetch all paid accounts sorted by `createdOn` in descending order
		List<PaidAccounts> paidAccounts = paidAccountsRepository.findAllByOrderByCreatedOnDesc();

		// Collect all unique `userId`s from the paid accounts
		List<String> userIds = paidAccounts.stream().map(PaidAccounts::getUserId).filter(Objects::nonNull).distinct()
				.collect(Collectors.toList());

		// Fetch all remarks for these user IDs
		List<AdminRemarkForUser> remarks = adminRemarkForUserRepository.findByUserIdIn(userIds);

		// Map remarks by userId for quick lookup
		Map<String, String> userRemarksMap = remarks.stream().collect(
				Collectors.toMap(AdminRemarkForUser::getUserId, AdminRemarkForUser::getRemark, (r1, r2) -> r1));

		// Attach remarks to the paid accounts
		paidAccounts.forEach(account -> {
			String remark = userRemarksMap.getOrDefault(account.getUserId(), "");
			account.setRemark(remark); // Assuming PaidAccounts has a `remark` field
		});

		return paidAccounts;
	}

//	public List<User> getAllUsers() {
//		// Fetch and return all demo accounts sorted by createdOn (newest first)
//		return userRepository.findAllByOrderByCreatedOnDesc();
//	}

	public List<User> getAllUsers() {
		// Fetch all users sorted by `createdOn` in descending order
		List<User> users = userRepository.findAllByOrderByCreatedOnDesc();

		// Collect all unique user IDs from the users
		List<String> userIds = users.stream().map(User::getId).filter(Objects::nonNull).distinct()
				.collect(Collectors.toList());

		// Fetch all remarks and followUpDates for these user IDs
		List<AdminRemarkForUser> remarks = adminRemarkForUserRepository.findByUserIdIn(userIds);

		// Map remarks and followUpDates by userId for quick lookup
		Map<String, AdminRemarkForUser> userRemarksMap = remarks.stream()
				.collect(Collectors.toMap(AdminRemarkForUser::getUserId, remark -> remark, // Map the entire entity
						(r1, r2) -> r1)); // Handle duplicates by keeping the first one

		// Attach remarks and followUpDates to the users
		users.forEach(user -> {
			AdminRemarkForUser remarkDetails = userRemarksMap.get(user.getId());
			if (remarkDetails != null) {
				user.setRemark(remarkDetails.getRemark()); // Assuming User entity has a `remark` field
				user.setFollowupDate(remarkDetails.getFollowupDate()); // Assuming User entity has a `followUpDate`
																		// field
			} else {
				user.setRemark(""); // Default to empty if no remark found
				user.setFollowupDate(null); // Default to null if no follow-up date found
			}
		});

		return users;
	}

	public Admin getAdminById(String adminId) {
		return adminRepository.findById(adminId).orElse(null);
	}

	public void updateReportStatus(String reportId, String adminId, boolean action) {
		// Fetch UserPropertyStatus by reportId
		UserPropertyStatus userPropertyStatus = userPropertyStatusRepository.findById(reportId)
				.orElseThrow(() -> new RuntimeException("UserPropertyStatus with reportId not found"));

		// Fetch Admin by adminId
		Admin admin = adminRepository.findById(adminId)
				.orElseThrow(() -> new RuntimeException("Admin with adminId not found"));

		if (action) {
			// Update PropertyDetails
			PropertyDetails propertyDetails = propertyDetailsRepository.findById(userPropertyStatus.getPropId())
					.orElseThrow(() -> new RuntimeException("PropertyDetails with propId not found"));
			propertyDetails.setIsDeleted(1);
			propertyDetails.setStatus(userPropertyStatus.getStatus());
			propertyDetailsRepository.save(propertyDetails);

			// Update UserPropertyStatus with adminId and reviewedDate
			userPropertyStatus.setAdminId(adminId);
			userPropertyStatus.setIsVerified(1);
			userPropertyStatus.setReviewedDate(LocalDateTime.now()); // Ensure `reviewedDate` field is present in
																		// UserPropertyStatus
			userPropertyStatusRepository.save(userPropertyStatus);
		} else {
			// Only update UserPropertyStatus if action is false
			userPropertyStatus.setAdminId(adminId);
			userPropertyStatus.setIsVerified(1);
			userPropertyStatus.setReviewedDate(LocalDateTime.now());
			userPropertyStatusRepository.save(userPropertyStatus);
		}

		// Record action in Admin entity's history
		if (admin.getReportedPropertyAction() == null) {
			admin.setReportedPropertyAction(new ArrayList<>()); // Initialize if null
		}

		// Record action in Admin entity's history
		Admin.ReportedPropertyAction actionRecord = new Admin.ReportedPropertyAction();
		actionRecord.setReportId(reportId);
		actionRecord.setActionType(action ? "Deleted" : "Not Deleted");
		actionRecord.setReviewedDate(LocalDateTime.now());

		admin.getReportedPropertyAction().add(actionRecord);
		adminRepository.save(admin);
	}

	public Page<PropertyDetails> filterPropertiesSharingFlat(PropertyFilter filterRequest, int page, int size) {
		try {
			Admin admin = null;
			if (filterRequest.getAdminId() != null && !filterRequest.getAdminId().isEmpty()) {
				admin = adminRepository.findById(filterRequest.getAdminId()).orElse(null);
				if (admin == null) {
					System.out.print("this method is running");
					return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
				}
			}

			// Initialize criteria
			Criteria criteria = new Criteria();

			// Handle status-based filtering
			if (filterRequest.getStatus() != null) {
				switch (filterRequest.getStatus().toLowerCase()) {
				case "active":
					criteria = criteria.and("isDeleted").is(0);
					break;
				case "deleted":
					criteria = criteria.and("isDeleted").is(1);
					break;
				}
			}

			// Handle 'listedOn' date-based filtering
			if (filterRequest.getListedOn() != null && !filterRequest.getListedOn().isEmpty()) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				LocalDate listedOnDate = LocalDate.parse(filterRequest.getListedOn(), formatter);

				LocalDateTime startOfDay = listedOnDate.atStartOfDay();
				LocalDateTime endOfDay = listedOnDate.atTime(LocalTime.MAX);

				criteria = criteria.and("listedDate").gte(startOfDay).lte(endOfDay);
			}

			// Handle type-based filtering
			if (filterRequest.getType() != null && !filterRequest.getType().isEmpty()) {
				criteria = criteria.and("type").regex("^" + Pattern.quote(filterRequest.getType()), "i");
			}

			if (filterRequest.getSearch() != null && !filterRequest.getSearch().isEmpty()) {
				String searchPattern = ".*" + filterRequest.getSearch().replace(" ", ".*") + ".*";

				criteria = criteria.orOperator(Criteria.where("title").regex(searchPattern, "i"),
						Criteria.where("area").regex(searchPattern, "i"),
						Criteria.where("address").regex(searchPattern, "i"),
						Criteria.where("number").regex(searchPattern, "i"));
				;
			}

			if (filterRequest.getListedBy() != null && !filterRequest.getListedBy().isEmpty()) {
				if (filterRequest.getListedBy().equalsIgnoreCase("Agent")) {
					criteria = criteria.and("userType").regex("^Agent$", "i");
				} else if (filterRequest.getListedBy().equalsIgnoreCase("Owner")) {
					criteria = criteria.and("userType").in("Owner", "Builder");
				} else {
					criteria = criteria.and("userType").in("Owner", "Builder");
				}

			}

			// Combine all $or conditions into a list
			List<Criteria> orCriteriaList = new ArrayList<>();

			// Area-based filtering (case-insensitive)
			if (filterRequest.getAreas() != null && !filterRequest.getAreas().isEmpty()) {
				List<Criteria> areaCriteria = filterRequest.getAreas().stream()
						.map(area -> Criteria.where("area").regex("^" + Pattern.quote(area) + "$", "i"))
						.collect(Collectors.toList());
				orCriteriaList.add(new Criteria().orOperator(areaCriteria.toArray(new Criteria[0])));
			}

			// BHK-based filtering (case-insensitive)
			if (filterRequest.getBhks() != null && !filterRequest.getBhks().isEmpty()) {
				List<Criteria> bhkCriteria = filterRequest.getBhks().stream()
						.map(bhk -> Criteria.where("bhk").regex("^" + Pattern.quote(bhk) + "$", "i"))
						.collect(Collectors.toList());
				orCriteriaList.add(new Criteria().orOperator(bhkCriteria.toArray(new Criteria[0])));
			}

			// Subtype-based filtering (case-insensitive)
			if (filterRequest.getSubType() != null && !filterRequest.getSubType().isEmpty()) {
				List<Criteria> subtypeCriteria = filterRequest.getSubType().stream()
						.map(subtype -> Criteria.where("unitType").regex("^" + Pattern.quote(subtype) + "$", "i"))
						.collect(Collectors.toList());
				orCriteriaList.add(new Criteria().orOperator(subtypeCriteria.toArray(new Criteria[0])));
			}

			if (filterRequest.getFurnishedTypes() != null && !filterRequest.getFurnishedTypes().isEmpty()) {
				List<Criteria> furnishedTypeCriteria = filterRequest.getFurnishedTypes().stream()
						.map(furnishedType -> Criteria.where("furnishedType")
								.regex("^" + Pattern.quote(furnishedType) + "$", "i"))
						.collect(Collectors.toList());
				orCriteriaList.add(new Criteria().orOperator(furnishedTypeCriteria.toArray(new Criteria[0])));
			}

			// Add the combined $or criteria to the main criteria
			if (!orCriteriaList.isEmpty()) {
				criteria = criteria.andOperator(orCriteriaList.toArray(new Criteria[0]));
			}

			if (filterRequest.getMinsqFt() != null && filterRequest.getMaxsqFt() != null) {
				if (filterRequest.getMinsqFt() != 0 && filterRequest.getMaxsqFt() != 10000) {
					criteria = criteria.and("sqFt").gte(filterRequest.getMinsqFt()).lte(filterRequest.getMaxsqFt());
				} else if (filterRequest.getMinsqFt() != 0) {
					criteria = criteria.and("sqFt").gte(filterRequest.getMinsqFt());
				} else if (filterRequest.getMaxsqFt() != 0) {
					criteria = criteria.and("sqFt").lte(filterRequest.getMaxsqFt());
				}
			}

			// Rent-based filtering
			if (filterRequest.getMinRent() != 0 && filterRequest.getMaxRent() != 0) {
				criteria = criteria.and("rentValue").gte(filterRequest.getMinRent()).lte(filterRequest.getMaxRent());
			} else if (filterRequest.getMinRent() != 0) {
				criteria = criteria.and("rentValue").gte(filterRequest.getMinRent());
			} else if (filterRequest.getMaxRent() != 0) {
				criteria = criteria.and("rentValue").lte(filterRequest.getMaxRent());
			}

			// Create Pageable object
			Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "listedDate"));

			// Create Query object with criteria and pagination
			Query query = Query.query(criteria).with(pageable);

			// Execute the query to get properties
			List<PropertyDetails> properties = mongoTemplate.find(query, PropertyDetails.class);

			// Count total number of items matching the criteria
			Query countQuery = Query.query(criteria);
			long totalItems = mongoTemplate.count(countQuery, PropertyDetails.class);

			return new PageImpl<>(properties, pageable, totalItems);

		} catch (Exception e) {
			System.out.println("An error occurred while fetching properties: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public void removeProperty(String adminId, String propId) {
		// Fetch UserPropertyStatus by reportId

		// Fetch Admin by adminId
		Admin admin = adminRepository.findById(adminId)
				.orElseThrow(() -> new RuntimeException("Admin with adminId not found"));

		// Update PropertyDetails
		PropertyDetails propertyDetails = propertyDetailsRepository.findById(propId)
				.orElseThrow(() -> new RuntimeException("PropertyDetails with propId not found"));
		propertyDetails.setIsDeleted(1);
		propertyDetailsRepository.save(propertyDetails);
	}

	public PaidAccounts updateSettlementStatus(String id, String adminId) {
		// Find the paid account by id
		PaidAccounts paidAccount = paidAccountsRepository.findById(id)
				.orElseThrow(() -> new IllegalStateException("Paid account not found with id: " + id));

		// Validate if the admin is authorized (if necessary)
//		if (!isAdminAuthorized(adminId)) {
//			throw new IllegalStateException("Admin is not authorized to update settlement status.");
//		}

		// Update settlement status
		paidAccount.setSettlementStatus(true); // Set it to true or some other logic

		// Track who updated the record and when
		paidAccount.setUpdatedBy(adminId); // Store the admin who updated
		paidAccount.setUpdatedOn(LocalDate.now()); // Store the timestamp of the update

		// Save the updated PaidAccount entity
		return paidAccountsRepository.save(paidAccount);
	}

	public void setCallAllowedToFalseForAllProperties() {
		Query query = new Query(); // Match all documents
		Update update = new Update().set("paymentStatus", "Pending");
		mongoTemplate.updateMulti(query, update, DemoAccounts.class);
	}

	public AdminRemarkForUser addOrUpdateRemark(String adminId, String userId, String remark, String followupDate) {
		Optional<AdminRemarkForUser> existingRemark = adminRemarkForUserRepository.findByUserId(userId);

		if (existingRemark.isPresent()) {
			// Update existing remark
			AdminRemarkForUser adminRemarkForUser = existingRemark.get();
			adminRemarkForUser.setRemark(remark); // Update remark text
			adminRemarkForUser.setFollowupDate(followupDate);
			return adminRemarkForUserRepository.save(adminRemarkForUser);
		} else {
			// Create a new remark
			AdminRemarkForUser adminRemarkForUser = new AdminRemarkForUser();
			adminRemarkForUser.setAdminId(adminId);
			adminRemarkForUser.setUserId(userId);
			adminRemarkForUser.setRemark(remark);
			adminRemarkForUser.setFollowupDate(followupDate);
			adminRemarkForUser.setCreatedOn(LocalDateTime.now());
			return adminRemarkForUserRepository.save(adminRemarkForUser);
		}
	}

	public PropertyDetails editPropertyDetails(PropertyDetails updatedDetails, String adminId) {
		try {

			// Fetch the existing property details
			PropertyDetails existingProperty = propertyDetailsRepository.findById(updatedDetails.getId()).orElseThrow(
					() -> new IllegalArgumentException("Property not found with ID: " + updatedDetails.getId()));

			// Track changes
			Map<String, String> changes = new HashMap<>();

			// Update the fields and track changes
			if (!existingProperty.getTitle().equals(updatedDetails.getTitle())) {
				changes.put("Title", updatedDetails.getTitle());
				existingProperty.setTitle(updatedDetails.getTitle());
			}
			if (!existingProperty.getListedDate().equals(updatedDetails.getListedDate())) {
				changes.put("listedDate", updatedDetails.getListedDate().toString());
				existingProperty.setListedDate(updatedDetails.getListedDate());
			}
			if (!existingProperty.getType().equals(updatedDetails.getType())) {
				changes.put("type", updatedDetails.getType());
				existingProperty.setType(updatedDetails.getType());
			}
			if (!existingProperty.getRent().equals(updatedDetails.getRent())) {
				changes.put("Rent", updatedDetails.getRent());
				existingProperty.setRent(updatedDetails.getRent());
			}
			if (existingProperty.getRentValue() != updatedDetails.getRentValue()) {
				changes.put("rentValue", String.valueOf(updatedDetails.getRentValue())); // Convert int to String for
																							// map
				existingProperty.setRentValue(updatedDetails.getRentValue());
			}
			if (!existingProperty.getBhk().equals(updatedDetails.getBhk())) {
				changes.put("BHK", updatedDetails.getBhk());
				existingProperty.setBhk(updatedDetails.getBhk());
			}
			if (!existingProperty.getFurnishedType().equals(updatedDetails.getFurnishedType())) {
				changes.put("Furnished Type", updatedDetails.getFurnishedType());
				existingProperty.setFurnishedType(updatedDetails.getFurnishedType());
			}
			if (!existingProperty.getSquareFt().equals(updatedDetails.getSquareFt())) {
				changes.put("squareFt", updatedDetails.getSquareFt());
				existingProperty.setSquareFt(updatedDetails.getSquareFt());
			}
			if (existingProperty.getSqFt() != updatedDetails.getSqFt()) {
				changes.put("sqFt Value", String.valueOf(updatedDetails.getSqFt())); // Convert int to String for map
				existingProperty.setSqFt(updatedDetails.getSqFt());
			}
			if (!existingProperty.getAddress().equals(updatedDetails.getAddress())) {
				changes.put("address", updatedDetails.getAddress());
				existingProperty.setAddress(updatedDetails.getAddress());
			}
			if (!existingProperty.getArea().equals(updatedDetails.getArea())) {
				changes.put("area", updatedDetails.getArea());
				existingProperty.setArea(updatedDetails.getArea());
			}
			if (!existingProperty.getCity().equals(updatedDetails.getCity())) {
				changes.put("city", updatedDetails.getCity());
				existingProperty.setCity(updatedDetails.getCity());
			}
			if (!existingProperty.getDescription().equals(updatedDetails.getDescription())) {
				changes.put("description", updatedDetails.getDescription());
				existingProperty.setDescription(updatedDetails.getDescription());
			}
			if (!existingProperty.getUnitType().equals(updatedDetails.getUnitType())) {
				changes.put("unitType", updatedDetails.getUnitType());
				existingProperty.setUnitType(updatedDetails.getUnitType());
			}
			if (!existingProperty.getName().equals(updatedDetails.getName())) {
				changes.put("name", updatedDetails.getName());
				existingProperty.setName(updatedDetails.getName());
			}
			if (!existingProperty.getNumber().equals(updatedDetails.getNumber())) {
				changes.put("number", updatedDetails.getNumber());
				existingProperty.setNumber(updatedDetails.getNumber());
			}

			// Save the updated property details
			propertyDetailsRepository.save(existingProperty);

			// Log the edit in the history
			PropertyEditHistory history = new PropertyEditHistory();
			history.setPropertyId(updatedDetails.getId());
			history.setAdminId(adminId);
			history.setEditedOn(LocalDateTime.now());
			history.setChanges(changes.toString()); // Convert changes map to a string
			propertyEditHistoryRepository.save(history);

			return existingProperty;
		} catch (IllegalArgumentException e) {
			// Rethrow to handle it in the controller
			throw e;
		} catch (Exception e) {
			// Log unexpected errors
			System.err.println("Error editing property: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to edit property: " + e.getMessage());
		}
	}

	public List<PropertyDetails> getDuplicatePropertyDetails() {
		return propertyDetailsRepository.findByIsDeleted(2);
	}
}
