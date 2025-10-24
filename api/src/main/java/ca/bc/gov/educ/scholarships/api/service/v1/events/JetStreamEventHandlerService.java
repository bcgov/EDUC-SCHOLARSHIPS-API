package ca.bc.gov.educ.scholarships.api.service.v1.events;


import ca.bc.gov.educ.scholarships.api.repository.v1.ScholarshipsEventRepository;
import ca.bc.gov.educ.scholarships.api.struct.v1.ChoreographedEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static ca.bc.gov.educ.scholarships.api.constants.v1.EventStatus.MESSAGE_PUBLISHED;


/**
 * This class will process events from Jet Stream, which is used in choreography pattern, where messages are published if a student is created or updated.
 */
@Service
@Slf4j
public class JetStreamEventHandlerService {

  private final ScholarshipsEventRepository eventRepository;
  
  @Autowired
  public JetStreamEventHandlerService(ScholarshipsEventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  /**
   * Update event status.
   *
   * @param choreographedEvent the choreographed event
   */
  @Transactional
  public void updateEventStatus(ChoreographedEvent choreographedEvent) {
    if (choreographedEvent != null && choreographedEvent.getEventID() != null) {
      var eventID = UUID.fromString(choreographedEvent.getEventID());
      var eventOptional = eventRepository.findById(eventID);
      if (eventOptional.isPresent()) {
        var studentEvent = eventOptional.get();
        studentEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
        eventRepository.save(studentEvent);
      }
    }
  }
}
