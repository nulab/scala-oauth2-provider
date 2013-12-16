package scalaoauth2.provider

class RequestBase(headers: Map[String, String], params: Map[String, Seq[String]]) {

  def header(name: String): Option[String] = headers.get(name)

  def requireHeader(name: String): String = headers.get(name).getOrElse(throw new InvalidRequest("required header: " + name))

  def param(name: String): Option[String] = params.get(name).flatMap(values => values.headOption)

  def requireParam(name: String): String = param(name).getOrElse(throw new InvalidRequest("required parameter: " + name))

}
