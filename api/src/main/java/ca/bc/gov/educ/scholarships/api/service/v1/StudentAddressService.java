package ca.bc.gov.educ.scholarships.api.service.v1;


import ca.bc.gov.educ.scholarships.api.constants.v1.EventOutcome;
import ca.bc.gov.educ.scholarships.api.constants.v1.EventType;
import ca.bc.gov.educ.scholarships.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.scholarships.api.exception.InvalidPayloadException;
import ca.bc.gov.educ.scholarships.api.exception.errors.ApiError;
import ca.bc.gov.educ.scholarships.api.mappers.v1.StudentAddressMapper;
import ca.bc.gov.educ.scholarships.api.model.v1.ScholarshipsEvent;
import ca.bc.gov.educ.scholarships.api.model.v1.StudentAddressEntity;
import ca.bc.gov.educ.scholarships.api.repository.v1.ScholarshipsEventRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.StudentAddressRepository;
import ca.bc.gov.educ.scholarships.api.struct.v1.StudentAddress;
import ca.bc.gov.educ.scholarships.api.util.JsonUtil;
import ca.bc.gov.educ.scholarships.api.util.TransformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.scholarships.api.constants.v1.EventOutcome.STUDENT_ADDRESS_UPDATED;
import static ca.bc.gov.educ.scholarships.api.constants.v1.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.scholarships.api.constants.v1.EventType.UPDATE_STUDENT_ADDRESS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@Slf4j
public class StudentAddressService {

  private static final String CREATE_DATE = "createDate";
  private static final String CREATE_USER = "createUser";
  private final StudentAddressRepository studentAddressRepository;
  private final ScholarshipsEventRepository scholarshipsEventRepository;
  private final StudentAddressMapper studentAddressMapper = StudentAddressMapper.mapper;
  
  @Autowired
  public StudentAddressService(StudentAddressRepository studentAddressRepository, ScholarshipsEventRepository scholarshipsEventRepository) {
      this.studentAddressRepository = studentAddressRepository;
      this.scholarshipsEventRepository = scholarshipsEventRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Pair<StudentAddressEntity, ScholarshipsEvent> createStudentAddress(StudentAddress studentAddress, UUID studentID) throws JsonProcessingException {
    var studentExistingAddress = studentAddressRepository.findByStudentID(studentID);
    if(studentExistingAddress.isPresent()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Student already has an address.").status(BAD_REQUEST).build();
      throw new InvalidPayloadException(error);
    }

    var studentAddressEntity = studentAddressMapper.toEntity(studentAddress);
    
    studentAddressEntity.setStudentID(studentID);
    TransformUtil.uppercaseFields(studentAddressEntity);
    var savedStudent = studentAddressRepository.save(studentAddressEntity);
    final ScholarshipsEvent scholarshipsEvent = createStudentAddressEvent(savedStudent.getCreateUser(), savedStudent.getUpdateUser(), JsonUtil.getJsonStringFromObject(StudentAddressMapper.mapper.toStructure(savedStudent)), UPDATE_STUDENT_ADDRESS, STUDENT_ADDRESS_UPDATED, null);
    scholarshipsEventRepository.save(scholarshipsEvent);
    return Pair.of(savedStudent, scholarshipsEvent);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Pair<StudentAddressEntity, ScholarshipsEvent> updateStudentAddress(StudentAddress studentAddress, UUID studentID, UUID studentAddressID) throws JsonProcessingException {
    var studentAddressEntity = studentAddressMapper.toEntity(studentAddress);
    var existingStudentAddress = studentAddressRepository.findById(studentAddressID).orElseThrow(() -> new EntityNotFoundException(StudentAddressEntity.class, "studentAddressID", studentAddressID.toString()));

    if (!studentID.equals(existingStudentAddress.getStudentID())) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Student ID does not match the existing student address record.").status(BAD_REQUEST).build();
      throw new InvalidPayloadException(error);
    }
    BeanUtils.copyProperties(studentAddressEntity, existingStudentAddress, CREATE_DATE, CREATE_USER); // update current student entity with incoming payload ignoring the fields.
    TransformUtil.uppercaseFields(existingStudentAddress); // convert the input to upper case.
    var savedStudent = studentAddressRepository.save(existingStudentAddress);
    final ScholarshipsEvent scholarshipsEvent = createStudentAddressEvent(savedStudent.getCreateUser(), savedStudent.getUpdateUser(), JsonUtil.getJsonStringFromObject(StudentAddressMapper.mapper.toStructure(savedStudent)), UPDATE_STUDENT_ADDRESS, STUDENT_ADDRESS_UPDATED, null);
    scholarshipsEventRepository.save(scholarshipsEvent);
    return Pair.of(savedStudent, scholarshipsEvent);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public StudentAddressEntity createOrUpdateStudentAddress(StudentAddress studentAddress, UUID studentID) {
    var studentAddressEntity = studentAddressMapper.toEntity(studentAddress);
    var existingStudentAddress = studentAddressRepository.findByStudentID(studentID);

    if(existingStudentAddress.isPresent()) {
      var currentAddress = existingStudentAddress.get();
      BeanUtils.copyProperties(studentAddressEntity, currentAddress, CREATE_DATE, CREATE_USER); // update current student entity with incoming payload ignoring the fields.
      TransformUtil.uppercaseFields(currentAddress); // convert the input to upper case.
      return studentAddressRepository.save(currentAddress);
    }else{
      studentAddressEntity.setStudentAddressId(null);
      TransformUtil.uppercaseFields(studentAddressEntity); // convert the input to upper case.
      return studentAddressRepository.save(studentAddressEntity); 
    }
  }

  public StudentAddressEntity readStudentAddress(UUID studentID) {
    return studentAddressRepository.findByStudentID(studentID).orElseThrow(() -> new EntityNotFoundException(StudentAddressEntity.class, "studentID", studentID.toString()));
  }

  public Optional<StudentAddressEntity> getStudentAddress(UUID studentID) {
    return studentAddressRepository.findByStudentID(studentID);
  }

  public ScholarshipsEvent createStudentAddressEvent(String createUser, String updateUser, String jsonString, EventType eventType, EventOutcome eventOutcome, UUID sagaID) {
    return ScholarshipsEvent.builder()
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser(createUser)
            .updateUser(updateUser)
            .sagaId(sagaID)
            .eventPayload(jsonString)
            .eventType(eventType.toString())
            .eventStatus(DB_COMMITTED.toString())
            .eventOutcome(eventOutcome.toString())
            .build();
  }
}
