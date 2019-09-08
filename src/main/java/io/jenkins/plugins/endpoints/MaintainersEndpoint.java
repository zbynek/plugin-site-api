package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.models.Maintainers;
import io.jenkins.plugins.services.DatastoreService;
import io.swagger.annotations.Api;

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
 * <p>Endpoint for retrieving unique maintainers</p>
 *
 * <p>Used for maintainer filtering</p>
 */
@Api
@Path("/maintainers")
@Produces(MediaType.APPLICATION_JSON)
public class MaintainersEndpoint {

  private Logger logger = LoggerFactory.getLogger(CategoriesEndpoint.class);

  @Inject
  private DatastoreService datastoreService;

  @GET
  public Maintainers getMaintainers() {
    try {
      return datastoreService.getMaintainers();
    } catch (Exception e) {
      logger.error("Problem getting maintainers", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
