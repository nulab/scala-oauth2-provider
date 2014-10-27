import sbt._
import Keys._

object ScalaOAuth2Build extends Build {

  lazy val _organization = "com.nulab-inc"
  lazy val _version =  "0.11.0"
  lazy val _playVersion = "2.3.4"

  val _scalaVersion = "2.10.4"
  val _crossScalaVersions = Seq("2.10.4", "2.11.2")

  val commonDependenciesInTestScope = Seq(
    "org.scalatest" %% "scalatest" % "2.2.0" % "test"
  )

  lazy val scalaOAuth2ProviderSettings = Defaults.defaultSettings ++ Seq(
    organization := _organization,
    version := _version,
    scalaVersion := _scalaVersion,
    crossScalaVersions := _crossScalaVersions,
    scalacOptions ++= _scalacOptions,
    publishTo <<= version { (v: String) => _publishTo(v) },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { x => false },
    pomExtra := _pomExtra
  )

  lazy val root = Project(
    id = "scala-oauth2-provider",
    base = file("."),
    settings = scalaOAuth2ProviderSettings ++ Seq(
      name := "scala-oauth2-provider",
      description := "OAuth 2.0 server-side implementation written in Scala"
    )
  ).aggregate(scalaOAuth2Core, play2OAuth2Provider)

  lazy val scalaOAuth2Core = Project(
    id = "scala-oauth2-core",
    base = file("scala-oauth2-core"),
    settings = scalaOAuth2ProviderSettings ++ Seq(
      name := "scala-oauth2-core",
      description := "OAuth 2.0 server-side implementation written in Scala",
      libraryDependencies ++= Seq(
        "commons-codec" % "commons-codec" % "1.8"
      ) ++ commonDependenciesInTestScope
    )
  )

  lazy val play2OAuth2Provider = Project(
    id = "play2-oauth2-provider",
    base = file("play2-oauth2-provider"),
    settings = scalaOAuth2ProviderSettings ++ Seq(
      name := "play2-oauth2-provider",
      description := "Support scala-oauth2-core library on Playframework Scala",
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/maven-releases/",
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play" % _playVersion % "provided"
      ) ++ commonDependenciesInTestScope
    )
  ) dependsOn(scalaOAuth2Core)

  def _publishTo(v: String) = {
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }

  val _scalacOptions = Seq("-deprecation", "-unchecked", "-feature")
  val _pomExtra = <url>https://github.com/nulab/scala-oauth2-provider</url>
      <licenses>
        <license>
          <name>MIT License</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>https://github.com/nulab/scala-oauth2-provider</url>
        <connection>scm:git:git@github.com:nulab/scala-oauth2-provider.git</connection>
      </scm>
      <developers>
        <developer>
          <id>tsuyoshizawa</id>
          <name>Tsuyoshi Yoshizawa</name>
          <url>https://github.com/tsuyoshizawa</url>
        </developer>
      </developers>
}

