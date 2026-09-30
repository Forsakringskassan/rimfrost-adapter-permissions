package se.fk.rimfrost.adapter.permissions.adapter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import se.fk.rimfrost.permissions.jaxrsspec.controllers.generatedsource.PermissionControllerApi;

/**
 * HTTP adapter for the permissions API, using the Jersey proxy client pattern.
 *
 * <p>Configured via {@code permissions.api.base-url}.
 */
@ApplicationScoped
public class PermissionsAdapter
{
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
    * @throws NotFoundException       if the user is not found
    * @throws ProcessingException     if the permissions service is unreachable
    * @throws WebApplicationException for other HTTP errors
    */
   public boolean hasSidPermission(String idTyp, String idVarde)
   {
      return permissionsClient.hasSidPermission(idTyp, idVarde);
   }

}
