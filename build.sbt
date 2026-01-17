lazy val akkaVersion = "2.8.8"

val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.13.18",
  scalacOptions ++=
    Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-language:_",
      "-target:jvm-1.8"
    ),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.5.25",
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.19" % Test
  ),
  scalafmtOnCompile in ThisBuild := true
)

val `akka-actor-3` = (project in file("akka-actor-3")).settings(commonSettings)

val `akka-actor-2` = (project in file("akka-actor-2")).settings(commonSettings)

val `akka-actor-0` =
  (project in file("akka-actor-0")).settings(commonSettings)

val `akka-actor-1` =
  (project in file("akka-actor-1")).settings(commonSettings)

val `scala-concurrent` =
  (project in file("scala-concurrent")).settings(commonSettings)

val root = (project in file("."))
  .aggregate(`akka-actor-1`, `akka-actor-2`, `akka-actor-3`)
