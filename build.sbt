val commonDependenciesInTestScope = Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.8" % "test"
)

lazy val scalaOAuth2ProviderSettings =
  Defaults.coreDefaultSettings ++
    scalariformSettings ++
    Seq(
      organization := "com.nulab-inc",
      scalaVersion := "2.12.1",
      crossScalaVersions := Seq("2.12.1", "2.11.8"),
      scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
      publishTo := {
        val v = version.value
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
        else Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := <url>https://github.com/nulab/scala-oauth2-provider</url>
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
    )

lazy val root = Project(
  id = "scala-oauth2-core",
  base = file("."),
  settings = scalaOAuth2ProviderSettings ++ Seq(
    name := "scala-oauth2-core",
    description := "OAuth 2.0 server-side implementation written in Scala",
    version := "1.3.1-SNAPSHOT",
    libraryDependencies ++= commonDependenciesInTestScope
  )
)
