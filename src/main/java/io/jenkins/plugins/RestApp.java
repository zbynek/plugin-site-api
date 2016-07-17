package io.jenkins.plugins;

import io.jenkins.plugins.datastore.support.ElasticsearchClientFactory;
import io.jenkins.plugins.datastore.support.EmbeddedElasticsearchServer;
import io.jenkins.plugins.schedule.JobScheduler;
import io.jenkins.plugins.service.SearchService;
import io.jenkins.plugins.service.impl.ElasticsearchSearchService;
import org.elasticsearch.client.Client;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class RestApp extends ResourceConfig {

  @Inject
  public RestApp(ServiceLocator locator) {
    ServiceLocatorUtilities.enableImmediateScope(locator);

    register(new io.jenkins.plugins.datastore.support.Binder());
    register(new io.jenkins.plugins.schedule.Binder());
    register(new io.jenkins.plugins.service.Binder());

    packages("io.jenkins.plugins");
  }

}
