
package com.capitalone.dashboard.collector;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.xml.sax.SAXException;


import com.capitalone.dashboard.model.Build;
import com.capitalone.dashboard.model.Description;
import com.capitalone.dashboard.model.FortifyUtils;
import com.capitalone.dashboard.model.Fvdl;
import com.capitalone.dashboard.model.ReplacementDefinition;
import com.capitalone.dashboard.model.Vulnerability;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class FvdlStAXParser {
  Fvdl parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {

    SMInputFactory inputFactory = FortifyUtils.newStaxParser();
    try {
      SMHierarchicCursor rootC = inputFactory.rootElementCursor(inputStream);
      rootC.advance(); // <FVDL>

      SMInputCursor childCursor = rootC.childCursor();

      Build build = null;
      Collection<Description> descriptions = new ArrayList<Description>();
      Collection<Vulnerability> vulnerabilities = null;

      while (childCursor.getNext() != null) {
        String nodeName = childCursor.getLocalName();

        if ("Build".equals(nodeName)) {
          build = processBuild(childCursor);
        } else if ("Description".equals(nodeName)) {
          descriptions.add(processDescription(childCursor));
        } else if ("Vulnerabilities".equals(nodeName)) {
          vulnerabilities = processVulnerabilities(childCursor);
        }
      }

      return new Fvdl(build, descriptions, vulnerabilities);

    } catch (XMLStreamException e) {
      throw new IllegalStateException("XML is not valid", e);
    }
  }

  private Collection<Vulnerability> processVulnerabilities(SMInputCursor vulnsC) throws XMLStreamException {
    Collection<Vulnerability> vulnerabilities = new ArrayList<Vulnerability>();
    SMInputCursor vulnCursor = vulnsC.childElementCursor("Vulnerability");
    while (vulnCursor.getNext() != null) {
      vulnerabilities.add(processVulnerability(vulnCursor));
    }
    return vulnerabilities;
  }

  private Vulnerability processVulnerability(SMInputCursor vulnCursor) throws XMLStreamException {
    Vulnerability vulnerability = new Vulnerability();
    SMInputCursor childCursor = vulnCursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();

      if ("ClassInfo".equals(nodeName)) {
        processClassInfo(childCursor, vulnerability);
      } else if ("InstanceInfo".equals(nodeName)) {
        processInstanceInfo(childCursor, vulnerability);
      } else if ("AnalysisInfo".equals(nodeName)) {
        processAnalysisInfo(childCursor, vulnerability);
      }
    }
    return vulnerability;
  }

  private void processAnalysisInfo(SMInputCursor paCursor, Vulnerability vulnerability) throws XMLStreamException {
    SMInputCursor unifiedCursor = paCursor.childElementCursor("Unified");
    if (unifiedCursor.getNext() != null) {
      SMInputCursor childCursor = unifiedCursor.childCursor();
      while (childCursor.getNext() != null) {
        String nodeName = childCursor.getLocalName();

        if ("Trace".equals(nodeName)) {
          processTrace(childCursor, vulnerability);
        } else if ("ReplacementDefinitions".equals(nodeName)) {
          processReplacementDefinitions(childCursor, vulnerability);
        }
      }
    }
  }

  private void processReplacementDefinitions(SMInputCursor repDefsCursor, Vulnerability vulnerability) throws XMLStreamException {
    SMInputCursor repDefCursor = repDefsCursor.childElementCursor("Def");
    while (repDefCursor.getNext() != null) {
      vulnerability.addReplacementDefinition(new ReplacementDefinition(repDefCursor.getAttrValue("key"), repDefCursor.getAttrValue("value")));
    }

  }

  private void processTrace(SMInputCursor traceCursor, Vulnerability vulnerability) throws XMLStreamException {
    SMInputCursor primaryCursor = traceCursor.childElementCursor("Primary");
    if (primaryCursor.getNext() != null) {
      SMInputCursor entryCursor = primaryCursor.childElementCursor("Entry");
      while (entryCursor.getNext() != null) {
        processNode(vulnerability, entryCursor);
      }
    }
  }

  private void processNode(Vulnerability vulnerability, SMInputCursor entryCursor) throws XMLStreamException {
    SMInputCursor nodeCursor = entryCursor.childElementCursor("Node");
    if (nodeCursor.getNext() != null && "true".equals(nodeCursor.getAttrValue("isDefault"))) {
      SMInputCursor sourceLocationCursor = nodeCursor.childElementCursor("SourceLocation");
      if (sourceLocationCursor.getNext() != null) {
        vulnerability.setPath(sourceLocationCursor.getAttrValue("path"));
        vulnerability.setLine(Integer.valueOf(sourceLocationCursor.getAttrValue("line")));
      }
    }
  }

  private void processInstanceInfo(SMInputCursor instanceInfoCursor, Vulnerability vulnerability) throws XMLStreamException {
    SMInputCursor childCursor = instanceInfoCursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();

      if ("InstanceID".equals(nodeName)) {
        vulnerability.setInstanceID(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("InstanceSeverity".equals(nodeName)) {
        vulnerability.setInstanceSeverity(FortifyUtils.fortifySeveritySimple(StringUtils.trim(childCursor.collectDescendantText(false))));
      }
    }
  }

  private void processClassInfo(SMInputCursor classInfoCursor, Vulnerability vulnerability) throws XMLStreamException {
    SMInputCursor childCursor = classInfoCursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();

      if ("ClassID".equals(nodeName)) {
        vulnerability.setClassID(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("Kingdom".equals(nodeName)) {
        vulnerability.setKingdom(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("Type".equals(nodeName)) {
        vulnerability.setType(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("Subtype".equals(nodeName)) {
        vulnerability.setSubtype(StringUtils.trim(childCursor.collectDescendantText(false)));
      }
    }
  }

  private Description processDescription(SMInputCursor descC) throws XMLStreamException {
    Description description = new Description();
    description.setClassID(descC.getAttrValue("classID"));
    SMInputCursor abstractCursor = descC.childElementCursor("Abstract");
    if (abstractCursor.getNext() != null) {
      description.setAbstract(StringUtils.trim(abstractCursor.collectDescendantText(false)));
    }
    return description;
  }

  private Build processBuild(SMInputCursor buildC) throws XMLStreamException {
    SMInputCursor childCursor = buildC.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();

      if (StringUtils.equalsIgnoreCase("SourceBasePath", nodeName)) {
        String sourceBasePath = StringUtils.trim(childCursor.collectDescendantText(false));
        return new Build(sourceBasePath);
      }
    }

    return null;
  }
}
