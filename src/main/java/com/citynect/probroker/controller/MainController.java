package com.citynect.probroker.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.citynect.probroker.dao.ApiLogRepository;
import com.citynect.probroker.dao.PaidAccountsRepository;
import com.citynect.probroker.entities.ApiLog;
import com.citynect.probroker.entities.PaidAccounts;
import com.citynect.probroker.entities.PropertyDetails;
import com.citynect.probroker.entities.PropertyFilter;
import com.citynect.probroker.entities.Suggestion;
import com.citynect.probroker.entities.User;
import com.citynect.probroker.entities.UserPropertyRemark;
import com.citynect.probroker.entities.UserPropertyStatus;
import com.citynect.probroker.entities.UserRegistrationRequest;
import com.citynect.probroker.service.AccountLockedException;
import com.citynect.probroker.service.PropertyService;
import com.citynect.probroker.service.UserAlreadyExistsException;
import com.citynect.probroker.service.UserService;
import com.citynect.probroker.service.UserSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletRequest;

//@CrossOrigin("*")
@RestController
@RequestMapping("/cjidnvij/ceksfbuebijn/user")
public class MainController {

	@Autowired
	private PropertyService propertyService;

	@Autowired
	private ApiLogRepository logRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private PaidAccountsRepository paidAccountsRepository;

	@Autowired
	private UserSessionService userSessionService;

	private void saveApiLog(String method, String Description, String url, Map<String, String> requestParams,
			String requestPayload, String responseData, int statuscode, String errorMessage, String errorCode) {
		ApiLog log = new ApiLog();
		log.setHttpMethod(method);
		log.setRequestUrl(url);
		log.setDescription(Description);
		log.setRequestParams(requestParams);
		log.setRequestPayload(requestPayload);
		log.setResponsePayload(responseData);
		log.setResponseStatus(statuscode);
		log.setErrorMessage(errorMessage);
		log.setErrorCode(errorCode);
		log.setCreatedAt(LocalDateTime.now());
		logRepository.save(log);
	}

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	@GetMapping("/")
	public ResponseEntity<String> healthCheck() {
		return ResponseEntity.ok("this is User API's");
	}

