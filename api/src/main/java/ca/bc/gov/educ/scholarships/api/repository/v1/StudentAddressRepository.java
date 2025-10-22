package ca.bc.gov.educ.scholarships.api.repository.v1;


import ca.bc.gov.educ.scholarships.api.model.v1.StudentAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentAddressRepository extends JpaRepository<StudentAddressEntity, UUID> {

    Optional<StudentAddressEntity> findByStudentID(UUID studentID);
}
