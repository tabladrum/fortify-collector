/*
 * Fortify Plugin for SonarQube
 * Copyright (C) 2014 Vivien HENRIET and SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.capitalone.dashboard.collector.fortify.fvdl.element;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class Fvdl {
  private final Build build;
  private final Map<String, String> descriptions = new HashMap<String, String>();
  private final Collection<Vulnerability> vulnerabilities;

  public Fvdl(Build build, Collection<Description> descriptions, Collection<Vulnerability> vulnerabilities) {
    this.build = build;
    for (Description description : descriptions) {
      this.descriptions.put(description.getClassID(), description.getAbstract());
    }
    this.vulnerabilities = vulnerabilities;
  }

  public Build getBuild() {
    return this.build;
  }

  public Collection<Vulnerability> getVulnerabilities() {
    return this.vulnerabilities;
  }

  public String getDescription(Vulnerability vulnerability) {
    String message = "No message found";
    String abstractDescription = this.descriptions.get(vulnerability.getClassID());
    if (abstractDescription != null) {
      message = abstractDescription;
      for (ReplacementDefinition replacementDefinition : vulnerability.getReplacementDefinitions()) {
        String key = replacementDefinition.getKey();
        String value = replacementDefinition.getValue();
        String regex = "<Replace key=\"" + Matcher.quoteReplacement(key) + "\"/>";
        value = Matcher.quoteReplacement(value);
        message = message.replaceAll(regex, value);
      }
      message = message.replaceAll("\\<[^>]*>", "");
    }
    return message;
  }

}
