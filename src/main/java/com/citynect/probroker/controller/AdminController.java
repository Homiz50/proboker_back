package com.citynect.probroker.controller;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.citynect.probroker.dao.ApiLogRepository;
import com.citynect.probroker.dao.PaidAccountsRepository;
import com.citynect.probroker.dao.PropertyDetailsRepository;
import com.citynect.probroker.entities.Admin;
import com.citynect.probroker.entities.AdminRemarkForUser;
import com.citynect.probroker.entities.ApiLog;
import com.citynect.probroker.entities.DemoAccountDTO;
import com.citynect.probroker.entities.DemoAccounts;
import com.citynect.probroker.entities.PaidAccounts;
import com.citynect.probroker.entities.PaidAccountsDTO;
import com.citynect.probroker.entities.PropertyDetails;
import com.citynect.probroker.entities.PropertyDetailsWithUserStatus;
import com.citynect.probroker.entities.PropertyFilter;
import com.citynect.probroker.entities.UpdatePasswordRequest;
import com.citynect.probroker.entities.User;
import com.citynect.probroker.entities.UserPropertyRemark;
import com.citynect.probroker.service.AdminService;
import com.citynect.probroker.service.NotificationService;
import com.citynect.probroker.service.PropertyService;
import com.citynect.probroker.service.UserAlreadyExistsException;
import com.citynect.probroker.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ijscui/probroker-admin/ceknvuhbd/cjwbhusb/protected")
public class AdminController {

	@Autowired
	private AdminService adminService;

	@Autowired
	private PropertyDetailsRepository propertyDetailsRepository;

	@Autowired
	private PropertyService propertyService;

	@Autowired
	private UserService userService;

	@Autowired
	private ApiLogRepository logRepository;

	@Autowired
	private PaidAccountsRepository paidAccountsRepository;

	@Autowired
	private NotificationService notificationService;

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

