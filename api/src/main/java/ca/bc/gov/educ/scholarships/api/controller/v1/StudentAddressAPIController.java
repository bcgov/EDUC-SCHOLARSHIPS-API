package ca.bc.gov.educ.scholarships.api.controller.v1;


import ca.bc.gov.educ.scholarships.api.endpoint.v1.StudentAddressAPIEndpoint;
import ca.bc.gov.educ.scholarships.api.mappers.v1.StudentAddressMapper;
import ca.bc.gov.educ.scholarships.api.service.v1.StudentAddressService;
import ca.bc.gov.educ.scholarships.api.struct.v1.StudentAddress;
import ca.bc.gov.educ.scholarships.api.util.RequestUtil;
import ca.bc.gov.educ.scholarships.api.validator.StudentAddressPayloadValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static ca.bc.gov.educ.scholarships.api.util.ValidationUtil.validatePayload;

@RestController
@Slf4j
public class StudentAddressAPIController implements StudentAddressAPIEndpoint {

  private final StudentAddressService studentAddressService;
  private final StudentAddressPayloadValidator studentAddressPayloadValidator;
  private static final StudentAddressMapper mapper = StudentAddressMapper.mapper;

  @Autowired
  public StudentAddressAPIController(StudentAddressService studentAddressService, StudentAddressPayloadValidator studentAddressPayloadValidator) {
      this.studentAddressService = studentAddressService;
      this.studentAddressPayloadValidator = studentAddressPayloadValidator;
  }

  @Override
  public StudentAddress getStudentAddress(UUID studentID) {
    return mapper.toStructure(this.studentAddressService.readStudentAddress(studentID));
  }

  @Override
  public StudentAddress createStudentAddress(UUID studentID, StudentAddress studentAddress) {
    validatePayload(() -> this.studentAddressPayloadValidator.validatePayload(studentAddress));
    RequestUtil.setAuditColumnsForCreate(studentAddress);
    return mapper.toStructure(studentAddressService.createStudentAddress(studentAddress, studentID));
  }

  @Override
  public StudentAddress updateStudentAddress(UUID studentID, UUID studentAddressID, StudentAddress studentAddress) {
    validatePayload(() -> this.studentAddressPayloadValidator.validatePayload(studentAddress));
    RequestUtil.setAuditColumnsForUpdate(studentAddress);
    return mapper.toStructure(studentAddressService.updateStudentAddress(studentAddress, studentID, studentAddressID));
  }

  @Override
  public ResponseEntity<Void> deleteStudentAddress(UUID studentID, UUID studentAddressID) {
    this.studentAddressService.deleteStudentAddress(studentAddressID);
    return ResponseEntity.noContent().build();
  }

}
