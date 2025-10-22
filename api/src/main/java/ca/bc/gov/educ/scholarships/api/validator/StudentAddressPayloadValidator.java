package ca.bc.gov.educ.scholarships.api.validator;


import ca.bc.gov.educ.scholarships.api.model.v1.CountryCodeEntity;
import ca.bc.gov.educ.scholarships.api.model.v1.ProvinceCodeEntity;
import ca.bc.gov.educ.scholarships.api.service.v1.CodeTableService;
import ca.bc.gov.educ.scholarships.api.struct.v1.StudentAddress;
import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class StudentAddressPayloadValidator {
  
  public static final String PROVINCE_CODE = "provinceCode";

  public static final String COUNTRY_CODE = "countryCode";

  @Getter(AccessLevel.PRIVATE)
  private final CodeTableService codeTableService;

  @Autowired
  public StudentAddressPayloadValidator(final CodeTableService codeTableService) {
    this.codeTableService = codeTableService;
  }

  public List<FieldError> validatePayload(StudentAddress address) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    validateCountryCode(address, apiValidationErrors);
    validateProvinceCode(address, apiValidationErrors);
    return apiValidationErrors;
  }

  protected void validateProvinceCode(StudentAddress address, List<FieldError> apiValidationErrors) {
    if (StringUtils.isNotBlank(address.getCountryCode()) && address.getCountryCode().equals("CA")) {
      Optional<ProvinceCodeEntity> provinceCodeEntity = codeTableService.getProvinceCode(address.getProvinceStateCode());
      if (provinceCodeEntity.isEmpty()) {
        apiValidationErrors.add(createFieldError(PROVINCE_CODE, address.getProvinceStateCode(), "Invalid canadian province code."));
      } else if (provinceCodeEntity.get().getEffectiveDate() != null && provinceCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
        apiValidationErrors.add(createFieldError(PROVINCE_CODE, address.getProvinceStateCode(), "Province code provided is not yet effective."));
      } else if (provinceCodeEntity.get().getExpiryDate() != null && provinceCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
        apiValidationErrors.add(createFieldError(PROVINCE_CODE, address.getProvinceStateCode(), "Province code provided has expired."));
      }
    }else if(StringUtils.isBlank(address.getProvinceStateCode())){
      apiValidationErrors.add(createFieldError(PROVINCE_CODE, address.getProvinceStateCode(), "Empty province/state code."));
    }
  }

  protected void validateCountryCode(StudentAddress address, List<FieldError> apiValidationErrors) {
    if (StringUtils.isNotBlank(address.getCountryCode())) {
      Optional<CountryCodeEntity> countryCodeEntity = codeTableService.getCountryCode(address.getCountryCode());
      if (countryCodeEntity.isEmpty()) {
        apiValidationErrors.add(createFieldError(COUNTRY_CODE, address.getCountryCode(), "Invalid country code."));
      } else if (countryCodeEntity.get().getEffectiveDate() != null && countryCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
        apiValidationErrors.add(createFieldError(COUNTRY_CODE, address.getCountryCode(), "Country code provided is not yet effective."));
      } else if (countryCodeEntity.get().getExpiryDate() != null && countryCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
        apiValidationErrors.add(createFieldError(COUNTRY_CODE, address.getCountryCode(), "Country code provided has expired."));
      }
    }else{
      apiValidationErrors.add(createFieldError(COUNTRY_CODE, address.getCountryCode(), "Empty country code."));
    }
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("address", fieldName, rejectedValue, false, null, null, message);
  }
}
