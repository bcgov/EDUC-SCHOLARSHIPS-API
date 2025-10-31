package ca.bc.gov.educ.scholarships.api.messaging.jetstream;


import ca.bc.gov.educ.scholarships.api.helpers.LogHelper;
import ca.bc.gov.educ.scholarships.api.properties.ApplicationProperties;
import ca.bc.gov.educ.scholarships.api.service.v1.events.JetStreamEventHandlerService;
import ca.bc.gov.educ.scholarships.api.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.scholarships.api.struct.v1.Event;
import ca.bc.gov.educ.scholarships.api.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static ca.bc.gov.educ.scholarships.api.constants.v1.TopicsEnum.SCHOLARSHIPS_EVENTS_TOPIC;

/**
 * The type Subscriber.
 */
@Component
@DependsOn("publisher")
@Slf4j
public class Subscriber {
    private final Connection natsConnection;
    private final JetStreamEventHandlerService jetStreamEventHandlerService;


    @Autowired
    public Subscriber(final Connection natsConnection, JetStreamEventHandlerService jetStreamEventHandlerService) {
        this.natsConnection = natsConnection;
        this.jetStreamEventHandlerService = jetStreamEventHandlerService;
    }

    @PostConstruct
    public void subscribe() throws IOException, JetStreamApiException {
        val qName = "SCHOLARSHIPS-EVENTS-TOPIC-API";
        val autoAck = false;
        PushSubscribeOptions options = PushSubscribeOptions.builder().stream(ApplicationProperties.STREAM_NAME)
                .durable("SCHOLARSHIPS-API-EVENTS-TOPIC-DURABLE")
                .configuration(ConsumerConfiguration.builder().deliverPolicy(DeliverPolicy.New).build()).build();
        this.natsConnection.jetStream().subscribe(SCHOLARSHIPS_EVENTS_TOPIC.toString(), qName, this.natsConnection.createDispatcher(), this::onScholarshipsEventsTopicMessage,
                autoAck, options);
    }

    /**
     * This method will process the event message pushed into the scholarships_events_topic.
     * this will get the message and update the event status to mark that the event reached the message broker.
     * On message message handler.
     *
     * @param message the string representation of {@link Event} if it not type of event then it will throw exception and will be ignored.
     */
    public void onScholarshipsEventsTopicMessage(final Message message) {
        log.info("Received message Subject:: {} , SID :: {} , sequence :: {}, pending :: {} ", message.getSubject(), message.getSID(), message.metaData().consumerSequence(), message.metaData().pendingCount());
        try {
            val eventString = new String(message.getData());
            LogHelper.logMessagingEventDetails(eventString);
            ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
            jetStreamEventHandlerService.updateEventStatus(event);
            log.info("received event :: {} ", event);
            message.ack();
        } catch (final Exception ex) {
            log.error("Exception ", ex);
        }
    }
}
