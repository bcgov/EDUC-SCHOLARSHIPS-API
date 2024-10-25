package ca.bc.gov.educ.scholarships.api;

import ca.bc.gov.educ.scholarships.api.model.v1.CitizenshipCodeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

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
