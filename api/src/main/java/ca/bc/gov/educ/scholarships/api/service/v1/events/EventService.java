package ca.bc.gov.educ.scholarships.api.service.v1.events;

import ca.bc.gov.educ.scholarships.api.model.v1.ScholarshipsEvent;

/**
 * The interface Event service.
 *
 * @param <T> the type parameter
 */
public interface EventService<T> {

  /**
   * Process event.
   *
   * @param request the request
   * @param event   the event
   */
  void processEvent(T request, ScholarshipsEvent event);

  /**
   * Gets event type.
   *
   * @return the event type
   */
  String getEventType();
}