	@PostMapping("/v2/signin/vkjdbfuhe/nkdkjbed")
	public ResponseEntity<Map<String, Object>> register(@RequestBody UserRegistrationRequest registrationRequest)
			throws JsonProcessingException {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String method = request.getMethod();
		String url = request.getRequestURI();
		String requestPayloadJson = objectMapper.writeValueAsString(registrationRequest);
		// Create a response map
		Map<String, Object> response = new HashMap<>();
		try {
			// Register the user
			User newUser = userService.registerUser(registrationRequest);
			newUser.setPassword(null); // Hide password in response
			String responsePayloadJson = objectMapper.writeValueAsString(newUser);

			saveApiLog(method, "register controller log", url, null, requestPayloadJson, responsePayloadJson,
					HttpStatus.OK.value(), null, null);

			// Track user session
			userSessionService.trackUserSession(newUser.getId(), registrationRequest);

			// Prepare response
			response.put("status", "success");
			response.put("message", "User registered successfully");
			response.put("data", newUser);
			return ResponseEntity.ok(response);

		} catch (UserAlreadyExistsException e) {
			response.put("status", "error");
			response.put("message", e.getMessage());
			String responsePayloadJson = objectMapper.writeValueAsString(response);

			saveApiLog(method, "register controller log", url, null, requestPayloadJson, responsePayloadJson,
					HttpStatus.OK.value(), null, null);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

		} catch (IllegalArgumentException e) {
			response.put("status", "error");
			response.put("message", e.getMessage());
			String responsePayloadJson = objectMapper.writeValueAsString(response);

			saveApiLog(method, "register controller log", url, null, requestPayloadJson, responsePayloadJson,
					HttpStatus.OK.value(), null, null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", "An error occurred during registration. Please try again later.");
			String responsePayloadJson = objectMapper.writeValueAsString(response);

			saveApiLog(method, "register controller log", url, null, requestPayloadJson, responsePayloadJson,
					HttpStatus.OK.value(), null, null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/v2/login/dljcnji/cekbjid")
	public ResponseEntity<?> login(@RequestBody UserRegistrationRequest registrationRequest)
			throws JsonProcessingException {
		Map<String, Object> response = new HashMap<>();
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String method = request.getMethod();
		String url = request.getRequestURI();
		String requestPayloadJson = objectMapper.writeValueAsString(registrationRequest);
		try {

			User user = userService.loginUser(registrationRequest.getNumber(), registrationRequest.getPassword());
			user.setPassword(null); // Hide password in response

			userSessionService.trackUserSession(user.getId(), registrationRequest);

			String responsePayloadJson = objectMapper.writeValueAsString(user);

			saveApiLog(method, "login controller log", url, null, requestPayloadJson, responsePayloadJson,
					HttpStatus.OK.value(), null, null);

			response.put("status", "success");
			response.put("message", "Login successful");
			response.put("data", user);

			return ResponseEntity.ok(response);
		} catch (AccountLockedException e) {
			response.put("status", "error");
			response.put("message", e.getMessage());

			String responsePayloadJson = objectMapper.writeValueAsString(response);
			saveApiLog(method, "login controller log", url, null, requestPayloadJson, responsePayloadJson,
					HttpStatus.UNAUTHORIZED.value(), null, null);
			return ResponseEntity.status(HttpStatus.LOCKED).body(response); // Use 423 LOCKED status

		} catch (IllegalArgumentException e) {
			response.put("status", "error");
			response.put("message", e.getMessage());

			String responsePayloadJson = objectMapper.writeValueAsString(response);
			saveApiLog(method, "login controller log", url, null, requestPayloadJson, responsePayloadJson,
					HttpStatus.UNAUTHORIZED.value(), null, null);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", "An error occurred during login.");

			String responsePayloadJson = objectMapper.writeValueAsString(response);
			saveApiLog(method, "login controller log", url, null, requestPayloadJson, responsePayloadJson,
					HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/properties/filter/jkdbxcb/wdjkwbshuvcw/fhwjvshudcknsb")
	public ResponseEntity<?> filterProperties(@RequestBody PropertyFilter filterRequest,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		try {
			if (size > 25) {
				size = 25;
			}

			// Determine property type and filter properties accordingly
			Map<String, Object> response = new HashMap<>();

			if (filterRequest.getUserId() == null || filterRequest.getUserId().isEmpty()) {
				response.put("properties", Collections.emptyList());
				response.put("currentPage", 0);
				response.put("totalItems", 0);
				response.put("totalPages", 0);
				return ResponseEntity.ok(createResponse(true, "", response));

			}
			Page<PropertyDetails> filteredPropertiesPage = propertyService.filterPropertiesSharingFlat(filterRequest,
					page, size);

			response.put("properties", filteredPropertiesPage.getContent());
			response.put("currentPage", filteredPropertiesPage.getNumber());
			response.put("totalItems", filteredPropertiesPage.getTotalElements());
			response.put("totalPages", filteredPropertiesPage.getTotalPages());

			return ResponseEntity.ok(createResponse(true, "", response));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, "An error occurred while fetching properties", new HashMap<>()));

		}
	}

	@PostMapping("/v2/properties/filter/jkdbxcb/wdjkwbshuvcw/fhwjvshudcknsb")
	public ResponseEntity<?> filterPropertiesv2(@RequestBody PropertyFilter filterRequest,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		try {
			if (size > 25) {
				size = 25;
			}

			// Determine property type and filter properties accordingly
			Map<String, Object> response = new HashMap<>();

			if (filterRequest.getUserId() == null || filterRequest.getUserId().isEmpty()) {
				response.put("properties", Collections.emptyList());
				response.put("currentPage", 0);
				response.put("totalItems", 0);
				response.put("totalPages", 0);
				return ResponseEntity.ok(createResponse(true, "", response));

			}
			Page<PropertyDetails> filteredPropertiesPage = propertyService.filterPropertiesSharingFlatV2(filterRequest,
					page, size);

			response.put("properties", filteredPropertiesPage.getContent());
			response.put("currentPage", filteredPropertiesPage.getNumber());
			response.put("totalItems", filteredPropertiesPage.getTotalElements());
			response.put("totalPages", filteredPropertiesPage.getTotalPages());

			return ResponseEntity.ok(createResponse(true, "", response));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, "An error occurred while fetching properties", new HashMap<>()));

		}
	}

	@GetMapping("/counts/fjkbfhwb/fkjbwdiwhbdjwkfjwbj")
	public ResponseEntity<Map<String, Long>> getPropertyCountsByStatusAndType() {
		Map<String, Long> counts = propertyService.getPropertyCountsByStatusAndType();
		return ResponseEntity.ok(counts);
	}

	@PostMapping("/save-property/ijddskjidns/cudhsbcuev")
	public ResponseEntity<?> SaveFlatseeker(@RequestBody(required = false) Map<String, String> requestBody) {
		try {
			String userId = requestBody != null ? requestBody.get("userId") : null;
			String propId = requestBody != null ? requestBody.get("propId") : null;

			String responseMessage = userService.savePropertyToUser(userId, propId);
			return ResponseEntity.ok(createResponse(true, "", responseMessage));

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(createResponse(false, e.getMessage(), new HashMap<>()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, e.getMessage(), new HashMap<>()));
		}
	}

//	@PostMapping("/contacted/kjbuiscc/ckjsbucygwsu")
//	public ResponseEntity<?> ContactProperty(@RequestBody(required = false) Map<String, String> requestBody) {
//		try {
//
//			String userId = requestBody != null ? requestBody.get("userId") : null;
//			String propId = requestBody != null ? requestBody.get("propId") : null;
//
//			PropertyDetails responseMessage = propertyService.contactPropertyToUser(userId, propId);
//
//			Map<String, Object> response = new HashMap<>();
//			response.put("name", responseMessage.getName());
//			response.put("number", responseMessage.getNumber());
//
//			return ResponseEntity.ok(createResponse(true, "", response));
//
////			return ResponseEntity.ok(responseMessage);
//		} catch (IllegalArgumentException e) {
//			return ResponseEntity.badRequest().body(createResponse(false, e.getMessage(), new HashMap<>()));
//		} catch (Exception e) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body(createResponse(false, e.getMessage(), new HashMap<>()));
//		}
//	}

	@PostMapping("/v2/contacted/kcndjiwnjn/jdnjsnja/cxlbijbijsb")
	public ResponseEntity<?> ContactPropertyV2(@RequestBody(required = false) Map<String, String> requestBody) {
		try {

			String userId = requestBody != null ? requestBody.get("userId") : null;
			String propId = requestBody != null ? requestBody.get("propId") : null;

			PropertyDetails responseMessage = propertyService.contactPropertyToUserV2(userId, propId);

			Map<String, Object> response = new HashMap<>();
			response.put("name", responseMessage.getName());
			if ("67128ea2d6da233a1af20f30".equals(userId)) {
				String randomPhoneNumber = "9" + (100000000 + new Random().nextInt(900000000));
				response.put("number", randomPhoneNumber);
			} else {
				response.put("number", responseMessage.getNumber());

			}

			return ResponseEntity.ok(createResponse(true, "", response));

//			return ResponseEntity.ok(responseMessage);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(createResponse(false, e.getMessage(), new HashMap<>()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, e.getMessage(), new HashMap<>()));
		}
	}

	@PostMapping("/ckbwubuw/cjiwbucb/{id}/status/cajbyqwvfydgqv")
	public ResponseEntity<?> changePropertyStatus(@PathVariable String id,
			@RequestBody(required = false) Map<String, String> requestBody) {
		try {
			String newStatus = requestBody.get("newStatus");
			String userId = requestBody.get("userId");

			UserPropertyStatus updatedProperty = propertyService.updatePropertyStatus(id, newStatus, userId);

			Map<String, Object> response = new HashMap<>();
			response.put("id", updatedProperty.getPropId());
			response.put("status", updatedProperty.getStatus());

			return ResponseEntity.ok(createResponse(true, "", response));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, e.getMessage(), new HashMap<>()));
		}
	}

//	

	@PostMapping("/v2/ckjshcigsuch/kjciushcuihn/{userId}/saved-properties/ckjsiuc")
	public ResponseEntity<?> getSavedProperties(@PathVariable String userId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		try {
			// Call the service method to get paginated saved properties
			Page<PropertyDetails> paginatedProperties = propertyService.getSavedPropertiesV2(userId, page, size);

			// Build response similar to the required format
			Map<String, Object> response = new HashMap<>();
			response.put("properties", paginatedProperties.getContent());
			response.put("currentPage", paginatedProperties.getNumber());
			response.put("totalItems", paginatedProperties.getTotalElements());
			response.put("totalPages", paginatedProperties.getTotalPages());

			return ResponseEntity.ok(createResponse(true, "", response));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
					createResponse(false, "An error occurred while fetching properties: " + e.getMessage(), null));
		}
	}

//	@PostMapping("/dncvjien/chibchu/{userId}/ckjbcsjibwi")
//	public ResponseEntity<?> getUserDetails(@PathVariable String userId) {
//		try {
//			User user = userService.getUserById(userId);
//			if (user != null) {
//				user.setNumber(maskPhoneNumber(user.getNumber()));
//				user.setEmail(maskEmail(user.getEmail()));
//				user.setPassword(null);
//				user.setSavedPropertyIds(null);
//				user.setContactedPropertyIds(null);
//				user.setLimit(0);
//				user.setTotalCount(0);
//
//				// Retrieve user's payment history
////				List<PaidAccounts> paymentHistory = paidAccountsRepository.findByUserId(userId);
////
////				// Prepare response data
////				Map<String, Object> responseData = new HashMap<>();
////				responseData.put("user", user);
////				responseData.put("paymentHistory", paymentHistory);
//
//				return ResponseEntity.ok(createResponse(true, "", user));
//			} else {
//				return ResponseEntity.status(HttpStatus.NOT_FOUND)
//						.body(createResponse(false, "User not found", new HashMap<>()));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body(createResponse(false, "An unexpected error occurred.", new HashMap<>()));
//		}
//	}

