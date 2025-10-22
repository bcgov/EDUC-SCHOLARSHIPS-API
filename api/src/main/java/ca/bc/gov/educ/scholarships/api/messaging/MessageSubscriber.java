package ca.bc.gov.educ.scholarships.api.messaging;

import ca.bc.gov.educ.scholarships.api.helpers.LogHelper;
import ca.bc.gov.educ.scholarships.api.service.v1.events.EventHandlerDelegatorService;
import ca.bc.gov.educ.scholarships.api.struct.v1.Event;
import ca.bc.gov.educ.scholarships.api.util.JsonUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static ca.bc.gov.educ.scholarships.api.constants.v1.TopicsEnum.SCHOLARSHIPS_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class MessageSubscriber {

    /**
     * The Handlers.
     */
    @Getter(PRIVATE)
    private final Map<String, EventHandler> handlerMap = new HashMap<>();
    private final Connection connection;
    private final Executor messageProcessingThreads;
    private final EventHandlerDelegatorService eventHandlerDelegatorServiceV1;

    @Autowired
    public MessageSubscriber(final Connection con, EventHandlerDelegatorService eventHandlerDelegatorServiceV1, final List<EventHandler> eventHandlers) {
        this.connection = con;
        this.eventHandlerDelegatorServiceV1 = eventHandlerDelegatorServiceV1;
        messageProcessingThreads = new EnhancedQueueExecutor.Builder().setThreadFactory(new ThreadFactoryBuilder().setNameFormat("nats-message-subscriber-%d").build()).setCorePoolSize(10).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();
    }

    @PostConstruct
    public void subscribe() {
        String queue = SCHOLARSHIPS_API_TOPIC.toString().replace("_", "-");
        var dispatcher = connection.createDispatcher(onMessage());
        dispatcher.subscribe(SCHOLARSHIPS_API_TOPIC.toString(), queue);
    }

    /**
     * On message message handler.
     *
     * @return the message handler
     */
    private MessageHandler onMessage() {
        return (Message message) -> {
            if (message != null) {
                try {
                    var eventString = new String(message.getData());
                    LogHelper.logMessagingEventDetails(eventString);
                    var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
                
                    log.debug("message sub handling: {}, {}", event, message);
                    messageProcessingThreads.execute(() -> eventHandlerDelegatorServiceV1.handleEvent(event, message));
                } catch (final Exception e) {
                    log.debug("on message error: {}", e.getMessage());
                    log.error("Exception ", e);
                }
            }
        };
    }
}
