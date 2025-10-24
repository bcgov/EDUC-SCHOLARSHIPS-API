package ca.bc.gov.educ.scholarships.api.service.v1.events;


import ca.bc.gov.educ.scholarships.api.messaging.MessagePublisher;
import ca.bc.gov.educ.scholarships.api.messaging.jetstream.Publisher;
import ca.bc.gov.educ.scholarships.api.model.v1.ScholarshipsEvent;
import ca.bc.gov.educ.scholarships.api.struct.v1.Event;
import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * The type Event handler service.
 */
@Service
@Slf4j
@SuppressWarnings({"java:S3864", "java:S3776"})
public class EventHandlerDelegatorService {

  /**
   * The constant RESPONDING_BACK_TO_NATS_ON_CHANNEL.
   */
  public static final String RESPONDING_BACK_TO_NATS_ON_CHANNEL = "responding back to NATS on {} channel ";
  public static final String PAYLOAD_LOG = "payload is :: {}";
  private final MessagePublisher messagePublisher;
  private final EventHandlerService eventHandlerService;
  private final Publisher publisher;

  /**
   * Instantiates a new Event handler delegator service.
   *
   * @param messagePublisher    the message publisher
   * @param eventHandlerService the event handler service
   */
  @Autowired
  public EventHandlerDelegatorService(MessagePublisher messagePublisher, EventHandlerService eventHandlerService, Publisher publisher) {
    this.messagePublisher = messagePublisher;
    this.eventHandlerService = eventHandlerService;
    this.publisher = publisher;
  }

  /**
   * Handle event.
   *
   * @param event   the event
   * @param message the message
   */
  public void handleEvent(final Event event, final Message message) {
    boolean isSynchronous = message.getReplyTo() != null;
    try {
      switch (event.getEventType()) {
        case UPDATE_STUDENT_SCHOLARSHIPS_ADDRESS:
          log.info("Received UPDATE_STUDENT_SCHOLARSHIPS_ADDRESS event :: {}", event);
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          var pair = eventHandlerService.handleUpdateStudentAddressEvent(event);
          log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, message.getReplyTo() != null ? message.getReplyTo() : event.getReplyTo());
          publishToNATS(event, message, isSynchronous, pair.getLeft());
          if(pair.getRight() != null) {
            publishToJetStream(pair.getRight());  
          }
          break;
        default:
          log.info("silently ignoring other events :: {}", event);
          break;
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }

  private void publishToJetStream(final ScholarshipsEvent event) {
    publisher.dispatchChoreographyEvent(event);
  }
  
  private void publishToNATS(Event event, Message message, boolean isSynchronous, byte[] left) {
    if (isSynchronous) { // sync, req/reply pattern of nats
      messagePublisher.dispatchMessage(message.getReplyTo(), left);
    } else { // async, pub/sub
      messagePublisher.dispatchMessage(event.getReplyTo(), left);
    }
  }

}
