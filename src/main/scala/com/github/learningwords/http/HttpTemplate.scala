package com.github.learningwords.http

import java.io.{IOException, InputStream}
import java.net.URISyntaxException

import android.os.StrictMode
import com.github.learningwords.exception.HttpClientException
import com.github.learningwords.http.response.BasicSuccessStatusHandler
import com.github.learningwords.mapping.jackson.JacksonJsonMapper
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.{HttpResponse, HttpVersion}

/**
 * Created by dmgcodevil on 1/17/2015.
 */
class HttpTemplate {

  private var httpClient: HttpClient = new DefaultHttpClient()


  //private JsonMapper jsonMapper = new JacksonJsonMapper();

  private val basicSuccessStatusHandler = new BasicSuccessStatusHandler()
  private val jsonMapper = new JacksonJsonMapper()

  def this(httpClient: HttpClient) {
    this()
    this.httpClient = httpClient
    httpClient.getParams.setParameter("http.protocol.version", HttpVersion.HTTP_1_1)
    httpClient.getParams.setParameter("http.protocol.content-charset", "UTF-8")
  }

  /**
   * Sends GET request to specified url.
   *
   * @param url the url to make GET request
   * @return response { @link org.apache.http.HttpResponse}
   * @throws HttpClientException
   */
  @throws[HttpClientException]
  def get(url: String): HttpResponse = {
    return get(url, RequestParameters.empty)
  }

  /**
   * Executes a request and return response as stream.
   *
   * @param url request url
   * @return response as stream
   * @throws HttpClientException
   */
  @throws[HttpClientException]
  def get(url: String, requestParameters: RequestParameters): HttpResponse = {
    def throwException = (e: Throwable) => throw HttpClientException.create("failed build url: " + url + ", parameters: " + requestParameters, e)
    var requestUrl = url
    var httpGet: HttpGet = null
    try {
      val policy = new StrictMode.ThreadPolicy.Builder().permitAll().build()
      StrictMode.setThreadPolicy(policy)
      requestUrl = setPathParams(requestUrl, requestParameters)
      httpGet = new HttpGet(requestUrl)
      val uriBuilder = addQueryParams(new URIBuilder(httpGet.getURI), requestParameters)
      httpGet.setURI(uriBuilder.build())
      val response = httpClient.execute(httpGet)
      handleResponse(response);
    } catch {
      case io: IOException => throwException(io)
      case ue: URISyntaxException => throwException(ue)
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
  @throws[HttpClientException]
  def getForStream(url: String, parameters: RequestParameters): InputStream = {
    val response = get(url, parameters)
    try {
      response.getEntity.getContent
    } catch {
      case io: Throwable => throw HttpClientException.create(io)
    }
  }

  def getForObject[T](url: String, parameters: RequestParameters, responseType: Class[T]): T = {
    val response = getForStream(url, parameters)
    convert(response, responseType)
  }

  /**
   * Sends GET request to specified url.
   *
   * @param url the url to make GET request
   * @return input stream that contains response data
   * @throws HttpClientException
   */
  @throws[HttpClientException]
  def getForStream(url: String): InputStream = {
    return getForStream(url, RequestParameters.empty)
  }


  private def handleResponse(response: HttpResponse): HttpResponse = {
    basicSuccessStatusHandler.handle(response)
    response
  }

  /**
   * Replaces a placeholders in the given urlTemplate.
   *
   * @param urlTemplate the url template
   * @param parameters  the request parameters
   * @return processed url
   */
  private def setPathParams(urlTemplate: String, parameters: RequestParameters): String = {
    var url = urlTemplate
    for (nameValuePair <- parameters.getPathParams) {
      var pathParam = "{" + nameValuePair.name + "}"
      url = url.replace(pathParam, nameValuePair.value)
    }
    return url
  }

  private def addQueryParams(uriBuilder: URIBuilder, parameters: RequestParameters): URIBuilder = {
    for (nameValuePair <- parameters.getQueryParams) {
      uriBuilder.addParameter(nameValuePair.name, nameValuePair.value)
    }
    uriBuilder
  }

  private def convert[T](inputStream: InputStream, responseType: Class[T]): T = {
    jsonMapper.readObject(inputStream, responseType);
  }
}
