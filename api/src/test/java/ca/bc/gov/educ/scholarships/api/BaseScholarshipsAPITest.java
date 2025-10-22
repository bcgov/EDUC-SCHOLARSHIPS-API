package ca.bc.gov.educ.scholarships.api;

import ca.bc.gov.educ.scholarships.api.model.v1.CitizenshipCodeEntity;
import ca.bc.gov.educ.scholarships.api.model.v1.CountryCodeEntity;
import ca.bc.gov.educ.scholarships.api.model.v1.ProvinceCodeEntity;
import ca.bc.gov.educ.scholarships.api.model.v1.StudentAddressEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest(classes = {ScholarshipsApiApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseScholarshipsAPITest {

  @BeforeEach
  public void before() {

  }

  @AfterEach
  public void resetState() {

  }

  public CitizenshipCodeEntity createCitizenshipCodeEntity() {
    return CitizenshipCodeEntity.builder()
            .citizenshipCode("C")
            .label("Canadian")
            .description("Canadian Citizenship")
            .displayOrder(1)
            .createUser("ABC")
            .updateUser("ABC")
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .effectiveDate(LocalDateTime.now().minusWeeks(2))
            .expiryDate(LocalDateTime.now().plusMonths(2))
            .build();
  }

  public StudentAddressEntity createStudentAddressData() {
    return StudentAddressEntity.builder().addressLine1("Line 1").city("City").provinceStateCode("BC").countryCode("CA").postalZip("V1V1V2").studentID(UUID.randomUUID())
            .createUser("ABC").updateUser("ABC").build();
  }

  public ProvinceCodeEntity createProvinceCodeData() {
    return ProvinceCodeEntity.builder().provinceCode("BC").description("British Columbia")
            .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("British Columbia").createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }

  public CountryCodeEntity createCountryCodeData() {
    return CountryCodeEntity.builder().countryCode("CA").description("Canada")
            .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("Canada").createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }

  public static String asJsonString(final Object obj) {
    try {
      ObjectMapper om = new ObjectMapper();
      om.registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
      return om.writeValueAsString(obj);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
