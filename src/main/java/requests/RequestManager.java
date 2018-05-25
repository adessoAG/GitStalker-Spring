package requests;

import enums.RequestType;
import objects.Query;

import java.util.Date;
import java.util.List;

public class RequestManager {

    private String organizationName;
    private String endCursor = null;
    private List<String> memberIDs;

    public RequestManager(String organizationName) {
        this.organizationName = organizationName;
    }

    public RequestManager(String organizationName, String endCursor){
        this.organizationName = organizationName;
        this.endCursor = "\"" + endCursor + "\"";
    }

    public RequestManager(String organizationName, List<String> memberIDs){
        this.organizationName = organizationName;
        this.memberIDs = memberIDs;
    }

    public Query processRequest(RequestType requestType) {
        switch (requestType) {
            case ORGANIZATION_DETAIL:
                return new OrganizationDetailRequest(organizationName).generateQuery();
            case MEMBER_ID:
                return new MemberIDRequest(organizationName,endCursor).generateQuery();
            case MEMBER_PR:
                return new MemberPRRequest(organizationName,endCursor).generateQuery();
            default:
                return null;
        }
    }
}
