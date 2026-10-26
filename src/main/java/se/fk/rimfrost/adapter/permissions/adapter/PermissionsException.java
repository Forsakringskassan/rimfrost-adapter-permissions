package se.fk.rimfrost.adapter.permissions.adapter;

/**
 * Exception thrown by {@link PermissionsAdapter} to signal a failure when calling the permissions service.
 */
public class PermissionsException extends Exception
{
   private final ErrorType errorType;

   /**
    * @param errorType the category of error
    * @param message   human-readable description
    */
   public PermissionsException(ErrorType errorType, String message)
   {
      super(message);

      this.errorType = errorType;
   }

   /**
    * @param errorType the category of error
    * @param message   human-readable description
    * @param cause     the underlying exception
    */
   public PermissionsException(ErrorType errorType, String message, Throwable cause)
   {
      super(message, cause);

      this.errorType = errorType;
   }

   /**
    * @return the error type
    */
   public ErrorType getErrorType()
   {
      return errorType;
   }

   /** Categorises failures returned by the permissions service. */
   public enum ErrorType
   {
      NOT_FOUND, BAD_REQUEST, SERVICE_UNAVAILABLE, UNEXPECTED_ERROR,
   }
}
