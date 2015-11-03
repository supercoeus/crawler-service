import sbt.Keys._
import sbt._

object BuildSettings {

  lazy val basicSettings = Seq(
    version := "0.0.1",
    homepage := Some(new URL("https://github.com/yangbajing/crawler-service")),
    organization := "me.yangbajing",
    organizationHomepage := Some(new URL("https://github.com/yangbajing/crawler-service")),
    startYear := Some(2015),
    scalaVersion := "2.11.7",
    scalacOptions := Seq(
      "-encoding", "utf8",
      "-unchecked",
      "-feature",
      "-deprecation"
    ),
    javacOptions := Seq(
      "-encoding", "utf8",
      "-Xlint:unchecked",
      "-Xlint:deprecation"
    ),
    resolvers ++= Seq(
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "releases" at "http://oss.sonatype.org/content/repositories/releases",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"),
    libraryDependencies ++= Seq(
      _scalatest
    ),
    offline := true
  )

  lazy val noPublishing = Seq(
    publish :=(),
    publishLocal :=()
  )

  val verAkka = "2.3.14"
  val _akkaActor = "com.typesafe.akka" %% "akka-actor" % verAkka

  val verAkkaHttp = "1.0"
  val _akkaHttpCore = ("com.typesafe.akka" %% "akka-http-core-experimental" % verAkkaHttp).exclude("com.typesafe.akka", "akka-actor")
  val _akkaHttp = ("com.typesafe.akka" %% "akka-http-experimental" % verAkkaHttp).exclude("com.typesafe.akka", "akka-actor")

  val _scalatest = "org.scalatest" %% "scalatest" % "2.2.5" % "test"

  val _scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

  val _akkaHttpJson4s = ("de.heikoseeberger" %% "akka-http-json4s" % "1.1.0").exclude("org.json4s", "json4s-jackson")

  val _json4sJackson = "org.json4s" %% "json4s-jackson" % "3.3.0"

  //val _hanlp = "com.hankcs" % "hanlp" % "portable-1.2.4"

  val _logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.3"

}