	@PostMapping("/dsvsdv/v2/casadyt/{userId}/csauyv")
	public ResponseEntity<?> getUserDetailsV2(@PathVariable String userId) {
		try {
			User user = userService.getUserById(userId);
			if (user != null) {
				user.setNumber(maskPhoneNumber(user.getNumber()));
				user.setEmail(maskEmail(user.getEmail()));
				user.setPassword(null);
				user.setSavedPropertyIds(null);
				user.setContactedPropertyIds(null);
				user.setLimit(0);
				user.setTotalCount(0);

				// Retrieve user's payment history
				List<PaidAccounts> paymentHistory = paidAccountsRepository.findByUserId(userId);

				// Prepare response data
				Map<String, Object> responseData = new HashMap<>();
				responseData.put("user", user);
				responseData.put("paymentHistory", paymentHistory);

				return ResponseEntity.ok(createResponse(true, "", responseData));
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(createResponse(false, "User not found", new HashMap<>()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, "An unexpected error occurred.", new HashMap<>()));
		}
	}

	public String maskPhoneNumber(String phoneNumber) {
		if (phoneNumber != null && phoneNumber.length() == 10) {
			return phoneNumber.replaceAll("(\\d{2})\\d{6}(\\d{2})", "$1******$2");
		}
		return phoneNumber; // Return as-is if phone number is invalid
	}

	// Method to mask email
	public String maskEmail(String email) {
		if (email != null && email.contains("@")) {
			String[] parts = email.split("@");
			String username = parts[0];
			String domain = parts[1];

			// Mask part of the username and domain
			String maskedUsername = username.length() > 2 ? username.substring(0, 2) + "****" : username;
			String maskedDomain = domain.length() > 3 ? "****" + domain.substring(domain.length() - 3) : domain;

			return maskedUsername + "@" + maskedDomain;
		}
		return email; // Return as-is if email is invalid
	}

	private Map<String, Object> createResponse(boolean success, String error, Object data) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", success);
		response.put("error", error);
		response.put("data", data);
		return response;
	}

