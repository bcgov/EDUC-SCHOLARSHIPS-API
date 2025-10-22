package ca.bc.gov.educ.scholarships.api.service.v1;

import ca.bc.gov.educ.scholarships.api.model.v1.CitizenshipCodeEntity;
import ca.bc.gov.educ.scholarships.api.model.v1.CountryCodeEntity;
import ca.bc.gov.educ.scholarships.api.model.v1.ProvinceCodeEntity;
import ca.bc.gov.educ.scholarships.api.repository.v1.CitizenshipCodeRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.CountryCodeRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.ProvinceCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CodeTableService {
  private final CitizenshipCodeRepository citizenshipCodeRepository;
  private final CountryCodeRepository countryCodeRepository;
  private final ProvinceCodeRepository provinceCodeRepository;

  @Cacheable("citizenshipCodes")
  public List<CitizenshipCodeEntity> getAllCitizenshipCodes() {
    return citizenshipCodeRepository.findAll();
  }

  @Cacheable("countryCodes")
  public List<CountryCodeEntity> getAllCountryCodes() {
    return countryCodeRepository.findAll();
  }

  @Cacheable("provinceCodes")
  public List<ProvinceCodeEntity> getAllCanadianProvinceCodes() {
    return provinceCodeRepository.findAll();
  }

  public Optional<ProvinceCodeEntity> getProvinceCode(String provinceCode) {
    return provinceCodeRepository.findById(provinceCode);
  }

  public Optional<CountryCodeEntity> getCountryCode(String countryCode) {
    return countryCodeRepository.findById(countryCode);
  }
}
