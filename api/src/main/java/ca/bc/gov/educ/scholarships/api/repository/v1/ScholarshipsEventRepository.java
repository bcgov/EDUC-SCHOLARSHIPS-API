package ca.bc.gov.educ.scholarships.api.repository.v1;

import ca.bc.gov.educ.scholarships.api.model.v1.ScholarshipsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScholarshipsEventRepository extends JpaRepository<ScholarshipsEvent, UUID> {

  Optional<ScholarshipsEvent> findByEventId(UUID eventId);
  /**
   * Find by saga id optional.
   *
   * @param sagaId the saga id
   * @return the optional
   */
  Optional<ScholarshipsEvent> findBySagaId(UUID sagaId);

  List<ScholarshipsEvent> findByEventStatus(String eventStatus);
  
  /**
   * Find by saga id and event type optional.
   *
   * @param sagaId    the saga id
   * @param eventType the event type
   * @return the optional
   */
  Optional<ScholarshipsEvent> findBySagaIdAndEventType(UUID sagaId, String eventType);


  List<ScholarshipsEvent> findByEventStatusAndEventTypeNotIn(String eventStatus, List<String> eventTypes);

  @Query(value = "select event.* from SCHOLARSHIPS_EVENT event where event.EVENT_STATUS = :eventStatus " +
          "AND event.CREATE_DATE < :createDate " +
          "AND event.EVENT_TYPE in :eventTypes " +
          "ORDER BY event.CREATE_DATE asc " +
          "FETCH FIRST :limit ROWS ONLY", nativeQuery=true)
  List<ScholarshipsEvent> findAllByEventStatusAndCreateDateBeforeAndEventTypeInOrderByCreateDate(String eventStatus, LocalDateTime createDate, int limit, List<String> eventTypes);

  @Transactional
  @Modifying
  @Query("delete from ScholarshipsEvent where createDate <= :createDate")
  void deleteByCreateDateBefore(LocalDateTime createDate);
}
