package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.models.Versions;
import io.jenkins.plugins.services.DatastoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <p>Endpoint for retrieving unique requiredCore versions</p>
 *
 * <p>Used for requiredCore filtering</p>
 */
@Api
@SwaggerDefinition(basePath="/api", host="plugins.jenkins.io",
  info = @Info(title="Jenkins plugin site",version="1.7.0"),
  schemes = {SwaggerDefinition.Scheme.HTTPS},
  produces = {"application/json"})
@Path("/versions")
@Produces(MediaType.APPLICATION_JSON)
public class VersionsEndpoint {

  private Logger logger = LoggerFactory.getLogger(CategoriesEndpoint.class);

  @Inject
  private DatastoreService datastoreService;

  @GET
  public Versions getVersions() {
    try {
      return datastoreService.getVersions();
    } catch (Exception e) {
      logger.error("Problem getting versions", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
