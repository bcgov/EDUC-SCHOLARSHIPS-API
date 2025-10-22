package ca.bc.gov.educ.scholarships.api.mappers.v1;

import ca.bc.gov.educ.scholarships.api.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.scholarships.api.model.v1.StudentAddressEntity;
import ca.bc.gov.educ.scholarships.api.struct.v1.StudentAddress;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {LocalDateTimeMapper.class})
public interface StudentAddressMapper {

    StudentAddressMapper mapper = Mappers.getMapper(StudentAddressMapper.class);

    StudentAddress toStructure(StudentAddressEntity entity);

    StudentAddressEntity toEntity(StudentAddress entity);
}
