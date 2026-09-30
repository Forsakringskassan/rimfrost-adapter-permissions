package se.fk.rimfrost.adapter.permissions.adapter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.permissions.jaxrsspec.controllers.generatedsource.PermissionControllerApi;

/**
 * HTTP adapter for the permissions API, using the Jersey proxy client pattern.
 *
 * <p>Configured via {@code permissions.api.base-url}.
 */
@ApplicationScoped
public class PermissionsAdapter
{
   private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsAdapter.class);

   @ConfigProperty(name = "permissions.api.base-url")
   String permissionsBaseUrl;

   private PermissionControllerApi permissionsClient;

   private Client client;

   /**
    * Initialises the Jersey HTTP client and the permissions API proxy.
    */
   @PostConstruct
   void init()
   {
      ClientConfig clientConfig = new ClientConfig();
      clientConfig.connectorProvider(new Apache5ConnectorProvider());
      this.client = ClientBuilder.newClient(clientConfig);
      this.permissionsClient = WebResourceFactory.newResource(PermissionControllerApi.class,
            client.target(this.permissionsBaseUrl));
   }

   /**
    * Cleans up the Jersey client on bean destruction.
    */
   @PreDestroy
   void destroy()
   {
      this.permissionsClient = null;
      if (this.client != null)
      {
         this.client.close();
         this.client = null;
      }
   }

   /**
    * Returns whether the given user has SID-behörighet.
    *
    * @param idTyp   the identity type
    * @param idVarde the identity value
    * @return {@code true} if the user has SID-behörighet, otherwise {@code false}
    * @throws PermissionsException if the permissions service returns an error or is unreachable
    */
   public boolean hasSidPermission(String idTyp, String idVarde) throws PermissionsException
   {
      try
      {
         return permissionsClient.hasSidPermission(idTyp, idVarde);
      }
      catch (NotFoundException ex)
      {
         var message = "User not found for idTyp=" + idTyp + ", idVarde=" + idVarde;
         LOGGER.error(message, ex);
         throw new PermissionsException(PermissionsException.ErrorType.NOT_FOUND, message, ex);
      }
      catch (BadRequestException ex)
      {
         var message = "Bad request when checking SID permission for idTyp=" + idTyp + ", idVarde=" + idVarde;
         LOGGER.error(message, ex);
         throw new PermissionsException(PermissionsException.ErrorType.BAD_REQUEST, message, ex);
      }
      catch (ServiceUnavailableException ex)
      {
         var message = "Permissions service unavailable";
         LOGGER.error(message, ex);
         throw new PermissionsException(PermissionsException.ErrorType.SERVICE_UNAVAILABLE, message, ex);
      }
      catch (ProcessingException | WebApplicationException ex)
      {
         var message = "Unexpected error when checking SID permission for idTyp=" + idTyp + ", idVarde=" + idVarde;
         LOGGER.error(message, ex);
         throw new PermissionsException(PermissionsException.ErrorType.UNEXPECTED_ERROR, message, ex);
      }
   }

}
