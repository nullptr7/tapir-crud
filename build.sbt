ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.13.8"

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

val scalafixCommonSettings = inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest))

lazy val commonSettings: Seq[Setting[_]] = Seq(
  scalacOptions -= "-Xfatal-warnings",
  // scalafixCommonSettings,
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.11",
    "com.github.liancheng" %% "organize-imports" % "0.6.0"
  )
)

lazy val database =
  project
    .settings(
      commonSettings,
      // scalafixCommonSettings,
      libraryDependencies ++= Seq(
        "org.tpolecat" %% "skunk-core" % "0.3.1",
        "org.tpolecat" %% "skunk-circe" % "0.3.1"
      )
    )
    .dependsOn(models)

lazy val models =
  project
    .settings(
      commonSettings,
      // scalafixCommonSettings,
      libraryDependencies ++= Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.0.0"
      )
    )

lazy val includeTestandIt = "it,test"

lazy val server =
  project
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(
      commonSettings,
      scalafixCommonSettings,
      libraryDependencies ++= Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.0.0",
        "org.http4s" %% "http4s-blaze-server" % "0.23.12",
        "io.circe" %% "circe-generic-extras" % "0.14.1",
        "com.github.pureconfig" %% "pureconfig" % "0.17.1",
        "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.1",
        "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % "1.0.0" % includeTestandIt,
        "org.scalatest" %% "scalatest" % "3.2.12" % includeTestandIt,
        "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % "3.6.2" % includeTestandIt,
        "com.softwaremill.sttp.client3" %% "circe" % "3.6.2" % includeTestandIt,
        "org.mockito" %% "mockito-scala" % "1.17.7" % Test,
        "org.tpolecat" %% "skunk-core" % "0.3.1" % IntegrationTest,
        "org.typelevel" %% "cats-effect-testing-specs2" % "1.4.0" % IntegrationTest
        // "org.http4s" %% "http4s-testing" % "0.21.33" % Test
        // "com.softwaremill.sttp.tapir" %% "tapir-server-tests" % "1.0.0",
        // "com.softwaremill.sttp.tapir" %% "tapir-testing" % "1.0.0",
        // "com.softwaremill.sttp.tapir" %% "tapir-tests" % "1.0.0"
      )
    )
    .dependsOn(models, database)

lazy val app =
  project
    .in(file("."))
    .settings(publish := {}, publish / skip := true)
    .aggregate(models, server, database)

lazy val deleteBloop = taskKey[Unit]("Delete Existing Bloop Directory")
deleteBloop := {
  import better.files._
  val temp = (baseDirectory.value / ".bloop").toScala
  if (temp.exists) temp.delete()
}

// .aggregate(server, client, shared, e2e)
