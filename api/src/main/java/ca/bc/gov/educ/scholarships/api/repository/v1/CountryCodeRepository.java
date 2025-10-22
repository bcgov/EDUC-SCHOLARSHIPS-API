package ca.bc.gov.educ.scholarships.api.repository.v1;


import ca.bc.gov.educ.scholarships.api.model.v1.CountryCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryCodeRepository extends JpaRepository<CountryCodeEntity, String> {

}
