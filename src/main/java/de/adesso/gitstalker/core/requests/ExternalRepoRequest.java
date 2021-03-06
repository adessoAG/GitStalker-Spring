package de.adesso.gitstalker.core.requests;

import de.adesso.gitstalker.core.config.Config;
import de.adesso.gitstalker.core.enums.RequestType;
import de.adesso.gitstalker.core.objects.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This is the request used for requesting ExternalRepo.
 */
public class ExternalRepoRequest {

    private final int estimatedQueryCost = 1;
    private String query;
    private String organizationName;
    private RequestType requestType;

    public ExternalRepoRequest(String organizationName, List<String> repoIDs) {
        this.organizationName = organizationName;
        /**
         * GraphQL Request for the external repositories of the organization members.
         * Requesting the first 100 repositories for each member of the organization. Checks if there is information left with the pageInfo.
         * For each repository it's collecting relevant information.
         * Requests the current rate limit of the token at the API.
         */
        this.query = "{\n" +
                //Request for ten repositories combined
                "nodes(ids: [" + formatRepoIDs(repoIDs) + "]) {\n" +
                "... on Repository {\n" +
                "url\n" +
                "id\n" +
                "name\n" +
                "description\n" +
                "forkCount\n" +
                "stargazers {\n" +
                "totalCount\n" +
                "}\n" +
                "licenseInfo {\n" +
                "name\n" +
                "}\n" +
                "primaryLanguage {\n" +
                "name\n" +
                "}\n" +
                "defaultBranchRef {\n" +
                "target {\n" +
                "... on Commit {\n" +
                "history(first: 50, since: \"" + getDateToStartCrawlingInISO8601UTC(new Date()) + "\") {\n" +
                "nodes {\n" +
                "committedDate\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "pullRequests(last: 50) {\n" +
                "nodes {\n" +
                "createdAt\n" +
                "}\n" +
                "}\n" +
                "issues(last: 50) {\n" +
                "nodes {\n" +
                "createdAt\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "rateLimit {\n" +
                "cost\n" +
                "remaining\n" +
                "resetAt\n" +
                "}\n" +
                "}";
        this.requestType = RequestType.EXTERNAL_REPO;
    }

    /**
     * Formatting for the RepoIDs to fit the API
     * @param repoIDs RepoIDs for the request
     * @return String with the formatted RepoIDs
     */
    protected String formatRepoIDs(List<String> repoIDs) {
        String formattedString = "";
        for (String repoID : repoIDs) {
            formattedString += "\"" + repoID + "\",";
        }
        return formattedString;
    }

    /**
     * Formats the date to fit the API
     * @param currentDate Unformatted date
     * @return Formatted Date as String
     */
    protected String getDateToStartCrawlingInISO8601UTC(Date currentDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return df.format(new Date(currentDate.getTime() - Config.PAST_DAYS_TO_CRAWL_IN_MS));
    }

    /**
     * Generates the query for the ExternalRepo request.
     * @return Generated query for the request type.
     */
    public Query generateQuery() {
        return new Query(this.organizationName, this.query, this.requestType, this.estimatedQueryCost);
    }
}
