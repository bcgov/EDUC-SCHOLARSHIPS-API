package ca.bc.gov.educ.scholarships.api.controller.v1;

import ca.bc.gov.educ.scholarships.api.BaseScholarshipsAPITest;
import ca.bc.gov.educ.scholarships.api.constants.v1.URL;
import ca.bc.gov.educ.scholarships.api.repository.v1.CountryCodeRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.ProvinceCodeRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.StudentAddressRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class StudentAddressControllerTest extends BaseScholarshipsAPITest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  StudentAddressRepository studentAddressRepository;

  @Autowired
  ProvinceCodeRepository provinceCodeRepository;

  @Autowired
  CountryCodeRepository countryCodeRepository;


  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @BeforeEach
  public void before() {
    this.provinceCodeRepository.save(this.createProvinceCodeData());
    this.countryCodeRepository.save(this.createCountryCodeData());
  }

  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @AfterEach
  public void after() {
    this.studentAddressRepository.deleteAll();
    this.provinceCodeRepository.deleteAll();
    this.countryCodeRepository.deleteAll();
  }

  @Test
  void testRetrieveStudentAddress_GivenValidID_ShouldReturnStatusOK() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_SCHOLARSHIPS_STUDENT";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final var studentAddress = this.createStudentAddressData();
    var studentAddressEntity = this.studentAddressRepository.save(studentAddress);
    this.mockMvc.perform(get(URL.BASE_URL + "/" + studentAddressEntity.getStudentID() + "/address").with(mockAuthority))
            .andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.studentAddressId")
                    .value(studentAddress.getStudentAddressId().toString()));
  }

  @Test
  void testCreateStudentAddress_GivenValidID_ShouldReturnStatusOK() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_WRITE_SCHOLARSHIPS_STUDENT";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final var studentAddress = this.createStudentAddressData();
    this.mockMvc.perform(post(URL.BASE_URL + "/" + studentAddress.getStudentID() + "/address").with(mockAuthority)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(asJsonString(studentAddress)))
            .andDo(print()).andExpect(status().isCreated()).andExpect(MockMvcResultMatchers.jsonPath("$.studentID")
                    .value(studentAddress.getStudentID().toString()));
  }

  @Test
  void testDeleteStudentAddress_GivenValidID_ShouldReturnStatusOK() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_WRITE_SCHOLARSHIPS_STUDENT";
    final var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final var studentAddress = this.createStudentAddressData();
    var studentAddressEntity = this.studentAddressRepository.save(studentAddress);
    this.mockMvc.perform(delete(URL.BASE_URL + "/" + studentAddressEntity.getStudentID() + "/address/" + studentAddressEntity.getStudentAddressId()).with(mockAuthority))
            .andDo(print()).andExpect(status().isNoContent());

    var deletedAddress = this.studentAddressRepository.findById(studentAddressEntity.getStudentAddressId());
    Assertions.assertTrue(deletedAddress.isEmpty());
  }

  @Test
  void testUpdateStudentAddress_GivenValidID_ShouldReturnStatusOK() throws Exception {
    final var studentAddress = createStudentAddressData();
    var studentAddressEntity = this.studentAddressRepository.save(studentAddress);
    studentAddressEntity.setAddressLine1("ABCD");

    this.mockMvc.perform(put(URL.BASE_URL + "/" + studentAddressEntity.getStudentID() + "/address/" + studentAddressEntity.getStudentAddressId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(asJsonString(studentAddressEntity))
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_SCHOLARSHIPS_STUDENT"))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.addressLine1").value(studentAddressEntity.getAddressLine1()));
  }

}


