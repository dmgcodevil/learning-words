package com.github.learningwords.http;

import android.net.http.AndroidHttpClient;
import android.os.StrictMode;

import com.github.learningwords.exception.HttpClientException;
import com.github.learningwords.http.response.BasicSuccessStatusHandler;
import com.github.learningwords.http.response.HttpResponseHandler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Created by dmgcodevil on 1/17/2015.
 */
public class HttpTemplate {

    private HttpClient httpClient = new DefaultHttpClient();


    //private JsonMapper jsonMapper = new JacksonJsonMapper();

    private HttpResponseHandler basicSuccessStatusHandler = new BasicSuccessStatusHandler();


    public HttpTemplate(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpTemplate() {

        httpClient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
        httpClient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
    }

    /**
     * Sends GET request to specified url.
     *
     * @param url the url to make GET request
     * @return response {@link org.apache.http.HttpResponse}
     * @throws HttpClientException
     */
    public HttpResponse get(String url) throws HttpClientException {
        return get(url, RequestParameters.empty());
    }

    /**
     * Executes a request and return response as stream.
     *
     * @param url request url
     * @return response as stream
     * @throws HttpClientException
     */
    public HttpResponse get(String url, RequestParameters requestParameters) throws HttpClientException {
        HttpGet httpGet;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            url = setPathParams(url, requestParameters);
            httpGet = new HttpGet(url);
            URIBuilder uriBuilder = addQueryParams(new URIBuilder(httpGet.getURI()), requestParameters);
            httpGet.setURI(uriBuilder.build());
            HttpResponse response = httpClient.execute(httpGet);
            return handleResponse(response);
        } catch (IOException | URISyntaxException e) {
            throw new HttpClientException("failed build url: " + url + ", parameters: " + requestParameters, e);
        }
        //finally ->  httpGet.abort(); this operation closes socket as well, don't invoke it before read data from socket
    }

    /**
     * Sends GET request to specified url.
     *
     * @param url        the url to make GET request
     * @param parameters the request parameters
     * @return input stream that contains response data
     * @throws HttpClientException
     */
    public InputStream getForStream(String url, RequestParameters parameters) throws HttpClientException {
        HttpResponse response = get(url, parameters);
        try {
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new HttpClientException(e);
        }
    }


    /**
     * Sends GET request to specified url.
     *
     * @param url the url to make GET request
     * @return input stream that contains response data
     * @throws HttpClientException
     */
    public InputStream getForStream(String url) throws HttpClientException {
        return getForStream(url, RequestParameters.empty());
    }


    private HttpResponse handleResponse(HttpResponse response) {
        basicSuccessStatusHandler.handle(response);
        return response;
    }

    /**
     * Replaces a placeholders in the given urlTemplate.
     *
     * @param urlTemplate the url template
     * @param parameters  the request parameters
     * @return processed url
     */
    private String setPathParams(String urlTemplate, RequestParameters parameters) {
        for (RequestParameters.NameValuePair nameValuePair : parameters.getPathParams()) {
            String pathParam = "{" + nameValuePair.getName() + "}";
            urlTemplate = urlTemplate.replace(pathParam, nameValuePair.getValue());
        }
        return urlTemplate;
    }

    private URIBuilder addQueryParams(URIBuilder uriBuilder, RequestParameters parameters) {
        for (RequestParameters.NameValuePair nameValuePair : parameters.getQueryParams()) {
            uriBuilder.addParameter(nameValuePair.getName(), nameValuePair.getValue());
        }
        return uriBuilder;
    }
}
