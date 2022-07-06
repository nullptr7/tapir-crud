ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.13.8"

lazy val commonSettings: Seq[Setting[_]] = Seq(
  scalacOptions -= "-Xfatal-warnings",
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.11"
  )
)

lazy val mockData =
  project
    .settings(
      commonSettings
    )
    .dependsOn(models)

lazy val models =
  project
    .settings(
      commonSettings,
      libraryDependencies ++= Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.0.0"
      )
    )

lazy val server =
  project
    .settings(
      commonSettings,
      libraryDependencies ++= Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.0.0",
        "org.http4s" %% "http4s-blaze-server" % "0.23.12"
      )
    )
    .dependsOn(models, mockData)

lazy val app =
  project
    .in(file("."))
    .settings(publish := {}, publish / skip := true)
    .aggregate(models, server)

// .aggregate(server, client, shared, e2e)
