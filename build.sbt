val playVersion = "2.9.0"
val commonDependenciesInTestScope = Seq(
  "org.scalatest" %% "scalatest" % "3.2.17" % "test",
  "ch.qos.logback" % "logback-classic" % "1.4.11" % "test"
)

def unusedWarnings(scalaVersion: String) =
  Seq("-Wunused:imports")

lazy val scalaOAuth2ProviderSettings =
  Defaults.coreDefaultSettings ++
    Seq(
      organization := "com.nulab-inc",
      scalaVersion := "3.3.1",
      crossScalaVersions ++= Seq("2.13.12"),
      scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
      scalacOptions ++= unusedWarnings(scalaVersion.value),
      publishTo := {
        val v = version.value
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      publishMavenStyle := true,
      Test / publishArtifact := false,
      pomIncludeRepository := { _ =>
        false
      },
      pomExtra := <url>https://github.com/nulab/play2-oauth2-provider</url>
        <licenses>
          <license>
            <name>MIT License</name>
            <url>http://www.opensource.org/licenses/mit-license.php</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>https://github.com/nulab/play2-oauth2-provider</url>
          <connection>scm:git:git@github.com:nulab/play2-oauth2-provider.git</connection>
        </scm>
        <developers>
          <developer>
            <id>tsuyoshizawa</id>
            <name>Tsuyoshi Yoshizawa</name>
            <url>https://github.com/tsuyoshizawa</url>
          </developer>
        </developers>
    ) ++ Seq(Compile, Test).flatMap(c =>
      c / console / scalacOptions --= unusedWarnings(scalaVersion.value)
    )

lazy val root = (project in file("."))
  .settings(
    scalaOAuth2ProviderSettings,
    name := "play2-oauth2-provider",
    description := "Support scala-oauth2-core library on Play Framework Scala",
    version := "1.6.0",
    libraryDependencies ++= Seq(
      "com.nulab-inc" %% "scala-oauth2-core" % "1.6.0" % "provided",
      "com.typesafe.play" %% "play" % playVersion % "provided",
      "com.typesafe.play" %% "play-test" % playVersion % "test"
    ) ++ commonDependenciesInTestScope
  )
