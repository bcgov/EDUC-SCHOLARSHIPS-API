package ca.bc.gov.educ.scholarships.api.endpoint.v1;


import ca.bc.gov.educ.scholarships.api.constants.v1.URL;
import ca.bc.gov.educ.scholarships.api.struct.v1.StudentAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RequestMapping(URL.BASE_URL)
public interface StudentAddressAPIEndpoint {

  @GetMapping("/{studentID}/address")
  @PreAuthorize("hasAuthority('SCOPE_READ_SCHOLARSHIPS')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Tag(name = "Student Address Entity", description = "Endpoints for student address entity.")
  @Schema(name = "StudentAddress", implementation = StudentAddress.class)
  StudentAddress getStudentAddress(@PathVariable UUID studentID);
  
  @PostMapping("/{studentID}/address")
  @PreAuthorize("hasAuthority('SCOPE_WRITE_SCHOLARSHIPS')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Tag(name = "Student Address Entity", description = "Endpoints for student address entity.")
  @Schema(name = "StudentAddress", implementation = StudentAddress.class)
  @ResponseStatus(CREATED)
  StudentAddress createStudentAddress(@PathVariable UUID studentID, @Validated @RequestBody StudentAddress studentAddress);

  @PutMapping("/{studentID}/address/{studentAddressID}")
  @PreAuthorize("hasAuthority('SCOPE_WRITE_SCHOLARSHIPS')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Tag(name = "Student Address Entity", description = "Endpoints for student address entity.")
  @Schema(name = "StudentAddress", implementation = StudentAddress.class)
  StudentAddress updateStudentAddress(@PathVariable UUID studentID, @PathVariable UUID studentAddressID, @Validated @RequestBody StudentAddress studentAddress);

  @DeleteMapping("/{studentID}/address/{studentAddressID}")
  @PreAuthorize("hasAuthority('SCOPE_WRITE_SCHOLARSHIPS')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Tag(name = "Student Address Entity", description = "Endpoints for student address entity.")
  ResponseEntity<Void> deleteStudentAddress(@PathVariable UUID studentID, @PathVariable UUID studentAddressID);

}
