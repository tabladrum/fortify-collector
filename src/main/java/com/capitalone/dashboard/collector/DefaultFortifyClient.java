package com.capitalone.dashboard.collector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.capitalone.dashboard.model.CodeQuality;
import com.capitalone.dashboard.model.CodeQualityMetric;
import com.capitalone.dashboard.model.CodeQualityMetricStatus;
import com.capitalone.dashboard.model.CodeQualityType;
import com.capitalone.dashboard.model.FortifyConstants;
import com.capitalone.dashboard.model.FortifyProject;
import com.capitalone.dashboard.element.Fvdl;
import com.capitalone.dashboard.element.Severity;
import com.capitalone.dashboard.element.Vulnerability;
import com.fortify.manager.schema.Project;
import com.fortify.manager.schema.ProjectIdentifier;
import com.fortify.manager.schema.ProjectVersionLite;
import com.fortify.manager.schema.Status;
import com.fortify.ws.client.FPRTransferClient;
import com.fortify.ws.client.FortifyWebServiceException;
import com.fortify.ws.client.ProjectClient;
import com.fortify.ws.client.ProjectVersionClient;
import com.fortify.ws.core.WSAuthenticationProvider;
import com.fortify.ws.core.util.PasswordAuthenticationProvider;
import com.fortify.ws.core.util.TokenAuthenticationProvider;

@Component
public class DefaultFortifyClient {
	private static final String F360_WS_PATH = "/fm-ws/services";
	private String urls;
	private static WSAuthenticationProvider auth = null;

	public DefaultFortifyClient() {

	}

	private static WSAuthenticationProvider getAuth(String token) {
		auth = new TokenAuthenticationProvider(token);
		return auth;
	}

	private static WSAuthenticationProvider getAuth(String user, String password) {
		auth = new PasswordAuthenticationProvider(user, password);
		return auth;
	}

	private static File getFile(String filename) {
		File f = new File(filename);
		return f;
	}

	public String getUri() {
		return this.urls + F360_WS_PATH;
	}

	public ArrayList<FortifyProject> listProjectVersions(String sscServer,
			String token) throws IOException, FortifyWebServiceException {

		ArrayList<FortifyProject> returnProjects = new ArrayList<FortifyProject>();
		FortifyWSTemplate template = new FortifyWSTemplate(sscServer);
		ProjectClient projectClient = new ProjectClient(template,
				getAuth(token), null);
		
		
		List<Project> projects = projectClient.getProjects();

		Map<Long, String> nameMap = new HashMap<Long, String>();
		for (Project project : projects) {
			
			nameMap.put(new Long(project.getId()), project.getName());
		}

		ProjectVersionClient projectVersionClient = new ProjectVersionClient(
				template, getAuth(token), null);
		List<ProjectVersionLite> projectVersions = projectVersionClient
				.getProjectVersions();

		Iterator<ProjectVersionLite> iter = projectVersions.iterator();
		while (iter.hasNext()) {
			ProjectVersionLite pv = (ProjectVersionLite) iter.next();
			FortifyProject fp = new FortifyProject();
			fp.setInstanceUrl(sscServer);
			fp.setEnabled(false);
			fp.setProjectId(String.valueOf(pv.getId()));
			fp.setProjectName((String) nameMap.get(new Long(pv.getProjectId())));
			fp.setProjectVersion(pv.getName());
			returnProjects.add(fp);
			System.out.println(pv.getId() + ","
					+ (String) nameMap.get(new Long(pv.getProjectId())) + ","
					+ pv.getName());
		}
		return returnProjects;
	}

	public int downloadFPR(String server, String token,
			ProjectIdentifier projectVersionID, String fileName,
			boolean includeSource) throws FortifyWebServiceException,
			IOException {
		FortifyWSTemplate template = new FortifyWSTemplate(server);
		File f = getFile(fileName);
		System.out.println("Filename=" + f.getAbsolutePath());
		boolean shouldIncludeSource = includeSource;
		FPRTransferClient xferClient = new FPRTransferClient(template,
				getAuth(token), null);
		
		Status status = xferClient.downloadFPR(f, projectVersionID);
		if ((status.getMsg() != null) && (!status.getMsg().equals("")))
			System.out.println(status.getMsg());
		else if (status.getCode() == 0L)
			System.out.println("Successfully downloaded file to "
					+ f.getAbsolutePath());
		else {
			System.out.println("unknown response");
		}
		return (int) status.getCode();
	}

	private InputStream getInputStreamFromFprFile(File file) throws IOException {
		final ZipFile fprFile = new ZipFile(file);
		try {
			final InputStream reportStream = fprFile.getInputStream(fprFile
					.getEntry(FortifyConstants.AUDIT_FVDL_FILE));
			return new InputStream() {
				@Override
				public int read() throws IOException {
					return reportStream.read();
				}

				@Override
				public void close() throws IOException {
					try {
						reportStream.close();
					} finally {
						fprFile.close();
					}
				}
			};
		} catch (IOException e) {
			fprFile.close();
			throw e;
		}
	}

	InputStream getInputStream(String filename) throws IOException {
		File file = new File(filename);
		if (file == null)
			throw new FileNotFoundException();
		return getInputStreamFromFprFile(file);

	}

	/**
	 * @param server
	 * @param token
	 * @param project
	 */

	public CodeQuality analyse(String server, String token,
			FortifyProject project) {
		ProjectIdentifier id = new ProjectIdentifier();
		id.setProjectVersionId(Long.valueOf(project.getProjectId()));

		CodeQuality quality = new CodeQuality();
		String filename = "dummy-" + project.getProjectId() + ".fpr";
		Map<String, Integer> group = new HashMap<String, Integer>();
		quality.setVersion(project.getProjectName() + "-" + project.getProjectVersion());
		quality.setTimestamp(System.currentTimeMillis());
		try {
			downloadFPR(server, token, id, filename, false);
			InputStream stream = getInputStream(filename);
			try {
				Fvdl fvdl = new FvdlStAXParser().parse(stream);
				Collection<Vulnerability> vuls = fvdl.getVulnerabilities();
				quality.setName(project.getProjectName());
				quality.setUrl(server);
				quality.setType(CodeQualityType.SecurityAnalysis);
				for (Vulnerability vul : vuls) {
					String sev = vul.getInstanceSeverity();
					if (group.get(sev) != null) {
						group.put(sev,
								Integer.valueOf(group.get(sev).intValue() + 1));
					} else {
						group.put(sev, Integer.valueOf(1));
					}
					System.out.println(vul.toString());
				}

				Set<String> keys = group.keySet();
				for (String key : keys) {
					CodeQualityMetric metric = new CodeQualityMetric(key);
					metric.setFormattedValue(group.get(key).toString());
					switch (key) {
					case Severity.BLOCKER:
						metric.setStatus(CodeQualityMetricStatus.Alert);
						break;
					case Severity.CRITICAL:
						metric.setStatus(CodeQualityMetricStatus.Alert);
						break;
					case Severity.MAJOR:
						metric.setStatus(CodeQualityMetricStatus.Warning);
						break;
					case Severity.MINOR:
						metric.setStatus(CodeQualityMetricStatus.Ok);
						break;					
					default:
						metric.setStatus(CodeQualityMetricStatus.Ok);
						break;
					}
					
					quality.getMetrics().add(metric);
				}
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				stream.close();
				File file = new File(filename);
				file.delete();
			}
		} catch (FortifyWebServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return quality;
	}

}
