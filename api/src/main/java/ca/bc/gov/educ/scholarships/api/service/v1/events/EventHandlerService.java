package ca.bc.gov.educ.scholarships.api.service.v1.events;

import ca.bc.gov.educ.scholarships.api.constants.v1.EventOutcome;
import ca.bc.gov.educ.scholarships.api.mappers.v1.StudentAddressMapper;
import ca.bc.gov.educ.scholarships.api.model.v1.ScholarshipsEvent;
import ca.bc.gov.educ.scholarships.api.repository.v1.ScholarshipsEventRepository;
import ca.bc.gov.educ.scholarships.api.service.v1.StudentAddressService;
import ca.bc.gov.educ.scholarships.api.struct.v1.Event;
import ca.bc.gov.educ.scholarships.api.struct.v1.StudentAddress;
import ca.bc.gov.educ.scholarships.api.util.JsonUtil;
import ca.bc.gov.educ.scholarships.api.util.RequestUtil;
import ca.bc.gov.educ.scholarships.api.validator.StudentAddressPayloadValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static ca.bc.gov.educ.scholarships.api.constants.v1.EventOutcome.STUDENT_ADDRESS_UPDATED;
import static ca.bc.gov.educ.scholarships.api.constants.v1.EventType.UPDATE_STUDENT_ADDRESS;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
@SuppressWarnings("java:S3864")
public class EventHandlerService {

  private final StudentAddressPayloadValidator studentAddressPayloadValidator;
  private final StudentAddressService studentAddressService;
  private final ScholarshipsEventRepository scholarshipsEventRepository;
  public static final String PAYLOAD_LOG = "payload is :: {}";
  private final StudentAddressMapper studentAddressMapper = StudentAddressMapper.mapper;
  public static final String EVENT_PAYLOAD = "event is :: {}";
  
  @Autowired
  public EventHandlerService(StudentAddressPayloadValidator studentAddressPayloadValidator, StudentAddressService studentAddressService, ScholarshipsEventRepository scholarshipsEventRepository){
      this.studentAddressPayloadValidator = studentAddressPayloadValidator;
      this.studentAddressService = studentAddressService;
      this.scholarshipsEventRepository = scholarshipsEventRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Pair<byte[], ScholarshipsEvent> handleUpdateStudentAddressEvent(Event event) throws JsonProcessingException {
    log.trace(EVENT_PAYLOAD, event);
    StudentAddress studentAddress = JsonUtil.getJsonObjectFromString(StudentAddress.class, event.getEventPayload());

    val validationErrors = studentAddressPayloadValidator.validatePayload(studentAddress);
    if (!validationErrors.isEmpty()) {
      log.debug("Validation errors found for school update: {}", validationErrors);
      event.setEventOutcome(EventOutcome.STUDENT_ADDRESS_VALIDATION_ERRORS);
      event.setEventPayload(JsonUtil.getJsonStringFromObject(validationErrors));
      return Pair.of(createResponseEvent(event), null);
    }else {
      RequestUtil.setAuditColumnsForCreate(studentAddress);
      var studentAddressUpdated = studentAddressService.createOrUpdateStudentAddress(studentAddress, UUID.fromString(studentAddress.getStudentID()), UUID.fromString(studentAddress.getStudentAddressId()));
      event.setEventOutcome(EventOutcome.STUDENT_ADDRESS_UPDATED);
      event.setEventPayload(JsonUtil.getJsonStringFromObject(studentAddressMapper.toStructure(studentAddressUpdated)));
      final ScholarshipsEvent scholarshipsEvent = studentAddressService.createStudentAddressEvent(studentAddressUpdated.getCreateUser(), studentAddressUpdated.getUpdateUser(), JsonUtil.getJsonStringFromObject(StudentAddressMapper.mapper.toStructure(studentAddressUpdated)), UPDATE_STUDENT_ADDRESS, STUDENT_ADDRESS_UPDATED, event.getSagaId());
      scholarshipsEventRepository.save(scholarshipsEvent);
      return Pair.of(createResponseEvent(event), scholarshipsEvent);
    }
  }

  private byte[] createResponseEvent(Event event) throws JsonProcessingException {
    val responseEvent = Event.builder()
            .sagaId(event.getSagaId())
            .eventType(event.getEventType())
            .eventOutcome(event.getEventOutcome())
            .eventPayload(event.getEventPayload()).build();
    return JsonUtil.getJsonBytesFromObject(responseEvent);
  }

}
