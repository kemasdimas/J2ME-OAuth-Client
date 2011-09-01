package com.tccoder.j2me.client;


import java.io.IOException;
import java.util.Hashtable;
import net.oauth.j2me.BadTokenStateException;
import net.oauth.j2me.Consumer;
import net.oauth.j2me.OAuthMessage;
import net.oauth.j2me.OAuthServiceProviderException;
import net.oauth.j2me.signature.HMACSHA1Signature;
import net.oauth.j2me.token.AccessToken;
import net.oauth.j2me.token.RequestToken;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Dimas
 */
public class TwitterClient {
    private static final String CONSUMER_TOKEN ="YOUR_TOKEN";
    private static final String CONSUMER_SECRET ="YOUR_TOKEN_SECRET";

    private static final String REQUEST_URL = "https://api.twitter.com/oauth/request_token";
    private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
    private static final String ACCESS_URL = "https://api.twitter.com/oauth/access_token";
    public static final String CALLBACK_URL = "YOUR_CALLBACK_URL";

    private static final String UPDATE_STATUS_URL = "https://api.twitter.com/1/statuses/update.json";

    private HMACSHA1Signature signer = new HMACSHA1Signature();
    private Consumer consumer;
    private RequestToken request;
    private AccessToken access;
    private String verifier = null;

    private static TwitterClient instance;

    /**
     * Singleton accessor
     *
     * @return TwitterClient singleton instance
     */
    public static TwitterClient getInstance() {
        if (instance == null) {
            instance = new TwitterClient();
        }

        return instance;
    }

    private TwitterClient() {
        consumer = new Consumer(CONSUMER_TOKEN, CONSUMER_SECRET);
        consumer.setSignatureMethod(signer.getMethod());
    }

    /**
     * Fetch new request token from twitter server
     *
     * @return
     * @throws OAuthServiceProviderException
     */
    public RequestToken fetchNewRequestToken() throws OAuthServiceProviderException {
        request = consumer.getRequestToken(REQUEST_URL, CALLBACK_URL);

        return request;
    }

    /**
     * Fetch new access token from twitter server, make sure to call parseAuthorizeResult()
     * before calling this function
     *
     * @return
     * @throws BadTokenStateException
     * @throws OAuthServiceProviderException
     */
    public AccessToken fetchNewAccessToken() throws BadTokenStateException, OAuthServiceProviderException {
        if (verifier == null || verifier.length() == 0) {
            throw new BadTokenStateException("No verifier set");
        }

        return fetchNewAccessToken(verifier);
    }

    /**
     * Fetch new access token from twitter server
     *
     * @param verifier
     * @return
     * @throws BadTokenStateException
     * @throws OAuthServiceProviderException
     */
    private AccessToken fetchNewAccessToken(String verifier) throws BadTokenStateException, OAuthServiceProviderException {
        if (request == null) {
            throw new BadTokenStateException("No request token initiated");
        }

        access = consumer.getAccessToken(ACCESS_URL, request, verifier);
        request = null;
        this.verifier = null;

        return access;
    }

    /**
     * Parse authorization page callback to get access token verifier, recommended
     * to call fetchNewAccessToken() after this function
     *
     * @param authorizeResult
     */
    public void parseAuthorizeResult(String authorizeResult) {
        final String verString = "oauth_verifier";
        int verifierIndex = authorizeResult.indexOf(verString);

        if (verifierIndex != -1) {
            verifier = authorizeResult.substring(verifierIndex + verString.length() + 1);
        }
    }

    /**
     * Get authorization URL to enter username & password
     *
     * @return String authorization URL
     * @throws BadTokenStateException
     */
    public String getAuthorizeUrl() throws BadTokenStateException {
        if (request == null) {
            throw new BadTokenStateException("No request token initiated");
        }

        return AUTHORIZE_URL.concat("?oauth_token=").concat(request.getToken());
    }

    /**
     * Access token accessor
     *
     * @return AccessToken
     */
    public AccessToken getAccessToken() {
        return access;
    }

    /**
     * Update authenticated user timeline with provided status message
     *
     * @param status Status to be updated on timeline
     * @return String response from Twitter server
     * @throws BadTokenStateException
     * @throws OAuthServiceProviderException
     * @throws IOException
     */
    public String updateStatus(String status) throws BadTokenStateException, OAuthServiceProviderException, IOException {
        if (access == null) {
            throw new BadTokenStateException("No access token set");
        }

        Hashtable queryParams = new Hashtable();
        queryParams.put("status", status);
        queryParams.put("trim_user", "0");
        queryParams.put("include_entities", "0");
        
        return consumer.accessProtectedResource(UPDATE_STATUS_URL, access, queryParams, "POST");
    }
}
