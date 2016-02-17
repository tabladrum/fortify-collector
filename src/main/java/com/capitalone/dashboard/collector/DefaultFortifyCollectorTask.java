package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CodeQuality;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.FortifyCollector;
import com.capitalone.dashboard.model.FortifyProject;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.CodeQualityRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.FortifyCollectorRepository;
import com.capitalone.dashboard.repository.FortifyProjectRepository;
import com.fortify.ws.client.FortifyWebServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DefaultFortifyCollectorTask extends
		CollectorTask<FortifyCollector> {
	private static final Log LOG = LogFactory
			.getLog(DefaultFortifyCollectorTask.class);
	private final FortifyProjectRepository fortifyProjectRepository;
	private final FortifyCollectorRepository fortifyCollectorRepository;
	private final DefaultFortifyClient fortifyClient;
	private final ComponentRepository dbComponentRepository;
	private final FortifySettings fortifySettings;
	private CodeQualityRepository codeQualityRepository;

	@Autowired
	public DefaultFortifyCollectorTask(TaskScheduler taskScheduler,
			FortifyProjectRepository fortifyProjectRepository,
			FortifyCollectorRepository fortifyCollectorRepository,
			CodeQualityRepository codeQualityRepository,
			FortifySettings fortifySettings,
			DefaultFortifyClient fortifyClient,
			ComponentRepository dbComponentRepository) {
		super(taskScheduler, "Fortify");
		this.fortifyCollectorRepository = fortifyCollectorRepository;
		this.fortifyProjectRepository = fortifyProjectRepository;
		this.codeQualityRepository = codeQualityRepository;
		this.fortifySettings = fortifySettings;
		this.fortifyClient = fortifyClient;
		this.dbComponentRepository = dbComponentRepository;
	}


	@Override
	public FortifyCollector getCollector() {
		return FortifyCollector.prototype(fortifySettings.getServer());
	}

	@Override
	public String getCron() {
		return fortifySettings.getCron();
	}

	@Override
	public BaseCollectorRepository<FortifyCollector> getCollectorRepository() {
		// TODO Auto-generated method stub
		return this.fortifyCollectorRepository;
	}

	@Override
	public void collect(FortifyCollector collector) {

		long start = System.currentTimeMillis();

		logBanner(collector.getFortifyServer());

		clean(collector);
		List<FortifyProject> projects = null;
		try {
			projects = fortifyClient.listProjectVersions(
					collector.getFortifyServer(), fortifySettings.getToken());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FortifyWebServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int projSize = ((projects != null) ? projects.size() : 0);
		log("Fetched projects   " + projSize, start, 0);

		addNewProjects(projects, collector);

		refreshData(enabledProjects(collector, collector.getFortifyServer()));

		log("Finished", start, 0);

	}

	/**
	 * Clean up unused fortify collector items
	 *
	 * @param collector
	 */

	private void clean(FortifyCollector collector) {
		Set<ObjectId> uniqueIDs = new HashSet<>();
		for (com.capitalone.dashboard.model.Component comp : dbComponentRepository
				.findAll()) {
			if (comp.getCollectorItems() != null && !comp.getCollectorItems().isEmpty()) {
				List<CollectorItem> itemList = comp.getCollectorItems().get(
						CollectorType.StaticSecurityScan);
				if (itemList != null) {
					for (CollectorItem ci : itemList) {
						if (ci != null && ci.getCollectorId().equals(collector.getId())){
							uniqueIDs.add(ci.getId());
						}
					}
				}
			}
		}
		List<FortifyProject> jobList = new ArrayList<>();
		Set<ObjectId> udId = new HashSet<>();
		udId.add(collector.getId());
		for (FortifyProject job : fortifyProjectRepository.findByCollectorIdIn(udId)) {
			if (job != null) {
				job.setEnabled(uniqueIDs.contains(job.getId()));
				jobList.add(job);
			}
		}
		fortifyProjectRepository.save(jobList);
	}
	
    private void refreshData(List<FortifyProject> fortifyProjects) {
        long start = System.currentTimeMillis();
        int count = 0;

        
        for (FortifyProject project : fortifyProjects) {
        	try {
        	
            CodeQuality codeQuality = fortifyClient.analyse(fortifySettings.getServer(), fortifySettings.getToken(), project);

            if ((codeQuality != null) && isNewQualityData(project, codeQuality)) {
                codeQuality.setCollectorItemId(project.getId());
                codeQualityRepository.save(codeQuality);
                count++;
            } } catch (IllegalStateException ise) {
            	ise.printStackTrace();
            	LOG.warn("Unable to analyze Fortify Project: " + project.getProjectName() + ". Possibly missing scan report on Fortify Server.");
            }
        }

        log("Updated", start, count);
    }
    
    private boolean isNewQualityData(FortifyProject project, CodeQuality codeQuality) {
        return codeQualityRepository.findByCollectorItemIdAndTimestamp(
                project.getId(), codeQuality.getTimestamp()) == null;
    }
    
    
	private List<FortifyProject> enabledProjects(FortifyCollector collector,
			String instanceUrl) {
		return fortifyProjectRepository.findEnabledProjects(collector.getId(),
				instanceUrl);
	}

	private void addNewProjects(List<FortifyProject> projects,
			FortifyCollector collector) {
		long start = System.currentTimeMillis();
		int count = 0;

		for (FortifyProject project : projects) {
			if (isNewProject(collector, project)) {
				project.setCollectorId(collector.getId());
				project.setEnabled(false);
				project.setDescription(project.getProjectName());
				fortifyProjectRepository.save(project);
				count++;
			}
		}
		log("New projects", start, count);
	}
// Another change
	private boolean isNewProject(FortifyCollector collector,
			FortifyProject project) {
		return fortifyProjectRepository.findFortifyProject(collector.getId(),
				project.getInstanceUrl(), project.getProjectId()) == null;
	}

}
