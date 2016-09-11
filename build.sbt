val playVersion = "2.5.6"
val akkaVersion = "2.4.7"
val commonDependenciesInTestScope = Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.7" % "test"
)

lazy val scalaOAuth2ProviderSettings =
  Defaults.coreDefaultSettings ++
    scalariformSettings ++
    Seq(
      organization := "com.nulab-inc",
      scalaVersion := "2.11.8",
      crossScalaVersions := Seq("2.11.8"),
      scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
      publishTo <<= version { (v: String) =>
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
  id = "scala-oauth2-provider",
  base = file("."),
  settings = scalaOAuth2ProviderSettings ++ Seq(
    name := "scala-oauth2-provider",
    description := "OAuth 2.0 server-side implementation written in Scala"
  )
).aggregate(scalaOAuth2Core, play2OAuth2Provider, akkahttpOAuth2Provider)

lazy val scalaOAuth2Core = Project(
  id = "scala-oauth2-core",
  base = file("scala-oauth2-core"),
  settings = scalaOAuth2ProviderSettings ++ Seq(
    name := "scala-oauth2-core",
    description := "OAuth 2.0 server-side implementation written in Scala",
    version := "1.0.1-SNAPSHOT",
    libraryDependencies ++= commonDependenciesInTestScope
  )
)

lazy val play2OAuth2Provider = Project(
  id = "play2-oauth2-provider",
  base = file("play2-oauth2-provider"),
  settings = scalaOAuth2ProviderSettings ++ Seq(
    name := "play2-oauth2-provider",
    description := "Support scala-oauth2-core library on Playframework Scala",
    version := "1.0.1-SNAPSHOT",
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/maven-releases/",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % playVersion % "provided",
      "com.typesafe.play" %% "play-test" % playVersion % "test"
    ) ++ commonDependenciesInTestScope
  )
) dependsOn (scalaOAuth2Core % "compile->compile;test->test")

lazy val akkahttpOAuth2Provider = Project(
  id = "akka-http-oauth2-provider",
  base = file("akka-http-oauth2-provider"),
  settings = scalaOAuth2ProviderSettings ++ Seq(
    name := "akka-http-oauth2-provider",
    description := "Support scala-oauth2-core library on akka-http",
    version := "1.0.1-SNAPSHOT",
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/maven-releases/",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion
    ) ++ commonDependenciesInTestScope
  )
) dependsOn (scalaOAuth2Core % "compile->compile;test->test")
