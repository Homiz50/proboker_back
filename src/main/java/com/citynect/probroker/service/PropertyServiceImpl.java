package com.citynect.probroker.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.citynect.probroker.dao.PropertyDetailsRepository;
import com.citynect.probroker.dao.SuggestionRepository;
import com.citynect.probroker.dao.UserContactedPropertyHistoryRepository;
import com.citynect.probroker.dao.UserPropertyRemarkRepository;
import com.citynect.probroker.dao.UserPropertyStatusRepository;
import com.citynect.probroker.dao.UserRepository;
import com.citynect.probroker.entities.PropertyDetails;
import com.citynect.probroker.entities.PropertyDetailsWithUserStatus;
import com.citynect.probroker.entities.PropertyFilter;
import com.citynect.probroker.entities.Suggestion;
import com.citynect.probroker.entities.User;
import com.citynect.probroker.entities.UserContactedPropertyHistory;
import com.citynect.probroker.entities.UserPropertyRemark;
import com.citynect.probroker.entities.UserPropertyStatus;
import com.ibm.icu.text.SimpleDateFormat;
import com.mongodb.client.result.DeleteResult;
import com.monitorjbl.xlsx.StreamingReader;

@Service
public class PropertyServiceImpl implements PropertyService {

	@Autowired
	private PropertyDetailsRepository propertyDetailsRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserPropertyRemarkRepository userPropertyRemarkRepository;

	@Autowired
	private SuggestionRepository suggestionRepository;

	@Autowired
	private UserContactedPropertyHistoryRepository userContactedPropertyHistoryRepository;

	@Autowired
	private UserPropertyStatusRepository userPropertyStatusRepository;

//	@Autowired
//	private NumbersRepository numbersRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Transactional
	public Map<String, Object> uploadProperties(MultipartFile file) {
		Map<String, Object> response = new HashMap<>();
		List<PropertyDetails> uploadedProperties = new ArrayList<>();
		List<Map<String, String>> notUploadedProperties = new ArrayList<>();
		try {
			Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096)
					.open(file.getInputStream());

			for (Sheet sheet : workbook) {
				for (Row row : sheet) {
					if (row.getRowNum() == 0)
						continue; // Skip header row

					// Check if the row is considered empty. For simplicity, we're checking if the
					// first cell is empty.
					Cell firstCell = row.getCell(0);
					if (firstCell == null || firstCell.getCellType() == CellType.BLANK) {
						// Stop processing further rows once an empty row is encountered
						break;
					}

					// Now process and save property details with owner ID set
					PropertyDetails propertyDetails = parseRowToPropertyDetails(row);

//					boolean propertyExists = propertyDetailsRepository
//							.existsByNumberAndType(propertyDetails.getNumber(), propertyDetails.getType());\

					boolean propertyExists = propertyDetailsRepository.existsByNumberAndTypeAndUnitTypeAndTitle(
							propertyDetails.getNumber(), propertyDetails.getType(), propertyDetails.getUnitType(),
							propertyDetails.getTitle());

					if (!propertyExists) {
						// Save the property if it doesn't exist
						PropertyDetails savedProperty = propertyDetailsRepository.save(propertyDetails);
						uploadedProperties.add(savedProperty);
					} else {
						// Add to not uploaded list
						Map<String, String> notUploadedInfo = new HashMap<>();
						notUploadedInfo.put("number", propertyDetails.getNumber());
						notUploadedInfo.put("address", propertyDetails.getAddress());
						notUploadedProperties.add(notUploadedInfo);
					}

				}
			}

			response.put("message", "Uploaded and saved " + uploadedProperties.size() + " properties successfully.");
			response.put("notUploadedProperties", notUploadedProperties);
		} catch (IOException e) {
			e.printStackTrace();
			e.printStackTrace();
			response.put("message", "Failed to upload and save properties: " + e.getMessage());
			response.put("notUploadedProperties", Collections.emptyList());
		}
		return response;
	}

	@Transactional
	public Map<String, Object> uploadPropertiesV2(MultipartFile file) {
		Map<String, Object> response = new HashMap<>();
		List<PropertyDetails> uploadedProperties = new ArrayList<>();
		List<Map<String, String>> notUploadedProperties = new ArrayList<>();
		try {
			Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096)
					.open(file.getInputStream());

			for (Sheet sheet : workbook) {
				for (Row row : sheet) {
					if (row.getRowNum() == 0)
						continue; // Skip header row

					// Check if the row is considered empty. For simplicity, we're checking if the
					// first cell is empty.
					Cell firstCell = row.getCell(0);
					if (firstCell == null || firstCell.getCellType() == CellType.BLANK) {
						// Stop processing further rows once an empty row is encountered
						break;
					}

					// Now process and save property details with owner ID set
					PropertyDetails propertyDetails = parseRowToPropertyDetails(row);

					boolean propertyExists = propertyDetailsRepository.existsByNumberAndTypeAndUnitTypeAndTitle(
							propertyDetails.getNumber(), propertyDetails.getType(), propertyDetails.getUnitType(),
							propertyDetails.getTitle());

					if (propertyExists) {
						propertyDetails.setIsDeleted(2);

						Map<String, String> duplicateInfo = new HashMap<>();
						duplicateInfo.put("number", propertyDetails.getNumber());
						duplicateInfo.put("title", propertyDetails.getTitle());
						duplicateInfo.put("address", propertyDetails.getAddress());
						duplicateInfo.put("unitType", propertyDetails.getUnitType());
						notUploadedProperties.add(duplicateInfo);
						// Save the property if it doesn't exist

					} else {
						propertyDetails.setIsDeleted(0);

					}
					PropertyDetails savedProperty = propertyDetailsRepository.save(propertyDetails);
					uploadedProperties.add(savedProperty);

				}
			}

			response.put("message", "Uploaded and saved " + uploadedProperties.size() + " properties successfully.");
			response.put("notUploadedProperties", notUploadedProperties);
		} catch (IOException e) {
			e.printStackTrace();
			e.printStackTrace();
			response.put("message", "Failed to upload and save properties: " + e.getMessage());
			response.put("notUploadedProperties", Collections.emptyList());
		}
		return response;
	}

	private PropertyDetails parseRowToPropertyDetails(Row row) {
		PropertyDetails propertyDetails = new PropertyDetails();

		String listedDateString = getStringCellValue(row.getCell(0)).trim();

		try {
			// Parse the String into a LocalDateTime object
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			LocalDate localDate = LocalDate.parse(listedDateString, formatter);

			// Convert LocalDateTime to Date
			ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
			Date listedDate = Date.from(zonedDateTime.toInstant());

			// Set the Date to the propertyDetails
			propertyDetails.setListedDate(listedDate);
		} catch (DateTimeParseException e) {
			// Handle the exception, maybe log it or set a default value
			propertyDetails.setListedDate(null); // Or handle it differently
		}

		propertyDetails.setType(getStringCellValue(row.getCell(1)).trim());
		propertyDetails.setRent(getStringCellValue(row.getCell(2)).trim());
		propertyDetails.setRentValue(getIntCellValue(row.getCell(3)));
		propertyDetails.setBhk(getStringCellValue(row.getCell(4)).trim());
		propertyDetails.setTitle(getStringCellValue(row.getCell(5)).trim());
		propertyDetails.setSqFt(getIntCellValue(row.getCell(6)));
		propertyDetails.setSquareFt(getStringCellValue(row.getCell(6)).trim());
		propertyDetails.setAddress(getStringCellValue(row.getCell(7)).trim());
		propertyDetails.setArea(getStringCellValue(row.getCell(8)).trim());
		propertyDetails.setCity(getStringCellValue(row.getCell(9)).trim());
		propertyDetails.setDescription(getStringCellValue(row.getCell(10)).trim());
		propertyDetails.setUnitType(getStringCellValue(row.getCell(11)).trim());
		propertyDetails.setName(getStringCellValue(row.getCell(12)).trim());
		propertyDetails.setNumber(getStringCellValue(row.getCell(13)).trim());
		propertyDetails.setFurnishedType(getStringCellValue(row.getCell(14)).trim());
		propertyDetails.setUserType("Owner");

		propertyDetails.setStatus("Active");

		propertyDetails.setCreatedOn(LocalDateTime.now());
		propertyDetails.setIsDeleted(0);

		return propertyDetails;
	}

	public String getBhkValue(Cell cell) {
		if (cell == null || cell.getCellType() == CellType.BLANK) {
			return "NA"; // Return "NA" if cell is empty
		}

		String cellValue = cell.getStringCellValue().trim(); // Get the cell value as a string

		// Check if the cell value is a valid number
		try {
			int bhkValue = Integer.parseInt(cellValue);
			return bhkValue + " BHK"; // Append " BHK" to the numeric value
		} catch (NumberFormatException e) {
			return "NA"; // Return "NA" if the value is not a number
		}
	}

	private String getStringCellValue(Cell cell) {
		if (cell == null) {
			return ""; // Returns an empty string if null
		}
		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
				return df.format(date);
			} else {
				// Treat numeric cell as a string to preserve its exact value
				return String.valueOf((long) cell.getNumericCellValue());
			}
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			return cell.getCellFormula();
		default:
			return "";
		}
	}

	private int getIntCellValue(Cell cell) {
		if (cell == null || cell.getCellType() != CellType.NUMERIC) {
			return 0; // Return a default value or handle appropriately
		}
		return (int) cell.getNumericCellValue();
	}

	// filter version-1
