package ca.bc.gov.educ.scholarships.api.properties;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Class holds all application properties
 *
 */
@Component
@Getter
@Setter
public class ApplicationProperties {
  public static final Executor bgTask = new EnhancedQueueExecutor.Builder()
    .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("bg-task-executor-%d").build())
    .setCorePoolSize(1).setMaximumPoolSize(1).setKeepAliveTime(Duration.ofSeconds(60)).build();
  public static final String SCHOLARSHIPS_API = "SCHOLARSHIPS_API";
  public static final String CORRELATION_ID = "correlationID";
  public static final String STREAM_NAME= "SCHOLARSHIPS_EVENTS";
  /**
   * The Client id.
   */
  @Value("${client.id}")
  private String clientID;
  /**
   * The Client secret.
   */
  @Value("${client.secret}")
  private String clientSecret;
  /**
   * The Token url.
   */
  @Value("${url.token}")
  private String tokenURL;

  @Value("${nats.server}")
  private String server;

  @Value("${nats.maxReconnect}")
  private int maxReconnect;

  @Value("${nats.connectionName}")
  private String connectionName;

  @Value("${threads.min.subscriber}")
  private Integer minSubscriberThreads;
  @Value("${threads.max.subscriber}")
  private Integer maxSubscriberThreads;
}
