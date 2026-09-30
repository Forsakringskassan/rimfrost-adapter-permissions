package se.fk.rimfrost.adapter.permissions.adapter;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
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
   void testHasSidPermissionReturnsTrue()
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
   void testHasSidPermissionReturnsFalse()
   {
      server.stubFor(WireMock.get(WireMock.urlPathEqualTo("/KORTNUMMER/123456789/hasSidPermission"))
            .willReturn(WireMock.aResponse().withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("false")));

      var response = permissionsAdapter.hasSidPermission("KORTNUMMER", "123456789");

      assertEquals(Boolean.FALSE, response);
   }

   @Test
   @DisplayName("PERM-FR-01.3: hasSidPermission throws NotFoundException when service responds with 404")
   void testHasSidPermissionThrowsNotFound()
   {
      server.stubFor(WireMock.get(WireMock.urlPathEqualTo("/KORTNUMMER/unknown/hasSidPermission"))
            .willReturn(WireMock.aResponse().withStatus(404)));

      assertThrows(NotFoundException.class, () -> permissionsAdapter.hasSidPermission("KORTNUMMER", "unknown"));
   }

}