//	public Page<PropertyDetails> filterPropertiesSharingFlat(PropertyFilter filterRequest, int page, int size) {
//		// Build the query criteria based on the filter request
//		try {
//			User user = userRepository.findById(filterRequest.getUserId())
//					.orElseThrow(() -> new RuntimeException("User not found"));
//
//			Criteria criteria = new Criteria();
//
////			criteria = criteria.and("isDeleted").is(0);
//			if (filterRequest.getStatus() != null) {
//				switch (filterRequest.getStatus().toLowerCase()) {
//				case "active":
//					criteria = criteria.and("isDeleted").is(1);
//					break;
//				case "deleted":
//					criteria = criteria.and("isDeleted").is(0);
//					break;
//				case "yesterday":
//					LocalDate yesterday = LocalDate.now().minusDays(1);
//					criteria = criteria.and("listedDate").gte(yesterday.atStartOfDay())
//							.lt(yesterday.plusDays(1).atStartOfDay());
//					break;
//				case "today":
//					LocalDate today = LocalDate.now();
//					criteria = criteria.and("listedDate").gte(today.atStartOfDay())
//							.lt(today.plusDays(1).atStartOfDay());
//					break;
//				}
//			}
//
//			if (filterRequest.getType() != null && !filterRequest.getType().isEmpty()) {
//				criteria = criteria.and("type").regex("^" + Pattern.quote(filterRequest.getType()), "i"); // case-insensitive
//			}
//
//			if (filterRequest.getAreas() != null && !filterRequest.getAreas().isEmpty()) {
//				List<Criteria> areaCriteriaList = new ArrayList<>();
//				for (String area : filterRequest.getAreas()) {
//					areaCriteriaList.add(Criteria.where("area").regex("^" + Pattern.quote(area), "i"));
//				}
//				criteria = criteria.orOperator(areaCriteriaList.toArray(new Criteria[0]));
//			}
//
//			if (filterRequest.getBhks() != null && !filterRequest.getBhks().isEmpty()) {
//				criteria = criteria.and("bhk").in(filterRequest.getBhks());
//			}
//
//			if (filterRequest.getFurnishedTypes() != null && !filterRequest.getFurnishedTypes().isEmpty()) {
//				criteria = criteria.and("furnishedType").in(filterRequest.getFurnishedTypes());
//			}
//			if (filterRequest.getMinRent() != null) {
//				criteria = criteria.and("rentValue").gte(filterRequest.getMinRent());
//			}
//
//			if (filterRequest.getMaxRent() != null) {
//				criteria = criteria.and("rentValue").lte(filterRequest.getMaxRent());
//			}
//
//			// Create the Pageable object
//			Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "listedDate"));
//
//			// Create the Query object with pagination
//			Query query = Query.query(criteria).with(pageable);
//
//			// Execute the query to get the properties
//			List<PropertyDetails> properties = mongoTemplate.find(query, PropertyDetails.class);
//			// Check if the user is premium and modify property numbers if not
//			if (user.getIsPremium() == 0) {
//				properties.forEach(property -> {
//
//					property.setNumber("0");
//					property.setAddress("Buy Premium");
//					property.setTitle("Buy Premium");
//				});
//			}
//			// Count total number of items matching the criteria
//			Query countQuery = Query.query(criteria);
//			long totalItems = mongoTemplate.count(countQuery, PropertyDetails.class);
//
//			// Return the Page object
//			return new PageImpl<>(properties, pageable, totalItems);
//
//		} catch (Exception e) {
//			// Add logging for exceptions
//			System.out.println("An error occurred while fetching properties: " + e.getMessage());
//			e.printStackTrace();
//			throw e;
//		}
//	}

	// filter version-2
