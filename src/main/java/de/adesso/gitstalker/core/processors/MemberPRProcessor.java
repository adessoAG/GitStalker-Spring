package de.adesso.gitstalker.core.processors;

import de.adesso.gitstalker.core.config.Config;
import de.adesso.gitstalker.core.enums.RequestType;
import de.adesso.gitstalker.core.objects.OrganizationWrapper;
import de.adesso.gitstalker.core.objects.Query;
import de.adesso.gitstalker.core.repositories.OrganizationRepository;
import de.adesso.gitstalker.core.repositories.ProcessingRepository;
import de.adesso.gitstalker.core.repositories.RequestRepository;
import de.adesso.gitstalker.core.requests.RequestManager;
import de.adesso.gitstalker.core.resources.memberPR_Resources.*;
import lombok.NoArgsConstructor;

import java.util.*;

public class MemberPRProcessor extends ResponseProcessor {

    private RequestRepository requestRepository;
    private OrganizationRepository organizationRepository;
    private ProcessingRepository processingRepository;
    private Query requestQuery;
    private OrganizationWrapper organization;

    private HashMap<String, ArrayList<Calendar>> pullRequestsDates = new HashMap<>();
    private HashMap<String, ArrayList<String>> memberPRRepoIDs = new HashMap<>();

    public MemberPRProcessor(RequestRepository requestRepository, OrganizationRepository organizationRepository, ProcessingRepository processingRepository) {
        this.requestRepository = requestRepository;
        this.organizationRepository = organizationRepository;
        this.processingRepository = processingRepository;
    }

    /**
     * Response processing of the MemberPR request. Processing through every MemberPRRepoID and save it in a ArrayList.
     * Creating a MemberPR object containing the MemberPRRepoID ArrayList and the PageInfo wrapped into the ResponseWrapper.
     *
     * @return ResponseWrapper containing the MemberPR object.
     */
    public void processResponse(Query requestQuery) {
        this.requestQuery = requestQuery;
        this.organization = this.organizationRepository.findByOrganizationName(requestQuery.getOrganizationName());
        Data responseData = ((ResponseMemberPR) this.requestQuery.getQueryResponse()).getData();
        super.updateRateLimit(responseData.getRateLimit(), requestQuery.getQueryRequestType());
        this.processQueryResponse(responseData);
        this.processRequestForRemainingInformation(responseData.getOrganization().getMembersWithRole().getPageInfo(), this.requestQuery.getOrganizationName());
        super.doFinishingQueryProcedure(this.requestRepository, this.organizationRepository, this.processingRepository, organization, requestQuery, RequestType.MEMBER_PR);
    }

    /**
     * Creates the subsequent requests if it becomes clear during processing that information is still open in the section.
     * If finished the amount of external contributions is calculated and new requests are generated.
     * @param pageInfo Contains information required to define whether requests are still outstanding.
     * @param organizationName Organization name for creating the appropriate request
     */
    private void processRequestForRemainingInformation(PageInfo pageInfo, String organizationName) {
        if (pageInfo.isHasNextPage()) {
            this.generateNextRequests(organizationName, pageInfo.getEndCursor(), RequestType.MEMBER_PR, requestRepository);
        } else {
            organization.addMemberPRs(this.memberPRRepoIDs);
            this.processNumOfExternalRepoContributions();
            this.generateRequestsBasedOnMemberPR();
        }
    }

    /**
     * Calculates the amount of external contributions and saves it in the organization
     */
    private void processNumOfExternalRepoContributions() {
        super.calculateExternalOrganizationPullRequestsChartJSData(organization, this.pullRequestsDates);
        this.organization.getOrganizationDetail().setNumOfExternalRepoContributions(super.calculateExternalRepoContributions(this.organization).size());
    }

    /**
     * Generates the following requests after the MemberPR requests are all processed. Saves the generated requests in the repository.
     */
    private void generateRequestsBasedOnMemberPR() {
        Set<String> repoIDs = super.calculateExternalRepoContributions(this.organization).keySet();
        if(!repoIDs.isEmpty()){
        while (!repoIDs.isEmpty()) {
            Set<String> subSet = new HashSet<>(new ArrayList<>(repoIDs).subList(0, Math.min(9, repoIDs.size())));
            List<String> targetList = new ArrayList<>(subSet);
            this.requestRepository.save(new RequestManager()
                    .setOrganizationName(this.requestQuery.getOrganizationName())
                    .setRepoIDs(targetList)
                    .generateRequest(RequestType.EXTERNAL_REPO));
            repoIDs.removeAll(subSet);
        }
        } else organization.addFinishedRequest(RequestType.EXTERNAL_REPO);
    }

    /**
     * Processes the response from the requests
     * @param responseData Response information from the requests
     */
    private void processQueryResponse(Data responseData) {
        Members members = responseData.getOrganization().getMembersWithRole();
        for (NodesMember nodes : members.getNodes()) {
            for (NodesPR pullRequests : nodes.getPullRequests().getNodes()) {
                if (!pullRequests.getRepository().isFork() && checkIfPullRequestIsActiveSinceOneYear(pullRequests.getUpdatedAt().getTime())) {
                    if (memberPRRepoIDs.containsKey(pullRequests.getRepository().getId())) {
                        if (!memberPRRepoIDs.get(pullRequests.getRepository().getId()).contains(nodes.getId())) {
                            memberPRRepoIDs.get(pullRequests.getRepository().getId()).add(nodes.getId());
                        }
                        if (new Date(System.currentTimeMillis() - Config.PAST_DAYS_TO_CRAWL_IN_MS).getTime() < pullRequests.getUpdatedAt().getTime().getTime()) {
                            if (pullRequestsDates.containsKey(pullRequests.getRepository().getId())) {
                                pullRequestsDates.get(pullRequests.getRepository().getId()).add(pullRequests.getUpdatedAt());
                            } else
                                pullRequestsDates.put(pullRequests.getRepository().getId(), new ArrayList<>(Arrays.asList(pullRequests.getUpdatedAt())));
                        }


                    } else {
                        ArrayList<String> contributorIDs = new ArrayList<>();
                        contributorIDs.add(nodes.getId());
                        memberPRRepoIDs.put(pullRequests.getRepository().getId(), contributorIDs);
                    }
                }

            }
        }
    }

    /**
     * Checks if the selected updatedDate is within one year.
     * @param updatedDate Date to check.
     * @return Boolean if it's within one year or not.
     */
    private boolean checkIfPullRequestIsActiveSinceOneYear(Date updatedDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        Date oneYearAgo = calendar.getTime();

        return oneYearAgo.getTime() < updatedDate.getTime();
    }
}
