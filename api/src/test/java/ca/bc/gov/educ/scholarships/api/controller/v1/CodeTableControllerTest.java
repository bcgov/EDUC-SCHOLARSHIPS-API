package ca.bc.gov.educ.scholarships.api.controller.v1;

import ca.bc.gov.educ.scholarships.api.BaseScholarshipsAPITest;
import ca.bc.gov.educ.scholarships.api.constants.v1.URL;
import ca.bc.gov.educ.scholarships.api.repository.v1.CitizenshipCodeRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.CountryCodeRepository;
import ca.bc.gov.educ.scholarships.api.repository.v1.ProvinceCodeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class CodeTableControllerTest extends BaseScholarshipsAPITest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  CitizenshipCodeRepository citizenshipCodeRepository;

  @Autowired
  CountryCodeRepository countryCodeRepository;

  @Autowired
  ProvinceCodeRepository provinceCodeRepository;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    citizenshipCodeRepository.save(createCitizenshipCodeEntity());
    countryCodeRepository.save(createCountryCodeData());
    provinceCodeRepository.save(createProvinceCodeData());
  }

  @AfterEach
  public void tearDown() {
    citizenshipCodeRepository.deleteAll();
    countryCodeRepository.deleteAll();
    provinceCodeRepository.deleteAll();
  }

  protected static final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

  @Test
  void testGetAllCitizenshipCodes_ShouldReturnCodes() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_SCHOLARSHIPS_CODES";
    final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

    this.mockMvc.perform(get(URL.BASE_URL + URL.CITIZENSHIP_CODES).with(mockAuthority)).andDo(print()).andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].citizenshipCode").value("C"));
  }

  @Test
  void testGetAllCountryCodes_ShouldReturnCodes() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_SCHOLARSHIPS_CODES";
    final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

    this.mockMvc.perform(get(URL.BASE_URL + URL.COUNTRY_CODES).with(mockAuthority)).andDo(print()).andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].countryCode").value("CA"));
  }

  @Test
  void testGetAllProvinceCodes_ShouldReturnCodes() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_SCHOLARSHIPS_CODES";
    final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

    this.mockMvc.perform(get(URL.BASE_URL + URL.PROVINCE_CODES).with(mockAuthority)).andDo(print()).andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].provinceCode").value("BC"));
  }
}
