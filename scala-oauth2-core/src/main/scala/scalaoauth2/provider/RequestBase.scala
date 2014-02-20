package scalaoauth2.provider

import collection.immutable.TreeMap
import math.Ordering

class RequestBase(headers: TreeMap[String, Seq[String]], params: Map[String, Seq[String]]) {

  def this(headers: Map[String, Seq[String]], params: Map[String, Seq[String]]) = {
    this(new TreeMap[String, Seq[String]]()(Ordering.by(_.toLowerCase)) ++ headers, params)
  }

  def header(name: String): Option[String] = headers.get(name).flatMap { _.headOption }

  def requireHeader(name: String): String = header(name).getOrElse(throw new InvalidRequest("required header: " + name))

  def param(name: String): Option[String] = params.get(name).flatMap(values => values.headOption)

  def requireParam(name: String): String = param(name).getOrElse(throw new InvalidRequest("required parameter: " + name))

}
