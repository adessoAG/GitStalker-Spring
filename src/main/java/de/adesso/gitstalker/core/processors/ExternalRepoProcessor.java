package de.adesso.gitstalker.core.processors;

import de.adesso.gitstalker.core.config.Config;
import de.adesso.gitstalker.core.enums.RequestType;
import de.adesso.gitstalker.core.objects.*;
import de.adesso.gitstalker.core.repositories.OrganizationRepository;
import de.adesso.gitstalker.core.repositories.RequestRepository;
import de.adesso.gitstalker.core.resources.externalRepo_Resources.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ExternalRepoProcessor extends ResponseProcessor {

    private RequestRepository requestRepository;
    private OrganizationRepository organizationRepository;
    private Query requestQuery;
    private OrganizationWrapper organization;

    private HashMap<String, Repository> repositoriesMap = new HashMap<>();

    public ExternalRepoProcessor() {
    }

    protected void setUp(Query requestQuery, RequestRepository requestRepository, OrganizationRepository organizationRepository) {
        this.requestQuery = requestQuery;
        this.requestRepository = requestRepository;
        this.organizationRepository = organizationRepository;
        this.organization = this.organizationRepository.findByOrganizationName(requestQuery.getOrganizationName());
    }

    public void processResponse(Query requestQuery, RequestRepository requestRepository, OrganizationRepository organizationRepository) {
        this.setUp(requestQuery, requestRepository, organizationRepository);
        Data repositoriesData = this.requestQuery.getQueryResponse().getResponseExternalRepository().getData();

        super.updateRateLimit(repositoriesData.getRateLimit(), requestQuery.getQueryRequestType());
        this.processQueryResponse(repositoriesData.getNodes());
        this.processExternalReposAndFindContributors(organization, requestQuery);
        super.doFinishingQueryProcedure(this.requestRepository, this.organizationRepository, organization, requestQuery, RequestType.EXTERNAL_REPO);
    }

    protected void processExternalReposAndFindContributors(OrganizationWrapper organization, Query requestQuery) {
        if (super.checkIfQueryIsLastOfRequestType(organization, requestQuery, RequestType.EXTERNAL_REPO, this.requestRepository)) {
            this.organization.addExternalRepos(this.repositoriesMap);
            HashMap<String, ArrayList<String>> externalRepos = super.calculateExternalRepoContributions(organization);
            for (String externalRepoID : externalRepos.keySet()) {
                for (String contributorID : externalRepos.get(externalRepoID)) {
                    Repository suitableExternalRepo = organization.getExternalRepos().containsKey(externalRepoID) ? organization.getExternalRepos().get(externalRepoID) : null;
                    if (suitableExternalRepo != null) {
                        if (suitableExternalRepo.getContributor() != null) {
                            suitableExternalRepo.addContributor(organization.getMembers().containsKey(contributorID) ? organization.getMembers().get(contributorID) : null);
                        } else {
                            ArrayList<Member> contributors = new ArrayList<>();
                            contributors.add(organization.getMembers().containsKey(contributorID) ? organization.getMembers().get(contributorID) : null);
                            suitableExternalRepo.setContributor(contributors);
                        }
                    }
                }
            }
        }
    }

    protected void processQueryResponse(ArrayList<NodesRepositories> repositories){
        ArrayList<Calendar> pullRequestDates = new ArrayList<>();
        ArrayList<Calendar> issuesDates = new ArrayList<>();
        ArrayList<Calendar> commitsDates = new ArrayList<>();

        for (NodesRepositories repo : repositories) {
            int stars = repo.getStargazers().getTotalCount();
            int forks = repo.getForkCount();
            String url = repo.getUrl();
            String license = getLicense(repo);
            String programmingLanguage = getProgrammingLanguage(repo);
            String description = getDescription(repo);
            String name = repo.getName();
            String id = repo.getId();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DATE, cal.get(Calendar.DATE)-Config.PAST_DAYS_AMOUNT_TO_CRAWL);

            for (NodesPullRequests nodesPullRequests : repo.getPullRequests().getNodes()) {
                if (cal.before(nodesPullRequests.getCreatedAt())) {
                    pullRequestDates.add(nodesPullRequests.getCreatedAt());
                }
            }
            for (NodesIssues nodesIssues : repo.getIssues().getNodes()) {
                if (cal.before(nodesIssues.getCreatedAt())) {
                    issuesDates.add(nodesIssues.getCreatedAt());
                }
            }
            if (repo.getDefaultBranchRef() != null) {
                for (NodesHistory nodesHistory : repo.getDefaultBranchRef().getTarget().getHistory().getNodes()) {
                    commitsDates.add(nodesHistory.getCommittedDate());
                }
            }
            this.repositoriesMap.put(id, new Repository(name, url, description, programmingLanguage, license, forks, stars, this.generateChartJSData(commitsDates), this.generateChartJSData(issuesDates), this.generateChartJSData(pullRequestDates)));
        }
    }
    protected String getLicense(NodesRepositories repo) {
        if (repo.getLicenseInfo() == null) return "";
        else return repo.getLicenseInfo().getName();
    }

    protected String getProgrammingLanguage(NodesRepositories repo) {
        if (repo.getPrimaryLanguage() == null) return "";
        else return repo.getPrimaryLanguage().getName();
    }

    protected String getDescription(NodesRepositories repo) {
        if (repo.getDescription() == null) return "";
        else return repo.getDescription();
    }

    public RequestRepository getRequestRepository() {
        return requestRepository;
    }

    public void setRequestRepository(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public OrganizationRepository getOrganizationRepository() {
        return organizationRepository;
    }

    public void setOrganizationRepository(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Query getRequestQuery() {
        return requestQuery;
    }

    public void setRequestQuery(Query requestQuery) {
        this.requestQuery = requestQuery;
    }

    public OrganizationWrapper getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationWrapper organization) {
        this.organization = organization;
    }

    public HashMap<String, Repository> getRepositoriesMap() {
        return repositoriesMap;
    }

    public void setRepositoriesMap(HashMap<String, Repository> repositoriesMap) {
        this.repositoriesMap = repositoriesMap;
    }
}