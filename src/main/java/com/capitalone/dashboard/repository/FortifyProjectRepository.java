package com.capitalone.dashboard.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.capitalone.dashboard.model.FortifyProject;
import com.capitalone.dashboard.repository.BaseCollectorItemRepository;


public interface FortifyProjectRepository extends BaseCollectorItemRepository<FortifyProject> {

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, options.projectId : ?2}")
    FortifyProject findFortifyProject(ObjectId collectorId, String instanceUrl, String projectId);

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, enabled: true}")
    List<FortifyProject> findEnabledProjects(ObjectId collectorId, String instanceUrl);
}
