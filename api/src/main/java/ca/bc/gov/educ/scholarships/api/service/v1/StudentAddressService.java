package ca.bc.gov.educ.scholarships.api.service.v1;


import ca.bc.gov.educ.scholarships.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.scholarships.api.exception.InvalidPayloadException;
import ca.bc.gov.educ.scholarships.api.exception.errors.ApiError;
import ca.bc.gov.educ.scholarships.api.mappers.v1.StudentAddressMapper;
import ca.bc.gov.educ.scholarships.api.model.v1.StudentAddressEntity;
import ca.bc.gov.educ.scholarships.api.repository.v1.StudentAddressRepository;
import ca.bc.gov.educ.scholarships.api.struct.v1.StudentAddress;
import ca.bc.gov.educ.scholarships.api.util.TransformUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@Slf4j
public class StudentAddressService {

  private static final String CREATE_DATE = "createDate";
  private static final String CREATE_USER = "createUser";
  private final StudentAddressRepository studentAddressRepository;
  private final StudentAddressMapper studentAddressMapper = StudentAddressMapper.mapper;
  
  @Autowired
  public StudentAddressService(StudentAddressRepository studentAddressRepository) {
      this.studentAddressRepository = studentAddressRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public StudentAddressEntity createStudentAddress(StudentAddress studentAddress, UUID studentID) {
    var studentExistingAddress = studentAddressRepository.findByStudentID(studentID);
    if(studentExistingAddress.isPresent()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Student already has an address.").status(BAD_REQUEST).build();
      throw new InvalidPayloadException(error);
    }

    var studentAddressEntity = studentAddressMapper.toEntity(studentAddress);
    
    studentAddressEntity.setStudentID(studentID);
    TransformUtil.uppercaseFields(studentAddressEntity);
    return studentAddressRepository.save(studentAddressEntity);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public StudentAddressEntity updateStudentAddress(StudentAddress studentAddress, UUID studentID, UUID studentAddressID) {
    var studentAddressEntity = studentAddressMapper.toEntity(studentAddress);
    var existingStudentAddress = studentAddressRepository.findById(studentAddressID).orElseThrow(() -> new EntityNotFoundException(StudentAddressEntity.class, "studentAddressID", studentAddressID.toString()));

    if (!studentID.equals(existingStudentAddress.getStudentID())) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Student ID does not match the existing student address record.").status(BAD_REQUEST).build();
      throw new InvalidPayloadException(error);
    }
    BeanUtils.copyProperties(studentAddressEntity, existingStudentAddress, CREATE_DATE, CREATE_USER); // update current student entity with incoming payload ignoring the fields.
    TransformUtil.uppercaseFields(existingStudentAddress); // convert the input to upper case.
    return studentAddressRepository.save(existingStudentAddress);
  }
  
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deleteStudentAddress(UUID studentAddressID) {
    studentAddressRepository.findById(studentAddressID).orElseThrow(() -> new EntityNotFoundException(StudentAddressEntity.class, "studentAddressID", studentAddressID.toString()));
    studentAddressRepository.deleteById(studentAddressID);
  }

  public StudentAddressEntity readStudentAddress(UUID studentID) {
    return studentAddressRepository.findByStudentID(studentID).orElseThrow(() -> new EntityNotFoundException(StudentAddressEntity.class, "studentID", studentID.toString()));
  }
}
