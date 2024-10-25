package ca.bc.gov.educ.scholarships.api;

import lombok.val;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * The Scholarships api application.
 *
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "1s")
@EnableRetry
public class ScholarshipsApiApplication {

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(ScholarshipsApiApplication.class, args);
  }

  /**
   * Lock provider For distributed lock, to avoid multiple pods executing the same scheduled task.
   *
   * @param jdbcTemplate       the jdbc template
   * @param transactionManager the transaction manager
   * @return the lock provider
   */
  @Bean
  public LockProvider lockProvider(@Autowired final JdbcTemplate jdbcTemplate,
      @Autowired final PlatformTransactionManager transactionManager) {
    return new JdbcTemplateLockProvider(jdbcTemplate, transactionManager,
        "SCHOLARSHIPS_SHEDLOCK");
  }

  /**
   * Thread pool task scheduler thread pool task scheduler.
   *
   * @return the thread pool task scheduler
   */
  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    val threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(5);
    return threadPoolTaskScheduler;
  }

  /**
   * The type Web security configuration. Add security exceptions for swagger UI and prometheus.
   */
  @Configuration
  @EnableMethodSecurity
  static
  class WebSecurityConfiguration {

    /**
     * Instantiates a new Web security configuration. This makes sure that security context is
     * propagated to async threads as well.
     */
    public WebSecurityConfiguration() {
      super();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/v3/api-docs/**",
                  "/actuator/health", "/actuator/prometheus", "/actuator/**",
                  "/swagger-ui/**").permitAll()
              .anyRequest().authenticated()
          )
          .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .oauth2ResourceServer(oauth2 -> oauth2
                  .jwt(Customizer.withDefaults())
          );
      return http.build();
    }
  }
}