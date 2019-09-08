package io.jenkins.plugins;

import io.jenkins.plugins.services.PrepareDatastoreService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import javax.ws.rs.ApplicationPath;

/**
 * <p>Main entry point of the application.</p>
 *
 * <p>It is responsible for registering the data, service and web tiers. The
 * embedded elassticsearch server is also preloaded to ensure it begins its
 * indexing process as fast as possible so the application endpoints are
 * available.</p>
 */
@ApplicationPath("/")
public class RestApp extends ResourceConfig {

  public RestApp() {

    // Data tier
    register(new io.jenkins.plugins.datastore.Binder());

    // Service tier
    register(new io.jenkins.plugins.services.Binder());

    // Ensure datastore is populated at boot
    register(new ContainerLifecycleListener() {
      @Override
      public void onStartup(Container container) {
        final ServiceLocator locator = container.getApplicationHandler().getServiceLocator();
        final PrepareDatastoreService service = locator.getService(PrepareDatastoreService.class);
        service.populateDataStore();
        service.schedulePopulateDataStore();
      }

      @Override
      public void onReload(Container container) {
        // Do nothing
      }

      @Override
      public void onShutdown(Container container) {
        // Do nothing
      }
    });

    // Web tier
    packages("io.swagger.jaxrs");
    packages("io.jenkins.plugins");
  }

}
