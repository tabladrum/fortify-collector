
package com.capitalone.dashboard.model;

public class FormatVersion implements Comparable<FormatVersion> {
  private final String versionString;
  private final String[] versionParts;

  public FormatVersion(String versionString) {
    this.versionString = versionString;
    this.versionParts = versionString.split("\\.");
  }

  @Override
  public int compareTo(FormatVersion o) {
    int length = Math.max(this.versionParts.length, o.versionParts.length);
    for (int i = 0; i < length; i++) {
      int version1 = i < this.versionParts.length ?
        Integer.valueOf(this.versionParts[i]).intValue() : 0;
      int version2 = i < o.versionParts.length ?
        Integer.valueOf(o.versionParts[i]).intValue() : 0;
      if (version1 < version2) {
        return -1;
      }
      if (version1 > version2) {
        return 1;
      }
    }
    return 0;
  }

  @Override
  public String toString() {
    return this.versionString;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof FormatVersion && this.versionString.equals(((FormatVersion) o).versionString);
  }

  @Override
  public int hashCode() {
    return this.versionString.hashCode();
  }
}