	@PostMapping("/admin-login/dcjndbvuh/csijbchu")
	public ResponseEntity<?> login(@RequestBody Admin loginRequest) {
		Map<String, Object> response = new HashMap<>();

		try {
			Admin user = adminService.loginAdmin(loginRequest.getNumber(), loginRequest.getPassword());
			user.setPassword(null); // Hide password in response

			response.put("status", "success");
			response.put("message", "Login successful");
			response.put("data", user);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			response.put("status", "error");
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", "An error occurred during login.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/dfxcvsvd/demo-account-list/easfcdasx")
	public ResponseEntity<?> getAllDemoAccounts() {
		List<DemoAccounts> demoAccounts = adminService.getAllDemoAccountsSortedByNewest();
		return ResponseEntity.ok(demoAccounts); // Send the response with the list of accounts
	}

	@PostMapping("/cishcub/premium-account-list/auhnuhzu")
	public ResponseEntity<?> getAllPremiumAccounts() {
		List<PaidAccounts> demoAccounts = adminService.getAllPaidAccountsSortedByNewest();
		return ResponseEntity.ok(demoAccounts); // Send the response with the list of accounts
	}

	@PostMapping("/jsnzvijccni/vdojsnvij/user-all/cjswdcskn")
	public ResponseEntity<?> getAllUsers() {
		List<User> demoAccounts = adminService.getAllUsers();
		return ResponseEntity.ok(demoAccounts); // Send the response with the list of accounts
	}

	@PostMapping("/admin-signup/cejibce/cwdfwa")
	public ResponseEntity<?> register(@RequestBody Admin loginRequest) {
		Map<String, Object> response = new HashMap<>();
		try {
			Admin newUser = adminService.registerAdmin(loginRequest);
			newUser.setPassword(null); // Hide password in response

			response.put("status", "success");
			response.put("message", "User registered successfully");
			response.put("data", newUser);
			return ResponseEntity.ok(response);

		} catch (UserAlreadyExistsException e) {
			response.put("status", "error");
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

		} catch (IllegalArgumentException e) {
			response.put("status", "error");
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

		} catch (Exception e) {
			// Log the error for further analysis
			// logger.error("An error occurred during registration", e);
			response.put("status", "error");
			response.put("message", "An error occurred during registration. Please try again later.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/jkdcbijc/vewhfbhu/{adminId}")
	public ResponseEntity<?> getAdminDetails(@PathVariable String adminId) {
		try {
			Admin user = adminService.getAdminById(adminId);
			if (user != null) {
				user.setNumber(maskPhoneNumber(user.getNumber()));
				user.setPassword(null);

				return ResponseEntity.ok(createResponse(true, "", user));
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

	@PostMapping("/deleteByCreatedDate/cijebduyh/coijsbuhcv/dkbuhvu")
	public ResponseEntity<String> deleteByCreatedDate(@RequestBody Map<String, String> requestBody) {
		try {

			String createdDate = requestBody.get("createdDate");
			String type = requestBody.get("type");

			int deletedCount = propertyService.deleteByCreatedDate(createdDate, type);
			return ResponseEntity.ok(deletedCount + " records deleted successfully");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error deleting records: " + e.getMessage());
		}
	}

	@PostMapping("/upload/vdkjfbwib/wjndbhwbd")
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
		Map<String, Object> response = propertyService.uploadPropertiesV2(file);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/update-password/cidbvuhvsdcvdf/vhbvuh")
	public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
		try {
			String message = userService.updatePassword(updatePasswordRequest);
			return ResponseEntity.ok(message);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating password: " + e.getMessage());
		}
	}

	@PostMapping("/updateFurnishedType/cjibuhc/cdjbsuh")
	public ResponseEntity<String> updateFurnishedType() {
		try {
			int updatedPropertiesCount = propertyService.updateFurnishedType();
			return ResponseEntity.ok("Successfully updated " + updatedPropertiesCount + " properties to 'Furnished'.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating properties' furnished type: " + e.getMessage());
		}
	}

	@PostMapping("/chjbhfdwbhsc/sjdwihvicshb/register-or-activate-demo/duhcwbhifbw/ckjdbicb")
	public ResponseEntity<?> registerOrActivateDemoAccount(@RequestBody DemoAccountDTO demoAccountDTO)
			throws JsonProcessingException {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String method = request.getMethod();
		String url = request.getRequestURI();
		try {
			String requestPayloadJson = objectMapper.writeValueAsString(demoAccountDTO);

			// Call the service method to register or activate a demo account
			String message = userService.registerOrActivateDemoAccountV2(demoAccountDTO);
			Map<String, String> response = new HashMap<>();
			response.put("message", message);
			response.put("number", demoAccountDTO.getNumber());
			response.put("password", demoAccountDTO.getPassword());

			String responsePayloadJson = objectMapper.writeValueAsString(response);

			saveApiLog(method, "activate demo controller log", url, null, requestPayloadJson, responsePayloadJson,
					HttpStatus.OK.value(), null, null);

			return ResponseEntity.ok(createResponse(true, "", response));

		} catch (IllegalArgumentException e) {
			String requestPayloadJson = objectMapper.writeValueAsString(demoAccountDTO);

			saveApiLog(method, "activate demo controller exception log", url, null, requestPayloadJson, null, 400,
					e.getMessage(), null);
			// Handle invalid inputs or arguments
			return ResponseEntity.badRequest().body(createResponse(false, e.getMessage(), new HashMap<>()));
		} catch (Exception e) {
			// Catch any unexpected exceptions
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, e.getMessage(), new HashMap<>()));
		}
	}

	@PostMapping("/cjevfhu/ejvfuhve/activate-premium/vejfscihjhei")
	public ResponseEntity<?> ActivatePremium(@RequestBody PaidAccountsDTO paidAccountsDTO) {
		try {
			// Call the service method to register or activate a demo account
			String message = userService.ActivatePremium(paidAccountsDTO);

			return ResponseEntity.ok(createResponse(true, "", message));
		} catch (IllegalArgumentException e) {
			// Handle invalid inputs or arguments
			return ResponseEntity.badRequest().body(createResponse(false, e.getMessage(), new HashMap<>()));
		} catch (Exception e) {
			// Catch any unexpected exceptions
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, e.getMessage(), new HashMap<>()));
		}
	}

	@PostMapping("/V2/cjevfhu/ejvfuhve/activate-premium/vejfscihjhei")
	public ResponseEntity<?> ActivatePremiumV2(@RequestBody PaidAccountsDTO paidAccountsDTO) {
		try {
			// Call the service method to register or activate a demo account
			String message = userService.ActivatePremiumV2(paidAccountsDTO);

			return ResponseEntity.ok(createResponse(true, "", message));
		} catch (IllegalArgumentException e) {
			// Handle invalid inputs or arguments
			return ResponseEntity.badRequest().body(createResponse(false, e.getMessage(), new HashMap<>()));
		} catch (Exception e) {
			// Catch any unexpected exceptions
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, e.getMessage(), new HashMap<>()));
		}
	}

