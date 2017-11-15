val commonDependenciesInTestScope = Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"
)

val unusedWarnings = Seq(
  "-Ywarn-unused",
  "-Ywarn-unused-import"
)

lazy val scalaOAuth2ProviderSettings =
  Defaults.coreDefaultSettings ++
    Seq(
      organization := "com.nulab-inc",
      scalaVersion := "2.12.4",
      crossScalaVersions := Seq("2.12.4", "2.11.11"),
      scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
      scalacOptions ++= unusedWarnings,
      publishTo := {
        val v = version.value
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
        else Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      scalacOptions in (Compile, doc) ++= {
        val v = if(isSnapshot.value) {
          sys.process.Process("git rev-parse HEAD").lineStream_!.head
        } else {
          version.value
        }
        Seq(
          "-sourcepath",
          (baseDirectory in LocalRootProject).value.getAbsolutePath,
          "-doc-source-url",
          s"https://github.com/nulab/scala-oauth2-provider/${v}€{FILE_PATH}.scala"
        )
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
    ) ++ Seq(Compile, Test).flatMap(c =>
      scalacOptions in (c, console) --= unusedWarnings
    )

lazy val root = (project in file("."))
  .settings(
    scalaOAuth2ProviderSettings,
    name := "scala-oauth2-core",
    description := "OAuth 2.0 server-side implementation written in Scala",
    version := "1.3.1-SNAPSHOT",
    libraryDependencies ++= commonDependenciesInTestScope
  )
