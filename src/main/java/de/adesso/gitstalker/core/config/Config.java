package de.adesso.gitstalker.core.config;

public class Config {

    /**
     * Github API Token is used to authorize the request. Without token the request will be signed as unauthorized and won't receive a response.
     * Github API Url is used to specify the interface where the request needs to be posted.
     */

    // API Configuration
    public static final String API_TOKEN = "0b20775d787a21574ac24b19fcf92de7a75b1854";
    public static final String API_URL = "https://api.github.com/graphql";

    // Range of days for evaluation
    public static final int PAST_DAYS_AMOUNT_TO_CRAWL = 5;
    public static final long DAY_IN_MS = 1000 * 60 * 60 * 24;
    public static final long PAST_DAYS_TO_CRAWL_IN_MS = PAST_DAYS_AMOUNT_TO_CRAWL * DAY_IN_MS;

    //Update of organization information
    public static final int UPDATE_RATE_DAYS = 1;
    public static final long UPDATE_RATE_DAYS_IN_MS = UPDATE_RATE_DAYS * DAY_IN_MS;
    public static final int LIMIT_BEFORE_LAST_ACCESS_DATE = 7;
    public static final long LIMIT_BEFORE_LAST_ACCESS_DATE_IN_MS = LIMIT_BEFORE_LAST_ACCESS_DATE * DAY_IN_MS;

    //Processing Speed
    public static final int PROCESSING_RATE_IN_MS = 100;
    public static final int PROCESSING_DELAY_IN_MS = 5000;
}
