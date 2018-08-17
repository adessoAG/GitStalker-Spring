package processors;

import enums.RequestType;
import objects.OrganizationWrapper;
import objects.Query;
import repositories.OrganizationRepository;
import repositories.RequestRepository;
import requests.RequestManager;


public class OrganizationValidationProcessor extends ResponseProcessor {

    private RequestRepository requestRepository;
    private OrganizationRepository organizationRepository;
    private Query requestQuery;
    private OrganizationWrapper organization;

    public OrganizationValidationProcessor() {
    }

    private void setUp(Query requestQuery, RequestRepository requestRepository, OrganizationRepository organizationRepository) {
        this.requestQuery = requestQuery;
        this.requestRepository = requestRepository;
        this.organizationRepository = organizationRepository;
        this.organization = this.organizationRepository.findByOrganizationName(requestQuery.getOrganizationName());
    }

    public void processResponse(Query requestQuery, RequestRepository requestRepository, OrganizationRepository organizationRepository) {
        this.setUp(requestQuery, requestRepository, organizationRepository);
        if (this.processQueryResponse()) {
            this.organization = this.generateOrganizationWrapper(this.requestQuery.getOrganizationName());
            this.requestRepository.saveAll(new RequestManager(this.requestQuery.getOrganizationName()).generateAllRequests());
            super.updateRateLimit(this.requestQuery.getQueryResponse().getResponseOrganizationValidation().getData().getRateLimit(), requestQuery.getQueryRequestType());
            super.doFinishingQueryProcedure(this.requestRepository, this.organizationRepository, this.organization, this.requestQuery, RequestType.ORGANIZATION_VALIDATION);
        }
    }

    private OrganizationWrapper generateOrganizationWrapper(String organizationName) {
        if (this.organization != null) {
            return this.organization;
        } else return new OrganizationWrapper(organizationName);
    }

    private boolean processQueryResponse() {
        return this.requestQuery.getQueryResponse().getResponseOrganizationValidation().getData().getOrganization() != null;
    }
}
