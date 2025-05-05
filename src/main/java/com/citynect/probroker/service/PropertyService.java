package com.citynect.probroker.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.citynect.probroker.entities.PropertyDetails;
import com.citynect.probroker.entities.PropertyDetailsWithUserStatus;
import com.citynect.probroker.entities.PropertyFilter;
import com.citynect.probroker.entities.Suggestion;
import com.citynect.probroker.entities.UserPropertyRemark;
import com.citynect.probroker.entities.UserPropertyStatus;

public interface PropertyService {

	Map<String, Object> uploadProperties(MultipartFile file);

	Page<PropertyDetails> filterPropertiesSharingFlat(PropertyFilter filterRequest, int page, int size);

	Map<String, Long> getPropertyCountsByStatusAndType();

	int deleteByCreatedDate(String createdDate, String type);

	int updateFurnishedType();

	PropertyDetails contactPropertyToUser(String userId, String propId);

	UserPropertyStatus updatePropertyStatus(String id, String newStatus, String userId);

	Map<String, Object> getContactedPropertiesV2(String userId, int page, int size);

	Page<PropertyDetails> getSavedPropertiesV2(String userId, int page, int size);

	PropertyDetails contactPropertyToUserV2(String userId, String propId);

	void saveSuggestion(Suggestion suggestion);

	List<PropertyDetailsWithUserStatus> getUnverifiedPropertyDetailsForAdmin();

	List<PropertyDetails> findPropertiesWithDuplicateNumbers();

	UserPropertyRemark addOrUpdateRemark(String id, String userId, String remark);

	Page<PropertyDetails> filterPropertiesSharingFlatV2(PropertyFilter filterRequest, int page, int size);

	Map<String, Object> uploadPropertiesV2(MultipartFile file);

}
