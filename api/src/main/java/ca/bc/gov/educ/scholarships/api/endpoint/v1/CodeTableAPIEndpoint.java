package ca.bc.gov.educ.scholarships.api.endpoint.v1;

import ca.bc.gov.educ.scholarships.api.constants.v1.URL;
import ca.bc.gov.educ.scholarships.api.struct.v1.CitizenshipCode;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping(URL.BASE_URL)
public interface CodeTableAPIEndpoint {

    @PreAuthorize("hasAuthority('SCOPE_READ_SCHOLARSHIPS_CODES')")
    @GetMapping(URL.CITIZENSHIP_CODES)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    @Transactional(readOnly = true)
    @Tag(name = "Citizenship Codes", description = "Endpoints to get citizenship codes.")
    @Schema(name = "CitizenshipCode", implementation = CitizenshipCode.class)
    List<CitizenshipCode> getCitizenshipCodes();

}
