package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.models.JiraIssues;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.services.ConfigurationService;
import io.jenkins.plugins.services.DatastoreService;
import io.jenkins.plugins.services.ServiceException;
import io.jenkins.plugins.services.WikiService;
import io.jenkins.plugins.services.impl.HttpClientJiraIssues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * <p>Endpoint for a specific plugin</p>
 *
 * <p>The major difference here compared to <code>PluginsEndpoint</code> is the result includes (if available) the wiki
 * content from the Jenkins Wiki page.</p>
 */
@Path("/plugin/{name}")
@Produces(MediaType.APPLICATION_JSON)
public class PluginEndpoint {

  private Logger logger = LoggerFactory.getLogger(PluginEndpoint.class);

  @Inject
  private DatastoreService datastoreService;

  @Inject
  private WikiService wikiService;

  @Inject
  private ConfigurationService configurationService;

  /**
   * <p>Get a plugin by name</p>
   *
   * @param name The plugin to retrieve
   * @return Matching plugin
   */
  @GET
  public Plugin getPlugin(@PathParam("name") String name) {
    try {
      final Plugin plugin = datastoreService.getPlugin(name);
      if (plugin != null) {
        if (plugin.getWiki() != null) {
          final String content = wikiService.getWikiContent(plugin.getWiki().getUrl());
          plugin.getWiki().setContent(content);
        }
        return plugin;
      } else {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } catch (ServiceException e) {
      logger.error("Problem getting plugin " + name, e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * <p>Get issues</p>
   *
   * @param name The plugin to retrieve
   * @return Matching issues
   */
  @Path("/issues/open")
  @GET
  public JiraIssues getIssue(@PathParam("name") String name) {
    try {
      final Plugin plugin = datastoreService.getPlugin(name);
      if (plugin == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      JiraIssues jiraIssues = new HttpClientJiraIssues(configurationService).getIssues(plugin.getName());
      // jiraIssues.issues = searchResult.getIssues();
      return jiraIssues;
    } catch (ServiceException | IOException e) {
      logger.error("Problem getting plugin " + name, e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
