package ca.bc.gov.educ.scholarships.api.model.v1;

import ca.bc.gov.educ.scholarships.api.util.ComparableField;
import ca.bc.gov.educ.scholarships.api.util.UpperCase;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@Table(name = "STUDENT_ADDRESS")
public class StudentAddressEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "student_address_id", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    @ComparableField
    private UUID studentAddressId;
    @Column(name = "student_id")
    @ComparableField
    private UUID studentID;
    @Basic
    @Column(name = "address_line_1")
    @ComparableField
    private String addressLine1;
    @Basic
    @Column(name = "address_line_2")
    @ComparableField
    private String addressLine2;
    @Basic
    @Column(name = "city")
    @ComparableField
    private String city;
    @Basic
    @Column(name = "postal_zip")
    @UpperCase
    @ComparableField
    private String postalZip;
    @Basic
    @Column(name = "province_state_code")
    @UpperCase
    @ComparableField
    private String provinceStateCode;
    @Basic
    @Column(name = "country_code")
    @UpperCase
    @ComparableField
    private String countryCode;
    @Column(name = "CREATE_USER", updatable = false)
    private String createUser;
    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    private LocalDateTime createDate;
    @Column(name = "update_user")
    private String updateUser;
    @PastOrPresent
    @Column(name = "update_date")
    private LocalDateTime updateDate;

}
