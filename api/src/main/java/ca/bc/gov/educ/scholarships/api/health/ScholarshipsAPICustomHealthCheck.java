package ca.bc.gov.educ.scholarships.api.health;

import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScholarshipsAPICustomHealthCheck implements HealthIndicator {
  private final Connection natsConnection;

  public ScholarshipsAPICustomHealthCheck(final Connection natsConnection) {
    this.natsConnection = natsConnection;
  }

  @Override
  public Health getHealth(final boolean includeDetails) {
    return this.healthCheck();
  }


  @Override
  public Health health() {
    return this.healthCheck();
  }

  private Health healthCheck() {
    if (this.natsConnection.getStatus() == Connection.Status.CLOSED) {
      log.warn("Health Check failed for NATS");
      return Health.down().withDetail("NATS", " Connection is Closed.").build();
    }
    return Health.up().build();
  }

}
