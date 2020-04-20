package io.jenkins.plugins;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;

@Provider
@PreMatching
public class Filters implements ContainerRequestFilter {
  @Override
  public void filter(ContainerRequestContext reqContext) throws IOException {
    UriInfo uriInfo = reqContext.getUriInfo();

    // remove top level /api incase people use it directly
    URI requestUri = uriInfo.getBaseUriBuilder().path( uriInfo.getPath().replaceFirst("^api/", "") ).build();

    reqContext.setRequestUri( requestUri );
  }
}
