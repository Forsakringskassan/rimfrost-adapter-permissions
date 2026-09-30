package se.fk.rimfrost.adapter.permissions.adapter;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusComponentTest(useSystemConfigSources = true)
public class PermissionsAdapterTest
{
   private static WireMockServer server;

   @Inject
   PermissionsAdapter permissionsAdapter;

   @BeforeAll
   public static void setup()
   {
      server = new WireMockServer(options().dynamicPort());
      server.start();

      System.setProperty("permissions.api.base-url", server.baseUrl());
   }

   @BeforeEach
   void resetStubs()
   {
      server.resetToDefaultMappings();
   }

   @AfterAll
   public static void teardown()
   {
      if (server != null)
      {
         server.stop();
      }
   }

   @Test
   @DisplayName("PERM-FR-01.1: hasSidPermission returns true when service responds with true")
   void testHasSidPermissionReturnsTrue() throws PermissionsException
   {
      server.stubFor(WireMock.get(WireMock.urlPathEqualTo("/KORTNUMMER/123456789/hasSidPermission"))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("true")));

      var response = permissionsAdapter.hasSidPermission("KORTNUMMER", "123456789");

      assertEquals(Boolean.TRUE, response);
   }

   @Test
   @DisplayName("PERM-FR-01.2: hasSidPermission returns false when service responds with false")
   void testHasSidPermissionReturnsFalse() throws PermissionsException
   {
      server.stubFor(WireMock.get(WireMock.urlPathEqualTo("/KORTNUMMER/123456789/hasSidPermission"))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("false")));

      var response = permissionsAdapter.hasSidPermission("KORTNUMMER", "123456789");

      assertEquals(Boolean.FALSE, response);
   }

   @Test
   @DisplayName("PERM-FR-01.3: hasSidPermission throws PermissionsException with NOT_FOUND when service responds with 404")
   void testHasSidPermissionThrowsNotFound()
   {
      server.stubFor(WireMock.get(WireMock.urlPathEqualTo("/KORTNUMMER/unknown/hasSidPermission"))
            .willReturn(WireMock.aResponse().withStatus(404)));

      var ex = assertThrows(PermissionsException.class, () -> permissionsAdapter.hasSidPermission("KORTNUMMER", "unknown"));
      assertEquals(PermissionsException.ErrorType.NOT_FOUND, ex.getErrorType());
   }

   @Test
   @DisplayName("PERM-FR-01.4: hasSidPermission throws PermissionsException with BAD_REQUEST when service responds with 400")
   void testHasSidPermissionThrowsBadRequest()
   {
      server.stubFor(WireMock.get(WireMock.urlPathEqualTo("/OGILTIG/123456789/hasSidPermission"))
            .willReturn(WireMock.aResponse().withStatus(400)));

      var ex = assertThrows(PermissionsException.class, () -> permissionsAdapter.hasSidPermission("OGILTIG", "123456789"));
      assertEquals(PermissionsException.ErrorType.BAD_REQUEST, ex.getErrorType());
   }

   @Test
   @DisplayName("PERM-FR-01.5: hasSidPermission throws PermissionsException with SERVICE_UNAVAILABLE when service responds with 503")
   void testHasSidPermissionThrowsServiceUnavailable()
   {
      server.stubFor(WireMock.get(WireMock.urlPathEqualTo("/KORTNUMMER/123456789/hasSidPermission"))
            .willReturn(WireMock.aResponse().withStatus(503)));

      var ex = assertThrows(PermissionsException.class, () -> permissionsAdapter.hasSidPermission("KORTNUMMER", "123456789"));
      assertEquals(PermissionsException.ErrorType.SERVICE_UNAVAILABLE, ex.getErrorType());
   }

   @Test
   @DisplayName("PERM-FR-01.6: hasSidPermission throws PermissionsException with UNEXPECTED_ERROR when service responds with 500")
   void testHasSidPermissionThrowsUnexpectedError()
   {
      server.stubFor(WireMock.get(WireMock.urlPathEqualTo("/KORTNUMMER/123456789/hasSidPermission"))
            .willReturn(WireMock.aResponse().withStatus(500)));

      var ex = assertThrows(PermissionsException.class, () -> permissionsAdapter.hasSidPermission("KORTNUMMER", "123456789"));
      assertEquals(PermissionsException.ErrorType.UNEXPECTED_ERROR, ex.getErrorType());
   }

}