	@PostMapping("/send-emails")
	public ResponseEntity<String> sendEmails(@RequestParam("file") MultipartFile file,
			@RequestParam("subject") String subject, @RequestParam("content") String content) {
		try {
			String response = notificationService.sendEmailsFromFile(file, subject, content);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Failed to send emails: " + e.getMessage());
		}
	}

	@PostMapping("/update-square-ft")
	public ResponseEntity<?> updateSquareFtField() {
		try {
			// Call the service method to update square feet field
			int modifiedCount = userService.updateSquareFtField();

			// Return the number of modified documents in the response
			Map<String, Object> response = new HashMap<>();
			response.put("message", "Square feet field updated successfully");
			response.put("modifiedDocuments", modifiedCount);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			// Handle any potential errors
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while updating square feet field.");
		}
	}

	@PostMapping("/unverified/fcijhscuh/ckjbschus")
	public List<PropertyDetailsWithUserStatus> getUnverifiedPropertyDetails() {
		return propertyService.getUnverifiedPropertyDetailsForAdmin();
	}

	@PostMapping("/vkndbvh/verify-duplicate-properties/vkjdbucw")
	public List<PropertyDetails> getDuplicateProperties() {
		return adminService.getDuplicatePropertyDetails();
	}

	@PostMapping("/update-report-status")
	public ResponseEntity<String> updateReportStatus(@RequestParam String reportId, @RequestParam String adminId,
			@RequestParam boolean action) {
		try {
			adminService.updateReportStatus(reportId, adminId, action);
			return ResponseEntity.ok("Report status updated successfully.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating report status: " + e.getMessage());
		}
	}

	@PostMapping("/remove-property/cdjivbi/ceijb")
	public ResponseEntity<String> removeProperty(@RequestBody Map<String, String> requestBody) {
		try {
			String adminId = requestBody.get("adminId");
			String propertyId = requestBody.get("propertyId");

			adminService.removeProperty(adminId, propertyId);
			return ResponseEntity.ok("Report status updated successfully.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating report status: " + e.getMessage());
		}
	}