//	public Page<PropertyDetails> filterPropertiesSharingFlat(PropertyFilter filterRequest, int page, int size) {
//		try {
//
//			User user = null;
//			boolean isUserPremium = false;
//			if (filterRequest.getUserId() != null && !filterRequest.getUserId().isEmpty()) {
//				user = userRepository.findById(filterRequest.getUserId()).orElse(null);
//				if (user == null || user.getIsPremium() == 0) {
//
//					Criteria criteria = new Criteria();
//
//					// Create Pageable object
//					Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "listedDate"));
//
//					// Create Query object with criteria and pagination
//					Query query = Query.query(criteria).with(pageable);
//
//					// Execute the query to get properties
//					List<PropertyDetails> properties = mongoTemplate.find(query, PropertyDetails.class);
//
//					// Count total number of items matching the criteria
//					Query countQuery = Query.query(criteria);
//					long totalItems = mongoTemplate.count(countQuery, PropertyDetails.class);
//					return new PageImpl<>(properties, pageable, totalItems);
//
//				} else {
//					isUserPremium = true;
//				}
//			}
//
//			// Initialize criteria
//			Criteria criteria = new Criteria();
//
////			criteria = criteria.and("userType").is("Owner");
//
//			// Define server time zone
//			ZoneOffset zoneOffset = ZoneOffset.UTC;
//
//			// Handle status-based filtering
//			if (filterRequest.getStatus() != null) {
//				switch (filterRequest.getStatus().toLowerCase()) {
//				case "active":
//					criteria = criteria.and("isDeleted").is(0);
//					break;
//				case "deleted":
//					criteria = criteria.and("isDeleted").is(1);
//					break;
//				case "yesterday":
//					ZonedDateTime yesterday = ZonedDateTime.now(zoneOffset).minusDays(1);
//					LocalDateTime startOfYesterday = yesterday.toLocalDate().atStartOfDay();
//					LocalDateTime endOfYesterday = startOfYesterday.plusDays(1);
//					criteria = criteria.and("listedDate").gte(startOfYesterday).lt(endOfYesterday);
//					break;
//				case "today":
//					ZonedDateTime today = ZonedDateTime.now(zoneOffset);
//					LocalDateTime startOfToday = today.toLocalDate().atStartOfDay();
//					LocalDateTime endOfToday = startOfToday.plusDays(1);
//					criteria = criteria.and("listedDate").gte(startOfToday).lt(endOfToday);
//					break;
//				}
//			}
//
//			// Handle type-based filtering
//			if (filterRequest.getType() != null && !filterRequest.getType().isEmpty()) {
//				criteria = criteria.and("type").regex("^" + Pattern.quote(filterRequest.getType()), "i");
//			}
//
//			if (filterRequest.getListedBy() != null && !filterRequest.getListedBy().isEmpty()) {
//				criteria = criteria.and("userType").regex("^" + Pattern.quote(filterRequest.getListedBy()), "i");
//			}
//
//			if (filterRequest.getListedBy() != null && !filterRequest.getListedBy().isEmpty()) {
//				if (filterRequest.getListedBy().equalsIgnoreCase("Owner")) {
//					criteria = criteria.andOperator(Criteria.where("userType").regex("^Owner$", "i"),
//							Criteria.where("userType").regex("^Builder$", "i"));
//				} else {
//					criteria = criteria.and("userType").regex("^" + Pattern.quote(filterRequest.getListedBy()), "i");
//				}
//			}
//
//			// Handle area-based filtering
//			if (filterRequest.getAreas() != null && !filterRequest.getAreas().isEmpty()) {
//				List<Criteria> areaCriteriaList = new ArrayList<>();
//				for (String area : filterRequest.getAreas()) {
//					areaCriteriaList.add(Criteria.where("area").regex("^" + Pattern.quote(area), "i"));
//				}
//				criteria = criteria.orOperator(areaCriteriaList.toArray(new Criteria[0]));
//			}
//
//			// Handle bhk-based filtering
//			if (filterRequest.getBhks() != null && !filterRequest.getBhks().isEmpty()) {
//				criteria = criteria.and("bhk").in(filterRequest.getBhks());
//			}
//
//			// Handle furnishedType-based filtering
//			if (filterRequest.getFurnishedTypes() != null && !filterRequest.getFurnishedTypes().isEmpty()) {
//				criteria = criteria.and("furnishedType").in(filterRequest.getFurnishedTypes());
//			}
//
//			// Handle rent-based filtering with proper criteria combination
//			if (filterRequest.getMinRent() != null && filterRequest.getMaxRent() != null) {
//				criteria = criteria.and("rentValue").gte(filterRequest.getMinRent()).lte(filterRequest.getMaxRent());
//			} else if (filterRequest.getMinRent() != null) {
//				criteria = criteria.and("rentValue").gte(filterRequest.getMinRent());
//			} else if (filterRequest.getMaxRent() != null) {
//				criteria = criteria.and("rentValue").lte(filterRequest.getMaxRent());
//			}
//
//			// Create Pageable object
//			Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "listedDate"));
//
//			// Create Query object with criteria and pagination
//			Query query = Query.query(criteria).with(pageable);
//
//			// Execute the query to get properties
//			List<PropertyDetails> properties = mongoTemplate.find(query, PropertyDetails.class);
//
//			// Check if the user is premium and modify property details if not
//			if (user.getIsPremium() == 0) {
//				properties.forEach(property -> {
//					property.setNumber("0");
//					property.setAddress("Buy Premium");
//					property.setTitle("Buy Premium");
//				});
//			}
//
//			// Check if the user is not premium or userId is null/empty/user not found, and
//			// modify property details if true
//			if (!isUserPremium) {
//				properties.forEach(property -> {
//					property.setNumber("0");
//					property.setAddress("Buy Premium");
//					property.setTitle("Buy Premium");
//				});
//			}
//
//			// Count total number of items matching the criteria
//			Query countQuery = Query.query(criteria);
//			long totalItems = mongoTemplate.count(countQuery, PropertyDetails.class);
//
//			// Adjust the size if it exceeds the remaining items
//			if (size > properties.size()) {
//				size = properties.size();
//			}
//			return new PageImpl<>(properties, pageable, totalItems);
//
//		} catch (Exception e) {
//			// Log and throw exception
//			System.out.println("An error occurred while fetching properties: " + e.getMessage());
//			e.printStackTrace();
//			throw e;
//		}
//	}

