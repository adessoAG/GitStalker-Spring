package de.adesso.gitstalker.core.Tasks;

import de.adesso.gitstalker.core.REST.OrganizationController;
import de.adesso.gitstalker.core.REST.responses.ProcessingOrganization;
import de.adesso.gitstalker.core.config.Config;
import de.adesso.gitstalker.core.config.RateLimitConfig;
import de.adesso.gitstalker.core.enums.RequestStatus;
import de.adesso.gitstalker.core.enums.RequestType;
import de.adesso.gitstalker.core.objects.OrganizationWrapper;
import de.adesso.gitstalker.core.objects.Query;
import de.adesso.gitstalker.core.repositories.OrganizationRepository;
import de.adesso.gitstalker.core.repositories.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Map;

public class RequestProcessorTask {

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    /**
     * Scheduled task checking for queries without crawled information.
     * After picking one query the request starts with the specified information out of the selected query. After the request the query is saved in the repository with the additional response data.
     */
    @Scheduled(fixedRate = Config.PROCESSING_RATE_IN_MS)
    private void crawlQueryData() {
        ArrayList<Query> queriesToProcess;
        String organizationName;

        if (!OrganizationController.processingOrganizations.isEmpty()) {
            Map.Entry<String, ProcessingOrganization> processingOrganization = OrganizationController.processingOrganizations.entrySet().iterator().next();
            organizationName = processingOrganization.getKey();
            queriesToProcess = this.requestRepository.findByQueryStatusAndOrganizationName(RequestStatus.CREATED, organizationName);
        } else queriesToProcess = this.requestRepository.findByQueryStatus(RequestStatus.CREATED);

        if (!queriesToProcess.isEmpty() && RateLimitConfig.getRemainingRateLimit() != 0) {
            Query queryToProcess = this.findProcessableQueryByRequestCostAndPriority(queriesToProcess);
            this.processQuery(queryToProcess);
        }
    }

    private boolean checkIfPrioritizedRequestsAreFinished(Query createdQuery) {
        OrganizationWrapper organization = this.organizationRepository.findByOrganizationName(createdQuery.getOrganizationName());
        switch (createdQuery.getQueryRequestType()) {
            case TEAM:
                return organization.getFinishedRequests().contains(RequestType.MEMBER) && organization.getFinishedRequests().contains(RequestType.REPOSITORY);
            case MEMBER_PR:
                return organization.getFinishedRequests().contains(RequestType.REPOSITORY);
            case CREATED_REPOS_BY_MEMBERS:
                return organization.getFinishedRequests().contains(RequestType.MEMBER);
        }
        return true;
    }

    private Query findProcessableQueryByRequestCostAndPriority(ArrayList<Query> processingQueries) {
        for (Query createdQuery : processingQueries) {
            if (RateLimitConfig.getRemainingRateLimit() - createdQuery.getEstimatedQueryCost() >= 0 && checkIfPrioritizedRequestsAreFinished(createdQuery)) {
                    return createdQuery;
            }
        }
        return null;
    }

    private void processQuery(Query queryToProcess) {
        this.requestRepository.delete(queryToProcess);
        queryToProcess.crawlQueryResponse();
        this.requestRepository.save(queryToProcess);
    }
}
