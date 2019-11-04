val playVersion = "2.7.3"
val commonDependenciesInTestScope = Seq(
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"
)

def unusedWarnings(scalaVersion: String) =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 11)) =>
      Seq(
        "-Ywarn-unused",
        "-Ywarn-unused-import"
      )
    case _ =>
      Seq("-Ywarn-unused:-imports,_")
  }

lazy val scalaOAuth2ProviderSettings =
  Defaults.coreDefaultSettings ++
    Seq(
      organization := "com.nulab-inc",
      scalaVersion := "2.13.0",
      crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0"),
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
      publishArtifact in Test := false,
      pomIncludeRepository := { x =>
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
    ) ++ Seq(Compile, Test).flatMap(
    c => scalacOptions in (c, console) --= unusedWarnings(scalaVersion.value)
  )

lazy val root = (project in file("."))
  .settings(
    scalaOAuth2ProviderSettings,
    name := "play2-oauth2-provider",
    description := "Support scala-oauth2-core library on Play Framework Scala",
    version := "1.4.3-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.nulab-inc" %% "scala-oauth2-core" % "1.4.0" % "provided",
      "com.typesafe.play" %% "play" % playVersion % "provided",
      "com.typesafe.play" %% "play-test" % playVersion % "test"
    ) ++ commonDependenciesInTestScope
  )
