
package com.capitalone.dashboard.model;

import org.codehaus.staxmate.SMInputFactory;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;

public final class FortifyUtils {
  private static final double BLOCKER_SEVERITY_THRESHOLD = 4.0;
  private static final double CRITICAL_SEVERITY_THRESHOLD = 3.0;
  private static final double MAJOR_SEVERITY_THRESHOLD = 2.0;
  private static final double MINOR_SEVERITY_THRESHOLD = 1.0;

  private FortifyUtils() {
    // only static stuff
  }

  public static SMInputFactory newStaxParser() throws FactoryConfigurationError {
    XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    // just so it won't try to load DTD in if there's DOCTYPE
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    return new SMInputFactory(xmlFactory);
  }

  public static String fortifySeveritySimple(String fortifySeverity) {
    String severity;
    Double level = Double.valueOf(fortifySeverity);
    if (level >= FortifyUtils.BLOCKER_SEVERITY_THRESHOLD) {
      severity = Severity.BLOCKER;
    } else if (level >= FortifyUtils.CRITICAL_SEVERITY_THRESHOLD) {
      severity = Severity.CRITICAL;
    } else if (level >= FortifyUtils.MAJOR_SEVERITY_THRESHOLD) {
      severity = Severity.MAJOR;
    } else if (level >= FortifyUtils.MINOR_SEVERITY_THRESHOLD) {
      severity = Severity.MINOR;
    } else {
      severity = Severity.MINOR;
    }
    return severity;

  }
}
