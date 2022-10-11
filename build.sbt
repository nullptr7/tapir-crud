ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.13.9"

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

val scalafixCommonSettings =
  inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest))

val Versions =
  new {
    val tapir       = "1.1.2"
    val log4cats    = "2.5.0"
    val circe       = "0.14.2"
    val http4s      = "0.23.12"
    val http4sCirce = "0.23.16"
  }

lazy val commonSettings: Seq[Setting[_]] = Seq(
  scalacOptions -= "-Xfatal-warnings",
  // scalafixCommonSettings,
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.4.3",
    "org.typelevel" %% "log4cats-slf4j" % Versions.log4cats,
    "org.typelevel" %% "log4cats-noop" % Versions.log4cats,
    "com.github.liancheng" %% "organize-imports" % "0.6.0"
  )
)

lazy val configs =
  project
    .settings(
      commonSettings,
      libraryDependencies ++= Seq(
        "com.github.pureconfig" %% "pureconfig" % "0.17.1",
        "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.1"
      )
    )

lazy val database =
  project
    .settings(
      commonSettings,
      // scalafixCommonSettings,
      libraryDependencies ++= Seq(
        "org.tpolecat" %% "skunk-core" % "0.3.2",
        "org.tpolecat" %% "skunk-circe" % "0.3.2"
      )
    )
    .dependsOn(models, configs)

lazy val models =
  project
    .settings(
      commonSettings,
      // scalafixCommonSettings,
      libraryDependencies ++= Seq(
        "io.circe" %% "circe-generic-extras" % Versions.circe,
        "dev.optics" %% "monocle-core" % "3.1.0"
      )
    )

lazy val includeTestandIt = "it,test"

lazy val client =
  project
    .settings(
      commonSettings,
      libraryDependencies ++= Seq(
        "org.http4s" %% "http4s-blaze-client" % Versions.http4s,
        "io.circe" %% "circe-generic-extras" % Versions.circe,
        "com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % Versions.tapir,
        "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % Versions.tapir
      )
    )
    .dependsOn(configs)

lazy val server =
  project
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(
      commonSettings,
      scalafixCommonSettings,
      libraryDependencies ++= Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % Versions.tapir,
        "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % Versions.tapir,
        "org.http4s" %% "http4s-blaze-server" % Versions.http4s,
        "org.http4s" %% "http4s-circe" % Versions.http4sCirce,
        "io.circe" %% "circe-generic-extras" % Versions.circe,
        "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % Versions.tapir % includeTestandIt,
        "org.scalatest" %% "scalatest" % "3.2.14" % includeTestandIt,
        "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % "3.8.2" % includeTestandIt,
        "com.softwaremill.sttp.client3" %% "circe" % "3.8.2" % includeTestandIt,
        "org.mockito" %% "mockito-scala" % "1.17.12" % Test,
        "org.tpolecat" %% "skunk-core" % "0.3.2" % IntegrationTest,
        "org.typelevel" %% "cats-effect-testing-specs2" % "1.4.0" % IntegrationTest
      )
    )
    .dependsOn(models, database, client)

lazy val app =
  project
    .in(file("."))
    .settings(publish := {}, publish / skip := true)
    .aggregate(models, server, database, client)

lazy val deleteBloop = taskKey[Unit]("Delete Existing Bloop Directory")
deleteBloop := {
  import better.files._
  val temp = (baseDirectory.value / ".bloop").toScala
  if (temp.exists) temp.delete()
}

// .aggregate(server, client, shared, e2e)
