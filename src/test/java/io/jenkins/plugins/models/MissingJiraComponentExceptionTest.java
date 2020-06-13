package io.jenkins.plugins.models;

import org.junit.Test;

import static org.junit.Assert.*;

public class MissingJiraComponentExceptionTest {
  @Test
  public void testMessage() {
    MissingJiraComponentException sot = new MissingJiraComponentException("plugin-git", "component-git");
    assertEquals(
      "ComponentName(component-git) and PluginName(plugin-git) do not match.",
      sot.getMessage()
    );
  }

  @Test
  public void testEquals() {
    MissingJiraComponentException sot1 = new MissingJiraComponentException("plugin-git", "component-git");
    MissingJiraComponentException sot2 = new MissingJiraComponentException("plugin-git", "component-git");
    assertEquals(
      sot1,
      sot2
    );
  }

}