	@PostMapping("/jdbifbei/uhygyu/filter/cjdbceu/cenjvbcyg")
	public ResponseEntity<?> filterPropertiesv2(@RequestBody PropertyFilter filterRequest,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		try {
			if (size > 25) {
				size = 25;
			}

			Map<String, Object> response = new HashMap<>();

			if (filterRequest.getUserId() == null || filterRequest.getUserId().isEmpty()) {
				response.put("properties", Collections.emptyList());
				response.put("currentPage", 0);
				response.put("totalItems", 0);
				response.put("totalPages", 0);
				return ResponseEntity.ok(createResponse(true, "", response));

			}
			Page<PropertyDetails> filteredPropertiesPage = adminService.filterPropertiesSharingFlat(filterRequest, page,
					size);

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

	@PostMapping("/update-settlement-status")
	public ResponseEntity<?> updateSettlementStatus(@RequestParam String id, @RequestParam String adminId) {

		try {
			// Call service method to update the settlement status
			PaidAccounts updatedPaidAccount = adminService.updateSettlementStatus(id, adminId);

			// Return success response with updated record
			return ResponseEntity.ok(createResponse(true, "", updatedPaidAccount));

		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createResponse(false, e.getMessage(), new HashMap<>()));

		} catch (Exception e) {
			// Handle unexpected errors
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, e.getMessage(), new HashMap<>()));
		}
	}

	@PostMapping("/properties/setCallAllowedFalse")
	public ResponseEntity<String> setCallAllowedToFalse() {
		adminService.setCallAllowedToFalseForAllProperties();
		return ResponseEntity.ok("All properties updated successfully.");
	}

	@PostMapping("/duplicate-numbers")
	public ResponseEntity<List<PropertyDetails>> getPropertiesWithDuplicateNumbers() {
		List<PropertyDetails> duplicateProperties = propertyService.findPropertiesWithDuplicateNumbers();
		return ResponseEntity.ok(duplicateProperties);
	}

	@PostMapping("/vjbenduhcbe/ceuhsfyue/ckjeisbuc")
	public ResponseEntity<?> updatePropertiesPhoneNumberByExcel(@RequestParam("file") MultipartFile file) {
		try {
			// Extract property IDs and phone numbers from Excel
			Map<String, String> propertyIdToPhoneNumberMap = extractPropertyIdToPhoneNumberMapFromExcel(
					file.getInputStream());

			// Update properties in the database
			for (Map.Entry<String, String> entry : propertyIdToPhoneNumberMap.entrySet()) {
				String propertyId = entry.getKey();
				String phoneNumber = entry.getValue();

				// Fetch property by ID
				PropertyDetails property = propertyDetailsRepository.findById(propertyId)
						.orElseThrow(() -> new RuntimeException("Property not found with ID: " + propertyId));

				// Update phone number
				property.setNumber(phoneNumber);
				propertyDetailsRepository.save(property);
			}

			System.out.println("Updated Properties with Phone Numbers: " + propertyIdToPhoneNumberMap);

			return ResponseEntity.ok("Phone numbers updated successfully for all properties.");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("Error processing file: " + e.getMessage());
		}
	}

	@PostMapping("/cdhucygevc/admin-remark/for-user/cdkjncu")
	public ResponseEntity<?> addOrUpdateRemark(@RequestBody Map<String, String> requestBody) {
		try {
			String userId = requestBody.get("userId");
			String adminId = requestBody.get("adminId");
			String remark = requestBody.get("remark");
			String followupDate = requestBody.get("followupDate");

			if (remark == null) {
				return ResponseEntity.badRequest()
						.body(createResponse(false, "Remark cannot be empty.", new HashMap<>()));
			}

			AdminRemarkForUser adminRemarkForUser = adminService.addOrUpdateRemark(adminId, userId, remark,
					followupDate);

			Map<String, Object> response = new HashMap<>();
			response.put("id", adminRemarkForUser.getId());
			response.put("adminId", adminRemarkForUser.getAdminId());
			response.put("userId", adminRemarkForUser.getUserId());
			response.put("remark", adminRemarkForUser.getRemark());
			response.put("followupDate", adminRemarkForUser.getFollowupDate());
			response.put("createdOn", adminRemarkForUser.getCreatedOn());

			return ResponseEntity.ok(createResponse(true, "", response));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createResponse(false, e.getMessage(), new HashMap<>()));
		}
	}

	@PostMapping("/cedbvhu/edit/property/fcojkehfui/{adminId}")
	public ResponseEntity<?> editPropertyDetails(@PathVariable String adminId,
			@RequestBody PropertyDetails updatedDetails) {

		try {
			PropertyDetails updatedProperty = adminService.editPropertyDetails(updatedDetails, adminId);
			return ResponseEntity.ok(createResponse(true, "", updatedProperty));

		} catch (IllegalArgumentException e) {
			// Handle specific exception for missing property
			return ResponseEntity.badRequest().body(createResponse(false, e.getMessage(), new HashMap<>()));
		} catch (Exception e) {
			// Handle any unexpected exceptions
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createResponse(false,
					"An error occurred while editing the property: " + e.getMessage(), new HashMap<>()));
		}
	}

	@PostMapping("/vkmednvu/property/{id}/restore")
	public ResponseEntity<?> restoreProperty(@PathVariable String id) {
		try {
			// Fetch the property by ID
			PropertyDetails propertyDetails = propertyDetailsRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + id));

			// Update the isDeleted field to 0
			propertyDetails.setIsDeleted(0);

			// Save the updated property
			propertyDetailsRepository.save(propertyDetails);

			return ResponseEntity.ok("Property restored successfully.");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while restoring the property: " + e.getMessage());
		}
	}

	@PostMapping("/vmkdnfvije/csjinhud/delete/property/{id}")
	public ResponseEntity<?> deleteProperty(@PathVariable String id) {
		try {
			// Check if the property exists
			if (!propertyDetailsRepository.existsById(id)) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Property not found with ID: " + id);
			}

			// Delete the property
			propertyDetailsRepository.deleteById(id);

			return ResponseEntity.ok("Property deleted successfully.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while deleting the property: " + e.getMessage());
		}
	}

	@PostMapping("/cdkjbcie/ckdbvhu/get/property/{id}")
	public ResponseEntity<?> getPropertyById(@PathVariable String id) {
		try {
			// Fetch the property details by ID
			Optional<PropertyDetails> propertyDetails = propertyDetailsRepository.findById(id);

			// Check if the property exists
			if (propertyDetails.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Property not found with ID: " + id);
			}

			// Return the property details
			return ResponseEntity.ok(propertyDetails.get());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while fetching the property: " + e.getMessage());
		}
	}

	private Map<String, String> extractPropertyIdToPhoneNumberMapFromExcel(InputStream inputStream) throws Exception {
		Map<String, String> propertyIdToPhoneNumberMap = new HashMap<>();

		Workbook workbook = WorkbookFactory.create(inputStream);
		Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

		boolean isFirstRow = true; // Flag to identify the first row

		for (Row row : sheet) {
			if (isFirstRow) {
				isFirstRow = false; // Skip the header row
				continue;
			}
			Cell idCell = row.getCell(0); // IDs are in the first column
			Cell phoneCell = row.getCell(1); // Phone numbers are in the second column

			if (idCell != null && phoneCell != null) {
				String propertyId = "";
				String phoneNumber = "";

				// Extract ID
				if (idCell.getCellType() == CellType.STRING) {
					propertyId = idCell.getStringCellValue().trim();
				} else if (idCell.getCellType() == CellType.NUMERIC) {
					propertyId = String.valueOf((long) idCell.getNumericCellValue());
				}

				// Extract phone number
				if (phoneCell.getCellType() == CellType.STRING) {
					phoneNumber = phoneCell.getStringCellValue().trim();
				} else if (phoneCell.getCellType() == CellType.NUMERIC) {
					phoneNumber = String.valueOf((long) phoneCell.getNumericCellValue());
				}

				// Add to map
				if (!propertyId.isEmpty() && !phoneNumber.isEmpty()) {
					propertyIdToPhoneNumberMap.put(propertyId, phoneNumber);
				}
			}
		}

		workbook.close();
		System.out.println("Extracted Property IDs and Phone Numbers: " + propertyIdToPhoneNumberMap); // Debug log

		return propertyIdToPhoneNumberMap;
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
}
