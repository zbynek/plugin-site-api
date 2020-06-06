package io.jenkins.plugins;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Provider
@PreMatching
public class Filters implements ContainerRequestFilter {
  @Override
  public void filter(ContainerRequestContext reqContext) throws IOException {
    UriInfo uriInfo = reqContext.getUriInfo();

    // remove top level /api incase people use it directly
    if (uriInfo.getPath().startsWith("api/")) {
      MultivaluedMap<String, String> queryParametersMultiMap = uriInfo.getQueryParameters();
      UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
      baseUriBuilder.path(uriInfo.getPath().replaceFirst("^api/", ""));
      baseUriBuilder.replaceQuery("");
      for (Map.Entry<String, List<String>> queryEntry : queryParametersMultiMap.entrySet()) {
        baseUriBuilder.queryParam(queryEntry.getKey(), queryEntry.getValue());
      }
      reqContext.setRequestUri(baseUriBuilder.build());
    }
  }
}
