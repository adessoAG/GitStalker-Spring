package de.adesso.gitstalker.core.requests;

import de.adesso.gitstalker.core.objects.Query;
import de.adesso.gitstalker.core.enums.RequestType;
import de.adesso.gitstalker.core.config.Config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * This is the request used for requesting the organization members.
 */
public class MemberRequest {

    private final int estimatedQueryCost = 1;
    private String query;
    private String organizationName;
    private RequestType requestType;

    public MemberRequest(String organizationName, String memberID) {
        this.organizationName = organizationName;
        /**
         * GraphQL Request for the member.
         * Requesting for a single member ID the relevant information of the member.
         * Requests the current rate limit of the token at the API.
         */
        this.query = "{\n" +
                "node(id: \"" + memberID + "\") {\n" +
                "... on User {\n" +
                "name\n" +
                "id\n" +
                "login\n" +
                "url\n" +
                "avatarUrl\n" +
                "repositoriesContributedTo(first: 100, contributionTypes: COMMIT, includeUserRepositories: true) {\n" +
                "nodes {\n" +
                "id\n" +
                "defaultBranchRef {\n" +
                "target {\n" +
                "... on Commit {\n" +
                "history(first: 100, since: \"" + this.getDateToStartCrawlingInISO8601UTC(new Date()) + "\"  ,author: {id: \"" + memberID + "\"}) {\n" +
                "nodes {\n" +
                "committedDate\n" +
                "url\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "issues(last: 25) {\n" +
                "nodes {\n" +
                "createdAt\n" +
                "url\n" +
                "}\n" +
                "}\n" +
                "pullRequests(last: 25) {\n" +
                "nodes {\n" +
                "createdAt\n" +
                "url\n" +
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
        this.requestType = RequestType.MEMBER;
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
     * Generates the query for the member request.
     * @return Generated query for the request type.
     */
    public Query generateQuery() {
        return new Query(this.organizationName, this.query, this.requestType, this.estimatedQueryCost);
    }
}
