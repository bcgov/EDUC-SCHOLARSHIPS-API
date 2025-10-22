package ca.bc.gov.educ.scholarships.api.mappers.v1;

import ca.bc.gov.educ.scholarships.api.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.scholarships.api.model.v1.CitizenshipCodeEntity;
import ca.bc.gov.educ.scholarships.api.model.v1.CountryCodeEntity;
import ca.bc.gov.educ.scholarships.api.model.v1.ProvinceCodeEntity;
import ca.bc.gov.educ.scholarships.api.struct.v1.CitizenshipCode;
import ca.bc.gov.educ.scholarships.api.struct.v1.CountryCode;
import ca.bc.gov.educ.scholarships.api.struct.v1.ProvinceCode;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {LocalDateTimeMapper.class})
public interface CodeTableMapper {

    CodeTableMapper mapper = Mappers.getMapper(CodeTableMapper.class);

    CitizenshipCode toStructure(CitizenshipCodeEntity entity);

    CountryCode toStructure(CountryCodeEntity entity);

    ProvinceCode toStructure(ProvinceCodeEntity entity);
}
