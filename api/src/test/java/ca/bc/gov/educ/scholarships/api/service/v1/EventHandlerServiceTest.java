package ca.bc.gov.educ.scholarships.api.service.v1;


import ca.bc.gov.educ.scholarships.api.BaseScholarshipsAPITest;
import ca.bc.gov.educ.scholarships.api.constants.v1.EventOutcome;
import ca.bc.gov.educ.scholarships.api.constants.v1.EventType;
import ca.bc.gov.educ.scholarships.api.constants.v1.TopicsEnum;
import ca.bc.gov.educ.scholarships.api.repository.v1.CountryCodeRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.ProvinceCodeRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.StudentAddressRepository;
import ca.bc.gov.educ.scholarships.api.service.v1.events.EventHandlerService;
import ca.bc.gov.educ.scholarships.api.struct.v1.Event;
import ca.bc.gov.educ.scholarships.api.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Slf4j
class EventHandlerServiceTest extends BaseScholarshipsAPITest {

  public static final String SCHOLARSHIPS_API_TOPIC = TopicsEnum.SCHOLARSHIPS_API_TOPIC.toString();
  
  @Autowired
  StudentAddressRepository studentAddressRepository;

  @Autowired
  CountryCodeRepository countryCodeRepository;

  @Autowired
  ProvinceCodeRepository provinceCodeRepository;

  @Autowired
  EventHandlerService eventHandlerServiceUnderTest;
  private final boolean isSynchronous = false;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    
  }

  @AfterEach
  public void tearDown() {
    studentAddressRepository.deleteAll();
    countryCodeRepository.deleteAll();
    provinceCodeRepository.deleteAll();
  }

  @Test
  void testHandleEvent_givenEventTypeUPDATE_STUDENT_SCHOLARSHIPS_ADDRESS__whenNoStudentExist_shouldHaveEventOutcome_STUDENT_ADDRESS_VALIDATION_ERRORS() throws IOException {
    final var studentAddress = this.createStudentAddressData();
    var savedAddress = this.studentAddressRepository.save(studentAddress);
    savedAddress.setAddressLine1("ABCD");

    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(EventType.UPDATE_STUDENT_SCHOLARSHIPS_ADDRESS).sagaId(sagaId).replyTo(SCHOLARSHIPS_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(savedAddress)).build();
    var response = eventHandlerServiceUnderTest.handleUpdateSchoolEvent(event);
    assertThat(response).isNotNull();
    Event responseEvent = JsonUtil.getJsonObjectFromByteArray(Event.class, response);
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.STUDENT_ADDRESS_VALIDATION_ERRORS);
  }

  @Test
  void testHandleEvent_givenEventTypeUPDATE_STUDENT_SCHOLARSHIPS_ADDRESS__whenNoStudentExist_shouldHaveEventOutcome_STUDENT_ADDRESS_UPDATED() throws IOException {
    final var studentAddress = this.createStudentAddressData();
    var savedAddress = this.studentAddressRepository.save(studentAddress);
    savedAddress.setAddressLine1("ABCD");
    
    provinceCodeRepository.save(createProvinceCodeData());
    countryCodeRepository.save(createCountryCodeData());

    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(EventType.UPDATE_STUDENT_SCHOLARSHIPS_ADDRESS).sagaId(sagaId).replyTo(SCHOLARSHIPS_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(savedAddress)).build();
    var response = eventHandlerServiceUnderTest.handleUpdateSchoolEvent(event);
    assertThat(response).isNotNull();
    Event responseEvent = JsonUtil.getJsonObjectFromByteArray(Event.class, response);
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.STUDENT_ADDRESS_UPDATED);
  }
}
