package ca.bc.gov.educ.scholarships.api.controller.v1;


import ca.bc.gov.educ.scholarships.api.endpoint.v1.StudentAddressAPIEndpoint;
import ca.bc.gov.educ.scholarships.api.mappers.v1.StudentAddressMapper;
import ca.bc.gov.educ.scholarships.api.messaging.jetstream.Publisher;
import ca.bc.gov.educ.scholarships.api.service.v1.StudentAddressService;
import ca.bc.gov.educ.scholarships.api.struct.v1.StudentAddress;
import ca.bc.gov.educ.scholarships.api.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
public class StudentAddressAPIController implements StudentAddressAPIEndpoint {

  private final StudentAddressService studentAddressService;
  private final Publisher publisher;
  private static final StudentAddressMapper mapper = StudentAddressMapper.mapper;

  @Autowired
  public StudentAddressAPIController(StudentAddressService studentAddressService, Publisher publisher) {
      this.studentAddressService = studentAddressService;
      this.publisher = publisher;
  }

  @Override
  public StudentAddress getStudentAddress(UUID studentID) {
    return mapper.toStructure(this.studentAddressService.readStudentAddress(studentID));
  }

  @Override
  public StudentAddress createStudentAddress(UUID studentID, StudentAddress studentAddress) throws JsonProcessingException {
    RequestUtil.setAuditColumnsForCreate(studentAddress);
    var response = studentAddressService.createStudentAddress(studentAddress, studentID);
    publisher.dispatchChoreographyEvent(response.getRight());
    return mapper.toStructure(response.getLeft());
  }

  @Override
  public StudentAddress updateStudentAddress(UUID studentID, UUID studentAddressID, StudentAddress studentAddress) throws JsonProcessingException {
    RequestUtil.setAuditColumnsForUpdate(studentAddress);
    var response = studentAddressService.updateStudentAddress(studentAddress, studentID, studentAddressID);
    publisher.dispatchChoreographyEvent(response.getRight());
    return mapper.toStructure(response.getLeft());
  }

}
