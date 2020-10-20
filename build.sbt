lazy val akkaVersion = "2.6.10"

val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.13.1",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.1.0" % Test
  )
)

val `akka-actor-2` =
  (project in file("akka-actor-2")).settings(commonSettings)

val `akka-actor-1` =
  (project in file("akka-actor-1")).settings(commonSettings)

val root = (project in file("."))
  .aggregate(`akka-actor-1`, `akka-actor-2`)
