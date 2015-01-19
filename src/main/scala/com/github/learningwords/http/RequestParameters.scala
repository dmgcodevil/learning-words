package com.github.learningwords.http


/**
 * Created by dmgcodevil on 1/17/2015.
 */
class RequestParameters {

  private var pathParams: List[NameValuePair] = List()
  private var queryParams: List[NameValuePair] = List()


  def addPathParam(name: String, value: Any): RequestParameters = {
    pathParams = pathParams :+ new NameValuePair(name, value.toString)
    this
  }

  def addQueryParam(name: String, value: Any): RequestParameters = {
    queryParams = queryParams :+ new NameValuePair(name, value.toString)
    this
  }

  def getPathParams = pathParams

  def getQueryParams = queryParams

  @Override
  override def toString = new StringBuilder()
    .append("pathParams=").append('[').append(toString(pathParams)).append(']')
    .append(", queryParams=").append('[').append(toString(queryParams)).append(']')
    .toString()

  private def toString(pairs: List[NameValuePair]): String = {
    if (pairs == null || pairs.isEmpty) {
      return ""
    }
    val stringBuilder = new StringBuilder()
    for (pair <- pairs) {
      stringBuilder.append(pair.toString()).append(",")
    }
    val result = stringBuilder.toString()
    result.substring(0, result.length() - 1)
  }


  class NameValuePair(val name: String, val value: String) {

    @Override
    override def toString =
      new StringBuilder()
        .append("name='").append(name).append('\'')
        .append(", value='").append(value).append('\'').toString()

  }

  class EmptyRequestParameters extends RequestParameters {

    private val MESSAGE = "this is EmptyRequestParameters implementation and it's prohibited be modified. Create new instance of RequestParameters"

    @throws[UnsupportedOperationException]
    override def addPathParam(name: String, value: Any): RequestParameters = throw new UnsupportedOperationException(MESSAGE)

    @throws[UnsupportedOperationException]
    override def addQueryParam(name: String, value: Any): RequestParameters = throw new UnsupportedOperationException(MESSAGE)
  }

}

object RequestParameters {

  val empty = new RequestParameters()

  def create() = new RequestParameters()
}