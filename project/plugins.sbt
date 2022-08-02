addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.3.1")
addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.5.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.0")
addSbtPlugin("com.geirsson" %% "sbt-scalafmt" % "1.5.1")
libraryDependencies ++= Seq(
  "com.github.pathikrit" %% "better-files" % "3.9.1"
)
