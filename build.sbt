val playVersion = "2.5.10"
val commonDependenciesInTestScope = Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.8" % "test"
)

val unusedWarnings = Seq(
  "-Ywarn-unused",
  "-Ywarn-unused-import"
)

lazy val scalaOAuth2ProviderSettings =
  Defaults.coreDefaultSettings ++
    scalariformSettings ++
    Seq(
      organization := "com.nulab-inc",
      scalaVersion := "2.11.8",
      crossScalaVersions := Seq("2.11.8"),
      scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
      scalacOptions ++= unusedWarnings,
      publishTo := {
        val v = version.value
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
        else Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
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
      scalacOptions in (c, console) --= unusedWarnings
    )

lazy val root = Project(
  id = "play2-oauth2-provider",
  base = file("."),
  settings = scalaOAuth2ProviderSettings ++ Seq(
    name := "play2-oauth2-provider",
    description := "Support scala-oauth2-core library on Playframework Scala",
    version := "1.2.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.nulab-inc" % "scala-oauth2-core_2.11" % "1.2.0",
      "com.typesafe.play" %% "play" % playVersion % "provided",
      "com.typesafe.play" %% "play-test" % playVersion % "test"
    ) ++ commonDependenciesInTestScope
  )
)
