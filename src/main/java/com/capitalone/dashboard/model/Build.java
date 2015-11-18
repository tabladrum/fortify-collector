
package com.capitalone.dashboard.model;

public class Build {
  private final String sourceBasePath;

  public Build(String sourceBasePath) {
    this.sourceBasePath = sourceBasePath;
  }

  public String getSourceBasePath() {
    return this.sourceBasePath;
  }

}
