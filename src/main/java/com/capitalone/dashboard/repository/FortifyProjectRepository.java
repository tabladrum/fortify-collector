package com.capitalone.dashboard.repository;

import com.capitalone.dashboard.model.FortifyProject;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface FortifyProjectRepository extends BaseCollectorItemRepository<FortifyProject>{

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, options.projectId : ?2}")
    FortifyProject findFortifyProject(ObjectId collectorId, String instanceUrl, String projectId);

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, enabled: true}")
    List<FortifyProject> findEnabledProjects(ObjectId collectorId, String instanceUrl);
}