	@GetMapping("/export-contacted-properties/{userId}/vijcbuhscb/csjibcgyswv")
	public ResponseEntity<List<PropertyDetails>> exportContactedPropertiesToJson(@PathVariable String userId) {
		try {
			// Get the list of contacted property details in JSON format
			List<PropertyDetails> properties = userService.exportContactedPropertiesToJson(userId);
			return ResponseEntity.ok(properties);
		} catch (IllegalArgumentException e) {
			// Handle the case where the user is not found or has no contacted properties
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
		}
	}

	@PostMapping("/fkjdbv/submit-suggestion/eijfbidb")
	public ResponseEntity<String> submitSuggestion(@RequestBody Suggestion suggestion) {
		try {
			// Save the suggestion data
			propertyService.saveSuggestion(suggestion);
			return ResponseEntity.ok("Suggestion submitted successfully!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error while submitting the suggestion: " + e.getMessage());
		}
	}

	@PostMapping("/jcebduvhd/vehbvyubheud/property-remark")
	public ResponseEntity<?> addOrUpdateRemark(@RequestBody Map<String, String> requestBody) {
		try {

			String userId = requestBody != null ? requestBody.get("userId") : null;
			String propId = requestBody != null ? requestBody.get("propId") : null;
			String remark = requestBody != null ? requestBody.get("remark") : null;

			if (remark == null) {
				return ResponseEntity.badRequest()
						.body(createResponse(false, "Remark cannot be empty.", new HashMap<>()));
			}

			UserPropertyRemark updatedRemark = propertyService.addOrUpdateRemark(propId, userId, remark);

			Map<String, Object> response = new HashMap<>();
			response.put("id", updatedRemark.getId());
			response.put("propId", updatedRemark.getPropId());
			response.put("userId", updatedRemark.getUserId());
			response.put("remark", updatedRemark.getRemark());
			response.put("createdOn", updatedRemark.getCreatedOn());

			return ResponseEntity.ok(createResponse(true, "", response));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, e.getMessage(), new HashMap<>()));
		}
	}
}
