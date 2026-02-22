package ca.bc.gov.educ.scholarships.api.service.v1;


import ca.bc.gov.educ.scholarships.api.BaseScholarshipsAPITest;
import ca.bc.gov.educ.scholarships.api.constants.v1.EventOutcome;
import ca.bc.gov.educ.scholarships.api.constants.v1.EventType;
import ca.bc.gov.educ.scholarships.api.constants.v1.TopicsEnum;
import ca.bc.gov.educ.scholarships.api.repository.v1.CountryCodeRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.ProvinceCodeRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.ScholarshipsEventRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.StudentAddressRepository;
import ca.bc.gov.educ.scholarships.api.service.v1.events.EventHandlerService;
import ca.bc.gov.educ.scholarships.api.struct.v1.Event;
import ca.bc.gov.educ.scholarships.api.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
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

import static ca.bc.gov.educ.scholarships.api.constants.v1.EventOutcome.*;
import static ca.bc.gov.educ.scholarships.api.constants.v1.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.scholarships.api.constants.v1.EventType.GET_STUDENT_SCHOLARSHIP_ADDRESS;
import static ca.bc.gov.educ.scholarships.api.constants.v1.EventType.UPDATE_STUDENT_ADDRESS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Slf4j
class EventHandlerServiceTest extends BaseScholarshipsAPITest {

  public static final String SCHOLARSHIPS_API_TOPIC = TopicsEnum.SCHOLARSHIPS_API_TOPIC.toString();

  @Autowired
  ScholarshipsEventRepository scholarshipsEventRepository;
  
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
    scholarshipsEventRepository.deleteAll();
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
    var response = eventHandlerServiceUnderTest.handleUpdateStudentAddressEvent(event);
    assertThat(response).isNotNull();
    Event responseEvent = JsonUtil.getJsonObjectFromByteArray(Event.class, response.getLeft());
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENT_ADDRESS_UPDATED);

    var studentEventUpdated = scholarshipsEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_STUDENT_ADDRESS.toString());
    Assertions.assertThat(studentEventUpdated).isPresent();
    Assertions.assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString());
    Assertions.assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_ADDRESS_UPDATED.toString());
  }

  @Test
  void testHandleEvent_givenEventTypeUPDATE_STUDENT_SCHOLARSHIPS_ADDRESS_WITH_CAN__whenNoStudentExist_shouldHaveEventOutcome_STUDENT_ADDRESS_UPDATED() throws IOException {
    final var studentAddress = this.createStudentAddressData();
    var savedAddress = this.studentAddressRepository.save(studentAddress);
    savedAddress.setAddressLine1("ABCD");
    savedAddress.setCountryCode("CAN");

    provinceCodeRepository.save(createProvinceCodeData());
    countryCodeRepository.save(createCountryCodeData());

    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(EventType.UPDATE_STUDENT_SCHOLARSHIPS_ADDRESS).sagaId(sagaId).replyTo(SCHOLARSHIPS_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(savedAddress)).build();
    var response = eventHandlerServiceUnderTest.handleUpdateStudentAddressEvent(event);
    assertThat(response).isNotNull();
    Event responseEvent = JsonUtil.getJsonObjectFromByteArray(Event.class, response.getLeft());
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENT_ADDRESS_UPDATED);

    var studentEventUpdated = scholarshipsEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_STUDENT_ADDRESS.toString());
    Assertions.assertThat(studentEventUpdated).isPresent();
    Assertions.assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString());
    Assertions.assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_ADDRESS_UPDATED.toString());
    
    var retrievedAddress = studentAddressRepository.findAll();
    Assertions.assertThat(retrievedAddress.size()).isEqualTo(1);
    Assertions.assertThat(retrievedAddress.getFirst().getCountryCode()).isEqualTo("CA");
  }

  @Test
  void testHandleEvent_givenEventTypeGET_STUDENT_SCHOLARSHIPS_ADDRESS__whenNoStudentExist_shouldHaveEventOutcome_STUDENT_ADDRESS_FOUND() throws IOException {
    final var studentAddress = this.createStudentAddressData();
    var savedAddress = this.studentAddressRepository.save(studentAddress);
    savedAddress.setAddressLine1("ABCD");

    provinceCodeRepository.save(createProvinceCodeData());
    countryCodeRepository.save(createCountryCodeData());

    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT_SCHOLARSHIP_ADDRESS).sagaId(sagaId).replyTo(SCHOLARSHIPS_API_TOPIC).eventPayload(savedAddress.getStudentID().toString()).build();
    var response = eventHandlerServiceUnderTest.handleGetStudentAddressEvent(event, false);
    assertThat(response).isNotNull();
    Event responseEvent = JsonUtil.getJsonObjectFromByteArray(Event.class, response);
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENT_SCHOLARSHIP_ADDRESS_FOUND);
  }

  @Test
  void testHandleEvent_givenEventTypeGET_STUDENT_SCHOLARSHIPS_ADDRESS__whenNoStudentExist_shouldHaveEventOutcome_STUDENT_ADDRESS__NOT_FOUND() throws IOException {
    final var studentAddress = this.createStudentAddressData();
    var savedAddress = this.studentAddressRepository.save(studentAddress);
    savedAddress.setAddressLine1("ABCD");

    provinceCodeRepository.save(createProvinceCodeData());
    countryCodeRepository.save(createCountryCodeData());

    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT_SCHOLARSHIP_ADDRESS).sagaId(sagaId).replyTo(SCHOLARSHIPS_API_TOPIC).eventPayload(UUID.randomUUID().toString()).build();
    var response = eventHandlerServiceUnderTest.handleGetStudentAddressEvent(event, false);
    assertThat(response).isNotNull();
    Event responseEvent = JsonUtil.getJsonObjectFromByteArray(Event.class, response);
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENT_SCHOLARSHIP_ADDRESS_NOT_FOUND);
  }
}
