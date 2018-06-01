package Tasks;

import enums.RequestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import repositories.OrganizationRepository;
import repositories.RequestRepository;
import requests.RequestManager;

public class OrganizationUpdateTask {

    @Autowired
    RequestRepository requestRepository;

    /**
     * Scheduled task for generating queries in order to update saved organisation.
     * Currently used for debugging the requests.
     * TODO: Scheduled task should run at night time!
     */
    @Scheduled(fixedDelay = 300000000, initialDelay = 20000)
    private void generateQuery() {
        requestRepository.save(new RequestManager("adessoAG").generateRequest(RequestType.ORGANIZATION_DETAIL));
        requestRepository.save(new RequestManager("google").generateRequest(RequestType.REPOSITORY_ID));
    }
}
