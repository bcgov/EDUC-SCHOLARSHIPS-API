package ca.bc.gov.educ.scholarships.api.exception;

/**
 * The type scholarships api runtime exception.
 */
public class ScholarshipsAPIRuntimeException extends RuntimeException {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 5241655513745148898L;

  /**
   * Instantiates a new scholarships api runtime exception.
   *
   * @param message the message
   */
  public ScholarshipsAPIRuntimeException(String message) {
		super(message);
	}

  public ScholarshipsAPIRuntimeException(Throwable exception) {
    super(exception);
  }

}
