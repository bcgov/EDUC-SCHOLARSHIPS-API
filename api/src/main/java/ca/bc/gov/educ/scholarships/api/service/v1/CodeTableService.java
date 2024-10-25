package ca.bc.gov.educ.scholarships.api.service.v1;

import ca.bc.gov.educ.scholarships.api.model.v1.CitizenshipCodeEntity;
import ca.bc.gov.educ.scholarships.api.repository.v1.CitizenshipCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CodeTableService {
  private final CitizenshipCodeRepository citizenshipCodeRepository;

  @Cacheable("citizenshipCodes")
  public List<CitizenshipCodeEntity> getAllCitizenshipCodes() {
    return citizenshipCodeRepository.findAll();
  }
}
