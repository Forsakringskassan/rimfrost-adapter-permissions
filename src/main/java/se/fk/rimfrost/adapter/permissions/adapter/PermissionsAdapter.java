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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.permissions.jaxrsspec.controllers.generatedsource.PermissionControllerApi;
import se.fk.rimfrost.permissions.jaxrsspec.controllers.generatedsource.model.SidPermissionRequest;

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
      clientConfig.register(AdapterObjectMapperProvider.class);
      this.client = JerseyClientBuilder.newClient(clientConfig);
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
         var request = new SidPermissionRequest(idTyp, idVarde);
         return permissionsClient.hasSidPermission(request);
      }
      catch (ProcessingException ex)
      {
         var message = "Unexpected error when checking SID permission for idTyp=" + idTyp + ", idVarde=" + idVarde;
         LOGGER.error(message, ex);
         throw new PermissionsException(PermissionsException.ErrorType.UNEXPECTED_ERROR, message, ex);
      }
      catch (WebApplicationException ex)
      {
         throw toPermissionsException(ex, idTyp, idVarde);
      }
   }

   private PermissionsException toPermissionsException(WebApplicationException ex, String idTyp, String idVarde)
   {
      return switch (ex.getResponse().getStatus())
      {
         case 404 -> {
            var message = "User not found for idTyp=" + idTyp + ", idVarde=" + idVarde;
            LOGGER.error(message, ex);
            yield new PermissionsException(PermissionsException.ErrorType.NOT_FOUND, message, ex);
         }
         case 400 -> {
            var message = "Bad request when checking SID permission for idTyp=" + idTyp + ", idVarde=" + idVarde;
            LOGGER.error(message, ex);
            yield new PermissionsException(PermissionsException.ErrorType.BAD_REQUEST, message, ex);
         }
         case 503 -> {
            var message = "Permissions service unavailable";
            LOGGER.error(message, ex);
            yield new PermissionsException(PermissionsException.ErrorType.SERVICE_UNAVAILABLE, message, ex);
         }
         default -> {
            var message = "Unexpected error when checking SID permission for idTyp=" + idTyp + ", idVarde=" + idVarde;
            LOGGER.error(message, ex);
            yield new PermissionsException(PermissionsException.ErrorType.UNEXPECTED_ERROR, message, ex);
         }
      };
   }
}