//	public Page<PropertyDetails> filterPropertiesSharingFlat(PropertyFilter filterRequest, int page, int size) {
//		try {
//			User user = null;
//			if (filterRequest.getUserId() != null && !filterRequest.getUserId().isEmpty()) {
//				user = userRepository.findById(filterRequest.getUserId()).orElse(null);
//				if (user == null || user.getIsPremium() == 0) {
//					Criteria criteria = new Criteria();
//					Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "listedDate"));
//					Query query = Query.query(criteria).with(pageable);
//					List<PropertyDetails> properties = mongoTemplate.find(query, PropertyDetails.class);
//					Query countQuery = Query.query(criteria);
//					long totalItems = mongoTemplate.count(countQuery, PropertyDetails.class);
//					return new PageImpl<>(properties, pageable, totalItems);
//				}
//			}
//
//			// Initialize criteria
//			Criteria criteria = new Criteria();
//
//			// Handle status-based filtering
//			if (filterRequest.getStatus() != null) {
//				switch (filterRequest.getStatus().toLowerCase()) {
//				case "active":
//					criteria = criteria.and("isDeleted").is(0);
//					break;
//				case "deleted":
//					criteria = criteria.and("isDeleted").is(1);
//					break;
//				}
//			}
//
//			// Handle type-based filtering
//			if (filterRequest.getType() != null && !filterRequest.getType().isEmpty()) {
//				criteria = criteria.and("type").regex("^" + Pattern.quote(filterRequest.getType()), "i");
//			}
//
//			// Handle search-based filtering (search in title and area)
//			if (filterRequest.getSearch() != null && !filterRequest.getSearch().isEmpty()) {
//				criteria = criteria.orOperator(Criteria.where("title").regex(filterRequest.getSearch(), "i"),
//						Criteria.where("area").regex(filterRequest.getSearch(), "i"));
//			}
//
//			// Handle listedBy filtering
//			if (filterRequest.getListedBy() != null && !filterRequest.getListedBy().isEmpty()) {
//				if (filterRequest.getListedBy().equalsIgnoreCase("Agent")) {
//					criteria = criteria.and("userType").regex("^Agent$", "i");
//				} else {
//					criteria = criteria.and("userType").in("Owner", "Builder");
//				}
//			}
//
//			// Combine all $or conditions into a list
//			List<Criteria> orCriteriaList = new ArrayList<>();
//
//			// Area-based filtering
//			if (filterRequest.getAreas() != null && !filterRequest.getAreas().isEmpty()) {
//				for (String area : filterRequest.getAreas()) {
//					orCriteriaList.add(Criteria.where("area").regex("^" + Pattern.quote(area), "i"));
//				}
//			}
//
//			// BHK-based filtering
//			if (filterRequest.getBhks() != null && !filterRequest.getBhks().isEmpty()) {
//				for (String bhk : filterRequest.getBhks()) {
//					orCriteriaList.add(Criteria.where("bhk").regex("^" + Pattern.quote(bhk), "i"));
//				}
//			}
//
//			// Subtype-based filtering
//			if (filterRequest.getSubtype() != null && !filterRequest.getSubtype().isEmpty()) {
//				for (String subtype : filterRequest.getSubtype()) {
//					orCriteriaList.add(Criteria.where("unitType").regex("^" + Pattern.quote(subtype), "i"));
//				}
//			}
//
//			// Furnished type filtering
//			if (filterRequest.getFurnishedTypes() != null && !filterRequest.getFurnishedTypes().isEmpty()) {
//				for (String furnishedType : filterRequest.getFurnishedTypes()) {
//					orCriteriaList.add(Criteria.where("furnishedType").regex("^" + Pattern.quote(furnishedType), "i"));
//				}
//			}
//
//			// Add the combined $or criteria to the main criteria
//			if (!orCriteriaList.isEmpty()) {
//				criteria = criteria.andOperator(orCriteriaList.toArray(new Criteria[0]));
//			}
//
//			// Handle sqft filtering
//			if (filterRequest.getSqft() > 0) {
//				criteria = criteria.and("squareFt").gte(String.valueOf(filterRequest.getSqft()));
//			}
//
//			// Handle rent-based filtering
//			if (filterRequest.getMinRent() != null && filterRequest.getMaxRent() != null) {
//				criteria = criteria.and("rentValue").gte(filterRequest.getMinRent()).lte(filterRequest.getMaxRent());
//			} else if (filterRequest.getMinRent() != null) {
//				criteria = criteria.and("rentValue").gte(filterRequest.getMinRent());
//			} else if (filterRequest.getMaxRent() != null) {
//				criteria = criteria.and("rentValue").lte(filterRequest.getMaxRent());
//			}
//
//			// Create Pageable object
//			Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "listedDate"));
//
//			// Create Query object with criteria and pagination
//			Query query = Query.query(criteria).with(pageable);
//
//			// Execute the query to get properties
//			List<PropertyDetails> properties = mongoTemplate.find(query, PropertyDetails.class);
//
//			// Additional processing for saved and contacted properties
//			if (user != null) {
//				Set<String> savedPropertyIds = new HashSet<>(user.getSavedPropertyIds());
//				Set<String> contactedPropertyIds = new HashSet<>(user.getContactedPropertyIds());
//
//				properties.forEach(property -> {
//					if (savedPropertyIds.contains(property.getId())) {
//						property.setIsSaved(1);
//					} else {
//						property.setIsSaved(0);
//					}
//
//					if (!contactedPropertyIds.contains(property.getId())) {
//						property.setNumber("0");
//					}
//				});
//			}
//
//			// Count total number of items matching the criteria
//			Query countQuery = Query.query(criteria);
//			long totalItems = mongoTemplate.count(countQuery, PropertyDetails.class);
//
//			return new PageImpl<>(properties, pageable, totalItems);
//
//		} catch (Exception e) {
//			System.out.println("An error occurred while fetching properties: " + e.getMessage());
//			e.printStackTrace();
//			throw e;
//		}
//	}

	public Page<PropertyDetails> filterPropertiesSharingFlat(PropertyFilter filterRequest, int page, int size) {
		try {
			User user = null;
			if (filterRequest.getUserId() != null && !filterRequest.getUserId().isEmpty()) {
				user = userRepository.findById(filterRequest.getUserId()).orElse(null);
				if (user == null || user.getIsPremium() == 0) {
					System.out.print("this method is running");

					return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
				}
			}

			// Initialize criteria
			Criteria criteria = new Criteria();

			ZoneOffset zoneOffset = ZoneOffset.UTC;

			// Handle status-based filtering
			if (filterRequest.getStatus() != null) {
				switch (filterRequest.getStatus().toLowerCase()) {
				case "active":
					criteria = criteria.and("isDeleted").is(0);
					break;
				case "deleted":
					criteria = criteria.and("isDeleted").is(1);
					break;
				case "yesterday":
					ZonedDateTime yesterday = ZonedDateTime.now(zoneOffset).minusDays(1);
					LocalDateTime startOfYesterday = yesterday.toLocalDate().atStartOfDay();
					LocalDateTime endOfYesterday = startOfYesterday.plusDays(1);
					criteria = criteria.and("listedDate").gte(startOfYesterday).lt(endOfYesterday);
					break;
				case "today":
					ZonedDateTime today = ZonedDateTime.now(zoneOffset);
					LocalDateTime startOfToday = today.toLocalDate().atStartOfDay();
					LocalDateTime endOfToday = startOfToday.plusDays(1);
					criteria = criteria.and("listedDate").gte(startOfToday).lt(endOfToday);
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
			} else {
				// Exclude future-dated properties if no 'listedOn' filter is provided
				LocalDateTime now = LocalDateTime.now(zoneOffset);
				criteria = criteria.and("listedDate").lte(now);
			}

			// Handle type-based filtering
			if (filterRequest.getType() != null && !filterRequest.getType().isEmpty()) {
				criteria = criteria.and("type").regex("^" + Pattern.quote(filterRequest.getType()), "i");
			}

			if (filterRequest.getSearch() != null && !filterRequest.getSearch().isEmpty()) {
				String searchPattern = ".*" + filterRequest.getSearch().replace(" ", ".*") + ".*";
				criteria = criteria.orOperator(Criteria.where("title").regex(searchPattern, "i"));
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

			// Additional processing for saved and contacted properties
			if (user != null) {
				Set<String> savedPropertyIds = new HashSet<>(user.getSavedPropertyIds());
				Set<String> contactedPropertyIds = new HashSet<>(user.getContactedPropertyIds());

				// Fetch UserPropertyStatus using the corrected repository method
				List<UserPropertyStatus> userPropertyStatuses = userPropertyStatusRepository.findByUserIdAndPropIdIn(
						user.getId(), properties.stream().map(PropertyDetails::getId).collect(Collectors.toList()));

				Map<String, String> propertyStatusMap = userPropertyStatuses.stream()
						.collect(Collectors.toMap(UserPropertyStatus::getPropId, UserPropertyStatus::getStatus));

				properties.forEach(property -> {
					boolean isSpecificUser = "67128ea2d6da233a1af20f30".equals(filterRequest.getUserId()); // replace
																											// with the
					// specific user ID

					if (savedPropertyIds.contains(property.getId())) {
						property.setIsSaved(1);
					} else {
						property.setIsSaved(0);
					}

					if (contactedPropertyIds.contains(property.getId())) {
						// Generate a random realistic phone number
						String randomPhoneNumber = "9" + (100000000 + new Random().nextInt(900000000));
						property.setNumber(isSpecificUser ? randomPhoneNumber : property.getNumber());
						property.setName(property.getName()); // Optional placeholder for contacted name
					} else {
						property.setNumber("0");
						property.setName("0");
					}

					property.setStatus(propertyStatusMap.getOrDefault(property.getId(), "Active"));

				});
			}

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
public Page<PropertyDetails> filterPropertiesSharingFlatV2(PropertyFilter filterRequest, int page, int size) {
    try {
        User user = null;

        if (filterRequest.getUserId() != null && !filterRequest.getUserId().isEmpty()) {
            user = userRepository.findById(filterRequest.getUserId()).orElse(null);
            if (user == null || user.getIsPremium() == 0) {
                System.out.print("this method is running");
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
            }
        }

        Criteria criteria = new Criteria();
        ZoneOffset zoneOffset = ZoneOffset.UTC;

        // Status filters
        if (filterRequest.getStatus() != null) {
            switch (filterRequest.getStatus().toLowerCase()) {
                case "active":
                    criteria = criteria.and("isDeleted").is(0);
                    break;
                case "deleted":
                    criteria = criteria.and("isDeleted").is(1);
                    break;
                case "yesterday":
                    ZonedDateTime yesterday = ZonedDateTime.now(zoneOffset).minusDays(1);
                    LocalDateTime startOfYesterday = yesterday.toLocalDate().atStartOfDay();
                    LocalDateTime endOfYesterday = startOfYesterday.plusDays(1);
                    criteria = criteria.and("listedDate").gte(startOfYesterday).lt(endOfYesterday);
                    break;
                case "today":
                    ZonedDateTime today = ZonedDateTime.now(zoneOffset);
                    LocalDateTime startOfToday = today.toLocalDate().atStartOfDay();
                    LocalDateTime endOfToday = startOfToday.plusDays(1);
                    criteria = criteria.and("listedDate").gte(startOfToday).lt(endOfToday);
                    break;
            }
        }

        // ListedOn filter
        if (filterRequest.getListedOn() != null && !filterRequest.getListedOn().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate listedOnDate = LocalDate.parse(filterRequest.getListedOn(), formatter);
            LocalDateTime startOfDay = listedOnDate.atStartOfDay();
            LocalDateTime endOfDay = listedOnDate.atTime(LocalTime.MAX);
            criteria = criteria.and("listedDate").gte(startOfDay).lte(endOfDay);
        } else {
            LocalDateTime now = LocalDateTime.now(zoneOffset);
            criteria = criteria.and("listedDate").lte(now);
        }

        // Type filter
        if (filterRequest.getType() != null && !filterRequest.getType().isEmpty()) {
            criteria = criteria.and("type").regex("^" + Pattern.quote(filterRequest.getType()), "i");
        }

        // Search filter
        if (filterRequest.getSearch() != null && !filterRequest.getSearch().isEmpty()) {
            String searchPattern = ".*" + filterRequest.getSearch().replace(" ", ".*") + ".*";
            criteria = criteria.orOperator(Criteria.where("title").regex(searchPattern, "i"));
        }

        // ListedBy filter
        if (filterRequest.getListedBy() != null && !filterRequest.getListedBy().isEmpty()) {
            if (filterRequest.getListedBy().equalsIgnoreCase("Agent")) {
                criteria = criteria.and("userType").regex("^Agent$", "i");
            } else {
                criteria = criteria.and("userType").in("Owner", "Builder");
            }
        }

        List<Criteria> orCriteriaList = new ArrayList<>();

        // Area filter
        if (filterRequest.getAreas() != null && !filterRequest.getAreas().isEmpty()) {
            List<Criteria> areaCriteria = filterRequest.getAreas().stream()
                    .map(area -> Criteria.where("area").regex("^" + Pattern.quote(area) + "$", "i"))
                    .collect(Collectors.toList());
            orCriteriaList.add(new Criteria().orOperator(areaCriteria.toArray(new Criteria[0])));
        }

        // BHK filter
        if (filterRequest.getBhks() != null && !filterRequest.getBhks().isEmpty()) {
            List<Criteria> bhkCriteria = filterRequest.getBhks().stream()
                    .map(bhk -> Criteria.where("bhk").regex("^" + Pattern.quote(bhk) + "$", "i"))
                    .collect(Collectors.toList());
            orCriteriaList.add(new Criteria().orOperator(bhkCriteria.toArray(new Criteria[0])));
        }

        // SubType filter
        if (filterRequest.getSubType() != null && !filterRequest.getSubType().isEmpty()) {
            List<Criteria> subtypeCriteria = filterRequest.getSubType().stream()
                    .map(subtype -> Criteria.where("unitType").regex("^" + Pattern.quote(subtype) + "$", "i"))
                    .collect(Collectors.toList());
            orCriteriaList.add(new Criteria().orOperator(subtypeCriteria.toArray(new Criteria[0])));
        }

        // FurnishedTypes filter
        if (filterRequest.getFurnishedTypes() != null && !filterRequest.getFurnishedTypes().isEmpty()) {
            List<Criteria> furnishedTypeCriteria = filterRequest.getFurnishedTypes().stream()
                    .map(type -> Criteria.where("furnishedType").regex("^" + Pattern.quote(type) + "$", "i"))
                    .collect(Collectors.toList());
            orCriteriaList.add(new Criteria().orOperator(furnishedTypeCriteria.toArray(new Criteria[0])));
        }

        // Apply combined $or
        if (!orCriteriaList.isEmpty()) {
            criteria = criteria.andOperator(orCriteriaList.toArray(new Criteria[0]));
        }

        // SqFt filter
        if (filterRequest.getMinsqFt() != null && filterRequest.getMaxsqFt() != null) {
            if (filterRequest.getMinsqFt() != 0 && filterRequest.getMaxsqFt() != 10000) {
                criteria = criteria.and("sqFt").gte(filterRequest.getMinsqFt()).lte(filterRequest.getMaxsqFt());
            } else if (filterRequest.getMinsqFt() != 0) {
                criteria = criteria.and("sqFt").gte(filterRequest.getMinsqFt());
            } else if (filterRequest.getMaxsqFt() != 0) {
                criteria = criteria.and("sqFt").lte(filterRequest.getMaxsqFt());
            }
        }

        // Rent filter
        if (filterRequest.getMinRent() != 0 && filterRequest.getMaxRent() != 0) {
            criteria = criteria.and("rentValue").gte(filterRequest.getMinRent()).lte(filterRequest.getMaxRent());
        } else if (filterRequest.getMinRent() != 0) {
            criteria = criteria.and("rentValue").gte(filterRequest.getMinRent());
        } else if (filterRequest.getMaxRent() != 0) {
            criteria = criteria.and("rentValue").lte(filterRequest.getMaxRent());
        }
	Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "listedDate"));
        Query query = Query.query(criteria).with(pageable);
		
        List<PropertyDetails> properties = mongoTemplate.find(query, PropertyDetails.class);
		System.out.println("An error occurred while fetching properties: " + properties);

        if (user != null) {
            Set<String> savedPropertyIds = new HashSet<>(user.getSavedPropertyIds());
            Set<String> contactedPropertyIds = new HashSet<>(user.getContactedPropertyIds());

            List<UserPropertyStatus> userPropertyStatuses = userPropertyStatusRepository.findByUserIdAndPropIdIn(
                    user.getId(), properties.stream().map(PropertyDetails::getId).collect(Collectors.toList()));

            Map<String, String> propertyStatusMap = userPropertyStatuses.stream()
                    .collect(Collectors.toMap(UserPropertyStatus::getPropId, UserPropertyStatus::getStatus));

            List<UserPropertyRemark> remarks = userPropertyRemarkRepository.findByUserIdAndPropIdIn(user.getId(),
                    properties.stream().map(PropertyDetails::getId).collect(Collectors.toList()));

            Map<String, String> propertyRemarksMap = remarks.stream()
                    .collect(Collectors.toMap(UserPropertyRemark::getPropId, UserPropertyRemark::getRemark));
			List<String> excludedStatuses = Arrays.asList("Sell out", "Rent out", "Broker", "Duplicate");

			Iterator<PropertyDetails> iterator = properties.iterator();
		while (iterator.hasNext()) {
			PropertyDetails property = iterator.next();
			boolean isSpecificUser = "67128ea2d6da233a1af20f30".equals(filterRequest.getUserId());

			property.setIsSaved(savedPropertyIds.contains(property.getId()) ? 1 : 0);

			if (contactedPropertyIds.contains(property.getId())) {
				String randomPhoneNumber = "9" + (100000000 + new Random().nextInt(900000000));
				property.setNumber(isSpecificUser ? randomPhoneNumber : property.getNumber());
				property.setName(property.getName()); // Optional
			} else {
				property.setNumber("0");
				property.setName("0");
			}

			String status = propertyStatusMap.getOrDefault(property.getId(), "Active");
			property.setStatus(status);
			property.setRemark(propertyRemarksMap.getOrDefault(property.getId(), null));

			// ❌ Remove if status is in the excluded list (case-insensitive)
			if (excludedStatuses.stream().anyMatch(ex -> ex.equalsIgnoreCase(status))) {
				iterator.remove();
			}
		}

           
        }



        long totalItems = mongoTemplate.count(Query.query(criteria), PropertyDetails.class);
		System.out.println("properties: " +totalItems);

		return new PageImpl<>(properties, pageable, totalItems);

    } catch (Exception e) {
        System.out.println("An error occurred while fetching properties: " + e.getMessage());
        e.printStackTrace();
        throw e;
    }
}
public List<PropertyDetails> findPropertiesWithDuplicateNumbers() {
		return propertyDetailsRepository.findPropertiesWithDuplicateNumbers();
	}

//	public Map<String, Long> getPropertyCountsByStatusAndType() {
//		Map<String, Long> counts = new HashMap<>();
//
//		try {
//
//			counts.put("todayResidentialRental", getRandomNumberInRange("Residential Rent", "today"));
//			counts.put("todayResidentialSell", getRandomNumberInRange("Residential Sell", "today"));
//			counts.put("todayCommercialRent", getRandomNumberInRange("Commercial Rent", "today"));
//			counts.put("todayCommercialSell", getRandomNumberInRange("Commercial Sell", "today"));
//
//			counts.put("activeResidentialRental", getRandomNumberInRange("Residential Rent", "active"));
//			counts.put("activeResidentialSell", getRandomNumberInRange("Residential Sell", "active"));
//			counts.put("activeCommercialRent", getRandomNumberInRange("Commercial Rent", "active"));
//			counts.put("activeCommercialSell", getRandomNumberInRange("Commercial Sell", "active"));
//
//			counts.put("deletedResidentialRental", getRandomNumberInRange("Residential Rent", "deleted"));
//			counts.put("deletedResidentialSell", getRandomNumberInRange("Residential Sell", "deleted"));
//			counts.put("deletedCommercialRent", getRandomNumberInRange("Commercial Rent", "deleted"));
//			counts.put("deletedCommercialSell", getRandomNumberInRange("Commercial Sell", "deleted"));
//
//			// Calculate total active properties
//			long totalActiveProperties = counts.getOrDefault("activeResidentialRental", 0L)
//					+ counts.getOrDefault("activeResidentialSell", 0L) + counts.getOrDefault("activeCommercialRent", 0L)
//					+ counts.getOrDefault("activeCommercialSell", 0L);
//			counts.put("totalActiveProperties", totalActiveProperties);
//
//			// Calculate total deleted properties
//			long totalDeletedProperties = counts.getOrDefault("deletedResidentialRental", 0L)
//					+ counts.getOrDefault("deletedResidentialSell", 0L)
//					+ counts.getOrDefault("deletedCommercialRent", 0L)
//					+ counts.getOrDefault("deletedCommercialSell", 0L);
//			counts.put("totalDeletedProperties", totalDeletedProperties);
//
//		} catch (Exception e) {
//			System.out.println("An error occurred while fetching property counts: " + e.getMessage());
//			e.printStackTrace();
//		}
//
//		return counts;
//	}

//	private long getRandomNumberInRange(int min, int max) {
//		return ThreadLocalRandom.current().nextLong(min, max + 1);
//	}

	// Helper method to get a random number from the defined range for a specific
	// type and status
//	private long getRandomNumberInRange(String type, String status) {
////		Optional<Numbers> rangeOpt = numbersRepository.findByType(type);
//		Optional<Numbers> rangeOpt = numbersRepository.findByTypeAndStatus(type, status);
//
//		if (rangeOpt.isPresent()) {
//			Numbers range = rangeOpt.get();
//			return ThreadLocalRandom.current().nextLong(range.getStartNumber(), range.getEndNumber() + 1);
//		}
//		return 0;
//	}

	public int deleteByCreatedDate(String createdDate, String type) {
		try {
			DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
			LocalDate date = LocalDate.parse(createdDate, dateFormatter);

			// Define start and end of the day
			LocalDateTime startOfDay = date.atStartOfDay();
			LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

			// Build the query
			Query query = new Query();
			query.addCriteria(Criteria.where("createdOn").gte(startOfDay).lte(endOfDay));

			// Add 'type' criteria only if it's not null or empty
			if (type != null && !type.trim().isEmpty()) {
				query.addCriteria(Criteria.where("type").is(type));
			}

			// Perform the delete operation
			DeleteResult result = mongoTemplate.remove(query, PropertyDetails.class);

			return (int) result.getDeletedCount();
		} catch (Exception e) {
			throw new RuntimeException("Error deleting records created on " + createdDate, e);
		}
	}

	private long countPropertiesByCriteria(String type, int isDeleted, LocalDateTime startDate, LocalDateTime endDate) {
		Criteria criteria = Criteria.where("type").is(type).and("isDeleted").is(isDeleted);
		if (startDate != null && endDate != null) {
			criteria = criteria.and("createdOn").gte(startDate).lt(endDate);
		}

		Query query = Query.query(criteria);
		return mongoTemplate.count(query, PropertyDetails.class);
	}

	public Map<String, Long> getPropertyCountsByStatusAndType() {
		Map<String, Long> counts = new HashMap<>();

		try {
			// Define today's date range based on server time
			ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
			LocalDateTime startOfTodayLocal = now.toLocalDate().atStartOfDay();
			LocalDateTime endOfTodayLocal = startOfTodayLocal.plusDays(1);

			System.out.println("Start of Today: " + startOfTodayLocal);
			System.out.println("End of Today: " + endOfTodayLocal);

			// Define criteria for different statuses and types
			counts.put("todayResidentialRental",
					countPropertiesByCriteria("Residential Rent", 0, startOfTodayLocal, endOfTodayLocal));
			counts.put("todayResidentialSell",
					countPropertiesByCriteria("Residential Sell", 0, startOfTodayLocal, endOfTodayLocal));
			counts.put("todayCommercialRent",
					countPropertiesByCriteria("Commercial Rent", 0, startOfTodayLocal, endOfTodayLocal));
			counts.put("todayCommercialSell",
					countPropertiesByCriteria("Commercial Sell", 0, startOfTodayLocal, endOfTodayLocal));

			counts.put("activeResidentialRental", countPropertiesByCriteria("Residential Rent", 0, null, null));
			counts.put("activeResidentialSell", countPropertiesByCriteria("Residential Sell", 0, null, null));
			counts.put("activeCommercialRent", countPropertiesByCriteria("Commercial Rent", 0, null, null));
			counts.put("activeCommercialSell", countPropertiesByCriteria("Commercial Sell", 0, null, null));

//			List<String> listedByTypes1 = Arrays.asList("Agent");
//
//			counts.put("agentResidentialRental",
//					countPropertiesByCriteria("Residential Rent", 0, null, null, listedByTypes1));
//			counts.put("agentResidentialSell",
//					countPropertiesByCriteria("Residential Sell", 0, null, null, listedByTypes1));
//			counts.put("agentCommercialRent",
//					countPropertiesByCriteria("Commercial Rent", 0, null, null, listedByTypes1));
//			counts.put("agentCommercialSell",
//					countPropertiesByCriteria("Commercial Sell", 0, null, null, listedByTypes1));

			// Calculate total active properties
			long totalActiveProperties = counts.getOrDefault("activeResidentialRental", 0L)
					+ counts.getOrDefault("activeResidentialSell", 0L) + counts.getOrDefault("activeCommercialRent", 0L)
					+ counts.getOrDefault("activeCommercialSell", 0L);
			counts.put("totalActiveProperties", totalActiveProperties);

			// Calculate total deleted properties
			long totalDeletedProperties = counts.getOrDefault("agentResidentialRental", 0L)
					+ counts.getOrDefault("agentResidentialSell", 0L) + counts.getOrDefault("agentCommercialRent", 0L)
					+ counts.getOrDefault("agentCommercialSell", 0L);
			counts.put("totalagentProperties", totalDeletedProperties);

		} catch (Exception e) {
			System.out.println("An error occurred while fetching property counts: " + e.getMessage());
			e.printStackTrace();
		}

		return counts;
	}

	public int updateFurnishedType() {
		List<PropertyDetails> properties = propertyDetailsRepository.findByFurnishedType("Semi furnished");
		properties.forEach(property -> property.setFurnishedType("Semi-Furnished"));
		propertyDetailsRepository.saveAll(properties);
		return properties.size();
	}

	public PropertyDetails contactPropertyToUser(String userId, String propertyId) {
		if (userId == null || userId.trim().isEmpty() || propertyId == null || propertyId.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid user ID or property ID!");
		}

		Optional<User> userOptional;
		try {
			userOptional = userRepository.findById(userId);
		} catch (Exception e) {
			throw new RuntimeException("Error while fetching user data.", e);
		}

		if (!userOptional.isPresent()) {
			throw new RuntimeException("User not found!");
		}

		User user = userOptional.get();

		if (user.getIsPremium() != 1) {
			throw new RuntimeException("Please Buy Premium");
		}

		if (user.getContactedPropertyIds() == null) {
			user.setContactedPropertyIds(new ArrayList<>());
		}

		PropertyDetails propertyDetails;
		try {
			propertyDetails = propertyDetailsRepository.findById(propertyId).orElse(null);
		} catch (Exception e) {
			throw new RuntimeException("Error fetching the property details.", e);
		}

		if (propertyDetails == null) {
			throw new RuntimeException("Property not found!");
		}

		if (!user.getContactedPropertyIds().contains(propertyId)) {
			try {
				user.getContactedPropertyIds().add(propertyId);
				userRepository.save(user);
			} catch (Exception e) {
				throw new RuntimeException("Error while updating user data.", e);
			}
		}

		return propertyDetails;
	}

	public PropertyDetails contactPropertyToUserV2(String userId, String propertyId) {
		if (userId == null || userId.trim().isEmpty() || propertyId == null || propertyId.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid user ID or property ID!");
		}

		Optional<User> userOptional;
		try {
			userOptional = userRepository.findById(userId);
		} catch (Exception e) {
			throw new RuntimeException("Error while fetching user data.", e);
		}

		if (!userOptional.isPresent()) {
			throw new RuntimeException("User not found!");
		}

		User user = userOptional.get();

		if (user.getIsPremium() != 1) {
			throw new RuntimeException("Please Buy Premium");
		}

		// If the user has already contacted this property, don't deduct credits/limit
		if (user.getContactedPropertyIds() != null && user.getContactedPropertyIds().contains(propertyId)) {
			return propertyDetailsRepository.findById(propertyId)
					.orElseThrow(() -> new RuntimeException("Property not found!"));
		}

		if (user.getLimit() <= 0) {
			throw new RuntimeException("You have reached today's contact limit.");
		}

		if (user.getContactedPropertyIds() == null) {
			user.setContactedPropertyIds(new ArrayList<>());
		}

		PropertyDetails propertyDetails;
		try {
			propertyDetails = propertyDetailsRepository.findById(propertyId).orElse(null);
		} catch (Exception e) {
			throw new RuntimeException("Error fetching the property details.", e);
		}

		if (propertyDetails == null) {
			throw new RuntimeException("Property not found!");
		}

		if (!user.getContactedPropertyIds().contains(propertyId)) {
			try {
				user.getContactedPropertyIds().add(propertyId);
				user.setLimit(user.getLimit() - 1);
				user.setTotalCount(user.getTotalCount() + 1);
				userRepository.save(user);
			} catch (Exception e) {
				throw new RuntimeException("Error while updating user data.", e);
			}
		}
		UserContactedPropertyHistory contactHistory = userContactedPropertyHistoryRepository.findByUserId(userId);
		if (contactHistory == null) {
			contactHistory = new UserContactedPropertyHistory();
			contactHistory.setUserId(userId);
			contactHistory.setCreatedOn(LocalDateTime.now());
		}

		if (contactHistory.getContactHistory() == null) {
			contactHistory.setContactHistory(new ArrayList<>());
		}
		UserContactedPropertyHistory.ContactHistoryEntry entry = new UserContactedPropertyHistory.ContactHistoryEntry();
		entry.setPropertyId(propertyId);
		entry.setContactedDate(LocalDateTime.now());
		contactHistory.getContactHistory().add(entry);

		userContactedPropertyHistoryRepository.save(contactHistory);

		return propertyDetails;
	}

	public UserPropertyStatus updatePropertyStatus(String propertyId, String newStatus, String changedByUserId) {

		// Check if this user has already changed the status of this property
		Optional<UserPropertyStatus> existingStatus = userPropertyStatusRepository
				.findByUserIdAndPropId(changedByUserId, propertyId);

		int isVerified = (newStatus.equalsIgnoreCase("Rent out") || newStatus.equalsIgnoreCase("Sell out"))
				|| newStatus.equalsIgnoreCase("Broker") || newStatus.equalsIgnoreCase("Duplicate")
				|| newStatus.equalsIgnoreCase("Data Mismatch") ? 0 : 1;

		if (existingStatus.isPresent()) {
			// Update the existing status
			UserPropertyStatus userPropertyStatus = existingStatus.get();
			userPropertyStatus.setStatus(newStatus);
			userPropertyStatus.setIsVerified(isVerified); // Update isVerified based on the status
			return userPropertyStatusRepository.save(userPropertyStatus); // Return updated entity

		} else {
			// Create a new status entry for this user and property
			UserPropertyStatus userPropertyStatus = new UserPropertyStatus();
			userPropertyStatus.setUserId(changedByUserId);
			userPropertyStatus.setPropId(propertyId);
			userPropertyStatus.setStatus(newStatus);
			userPropertyStatus.setIsVerified(isVerified); // Set isVerified based on the status
			userPropertyStatus.setCreatedOn(LocalDateTime.now());
			return userPropertyStatusRepository.save(userPropertyStatus); // Return newly created entity

		}
	}

	public Map<String, Object> getContactedPropertiesV2(String userId, int page, int size) {
		try {
			// Fetch the user by ID
			Optional<User> userOptional = userRepository.findById(userId);

			// Check if user is present
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				List<String> contactedPropertyIds = user.getContactedPropertyIds();
				List<String> savedPropertyIds = user.getSavedPropertyIds(); // Fetch saved properties

				// Check if contactedPropertyIds is null or empty
				if (contactedPropertyIds == null || contactedPropertyIds.isEmpty()) {
					// Return an empty response map
					Map<String, Object> emptyResponse = new HashMap<>();
					emptyResponse.put("properties", Collections.emptyList());
					emptyResponse.put("currentPage", page);
					emptyResponse.put("totalItems", 0);
					emptyResponse.put("totalPages", 0);
					return emptyResponse;
				}

				// Apply pagination logic (sublist to limit the result)
				int startIndex = page * size;
				int endIndex = Math.min(startIndex + size, contactedPropertyIds.size());

				if (startIndex >= contactedPropertyIds.size()) {
					// Return empty list if the start index is out of bounds
					Map<String, Object> emptyResponse = new HashMap<>();
					emptyResponse.put("properties", Collections.emptyList());
					emptyResponse.put("currentPage", page);
					emptyResponse.put("totalItems", contactedPropertyIds.size());
					emptyResponse.put("totalPages", (int) Math.ceil((double) contactedPropertyIds.size() / size));
					return emptyResponse;
				}

				// Get the paginated list of property IDs
				List<String> paginatedIds = contactedPropertyIds.subList(startIndex, endIndex);

				// Fetch the properties based on the paginated IDs
				List<PropertyDetails> contactedProperties = propertyDetailsRepository.findAllById(paginatedIds);

				// Set `isSaved` field for each property if it is in the savedPropertyIds list
				List<PropertyDetails> modifiedProperties = contactedProperties.stream().map(property -> {
					if (savedPropertyIds != null && savedPropertyIds.contains(property.getId())) {
						property.setIsSaved(1); // Set isSaved to 1 if the property is saved
					}
					return property;
				}).collect(Collectors.toList());

				// Prepare pagination response
				Map<String, Object> response = new HashMap<>();
				response.put("properties", modifiedProperties);
				response.put("currentPage", page);
				response.put("totalItems", contactedPropertyIds.size());
				response.put("totalPages", (int) Math.ceil((double) contactedPropertyIds.size() / size));

				// Return the response map
				return response;
			} else {
				// If the user is not found, return empty response map
				Map<String, Object> emptyResponse = new HashMap<>();
				emptyResponse.put("properties", Collections.emptyList());
				emptyResponse.put("currentPage", page);
				emptyResponse.put("totalItems", 0);
				emptyResponse.put("totalPages", 0);
				return emptyResponse;
			}
		} catch (Exception e) {
			// Log the error for debugging
			System.err.println("Error fetching contacted properties: " + e.getMessage());
			e.printStackTrace();

			// Return an empty response map
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("properties", Collections.emptyList());
			errorResponse.put("currentPage", page);
			errorResponse.put("totalItems", 0);
			errorResponse.put("totalPages", 0);
			return errorResponse;
		}
	}

	public Page<PropertyDetails> getSavedPropertiesV2(String userId, int page, int size) {
		try {
			// Fetch the user by ID
			Optional<User> userOptional = userRepository.findById(userId);

			if (!userOptional.isPresent()) {
				// Return an empty page if the user is not found
				return new PageImpl<>(Collections.emptyList());
			}

			User user = userOptional.get();
			List<String> savedPropertyIds = user.getSavedPropertyIds();

			if (savedPropertyIds == null || savedPropertyIds.isEmpty()) {
				// Return an empty page if there are no saved properties
				return new PageImpl<>(Collections.emptyList());
			}

			// Fetch all saved properties by their IDs
			List<PropertyDetails> savedProperties = propertyDetailsRepository.findAllById(savedPropertyIds);

			// Get the list of contacted property IDs
			List<String> contactedPropertyIds = user.getContactedPropertyIds();

			// Modify the properties based on whether they were contacted
			List<PropertyDetails> modifiedProperties = savedProperties.stream().map(property -> {
				property.setIsSaved(1); // Set isSaved to 1 for all properties

				if (!contactedPropertyIds.contains(property.getId())) {
					property.setName("0"); // Modify as per your requirements
					property.setNumber("0"); // Modify as per your requirements
				}
				return property;
			}).collect(Collectors.toList());

			// Manually paginate the modifiedProperties list
			int start = Math.min(page * size, modifiedProperties.size());
			int end = Math.min((page + 1) * size, modifiedProperties.size());

			if (start > modifiedProperties.size()) {
				return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), modifiedProperties.size());
			}

			// Create a sublist for the current page
			List<PropertyDetails> paginatedList = modifiedProperties.subList(start, end);

			// Return a paginated result using PageImpl
			return new PageImpl<>(paginatedList, PageRequest.of(page, size), modifiedProperties.size());

		} catch (Exception e) {
			System.err.println("Error fetching saved properties: " + e.getMessage());
			e.printStackTrace();
			return new PageImpl<>(Collections.emptyList());
		}
	}

	public void saveSuggestion(Suggestion suggestion) {
		// Create a new Suggestion object
		Suggestion newSuggestion = new Suggestion();
		newSuggestion.setName(suggestion.getName());
		newSuggestion.setNumber(suggestion.getNumber());
		newSuggestion.setSuggestion(suggestion.getSuggestion());
		newSuggestion.setCreatedOn(LocalDateTime.now()); // Set the creation time
		suggestionRepository.save(newSuggestion);
		// Save the suggestion to the database
	}

	public List<PropertyDetailsWithUserStatus> getUnverifiedPropertyDetailsForAdmin() {
		// Step 1: Fetch unverified property status entries with specified statuses
		List<UserPropertyStatus> unverifiedStatuses = userPropertyStatusRepository
				.findUnverifiedPropertiesWithSpecificStatuses();

		// Step 2: Extract property IDs from the status entries
		List<String> propertyIds = unverifiedStatuses.stream().map(UserPropertyStatus::getPropId)
				.collect(Collectors.toList());

		// Step 3: Fetch property details for the extracted property IDs
		List<PropertyDetails> propertyDetailsList = propertyDetailsRepository.findByIdIn(propertyIds);

		// Step 4: Map property details with their corresponding statuses and IDs
		return propertyDetailsList.stream().map(property -> {
			// Find the status entry for this property ID
			UserPropertyStatus statusEntry = unverifiedStatuses.stream()
					.filter(us -> us.getPropId().equals(property.getId())).findFirst().orElse(null);

			// Extract status and unverifiedStatusesId (if statusEntry is found)
			String status = statusEntry != null ? statusEntry.getStatus() : null;
			String unverifiedStatusesId = statusEntry != null ? statusEntry.getId() : null;

			// Create and return a PropertyDetailsWithUserStatus object
			return new PropertyDetailsWithUserStatus(property, status, unverifiedStatusesId);
		}).collect(Collectors.toList());
	}

	public UserPropertyRemark addOrUpdateRemark(String propId, String userId, String remark) {
		Optional<UserPropertyRemark> existingRemark = userPropertyRemarkRepository.findByPropIdAndUserId(propId,
				userId);

		if (existingRemark.isPresent()) {

			// Update existing remark
			UserPropertyRemark userPropertyRemark = existingRemark.get();
			userPropertyRemark.setRemark(remark); // Update remark text
			return userPropertyRemarkRepository.save(userPropertyRemark);
		} else {
			// Create a new remark

			UserPropertyRemark userPropertyRemark = new UserPropertyRemark();
			userPropertyRemark.setPropId(propId);
			userPropertyRemark.setUserId(userId);
			userPropertyRemark.setRemark(remark);
			return userPropertyRemarkRepository.save(userPropertyRemark);
		}
	}
}
