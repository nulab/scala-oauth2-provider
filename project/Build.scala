import sbt._
import Keys._

object ScalaOAuth2Build extends Build {

  lazy val _organization = "scalaoauth2"
  lazy val _version =  "0.1.0"
  lazy val _playVersion =  "2.1.0"

  lazy val scalaProvider = Project(
    id = "scala-provider",
    base = file("scala-provider"),
    settings = Defaults.defaultSettings ++ Seq(
      organization := _organization,
      version := _version,
      scalaVersion := "2.10.2",
      crossScalaVersions := Seq("2.9.3", "2.10.2"),
      libraryDependencies ++= Seq(
        "commons-codec" % "commons-codec" % "1.8",
        "org.scalatest" %% "scalatest" % "1.9.1" % "test"
      )
    )
  )

  lazy val play2Provider = Project(
    id = "play2-provider",
    base = file("play2-provider"),
    settings = Defaults.defaultSettings ++ Seq(
      organization := _organization,
      name := "play2-provider",
      version := _version,
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies ++= Seq(
        "play" %% "play" % _playVersion % "provided"
      )
    )
  ) dependsOn(scalaProvider)
}

