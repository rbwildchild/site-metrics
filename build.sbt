name := "site-metrics"

version := "1.0"

description := "GraphQL server with akka-http and sangria"

scalaVersion := "2.12.3"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % "3.13.0",
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "io.spray" %%  "spray-json" % "1.3.3",
  "com.github.nscala-time" %% "nscala-time" % "2.20.0"
)

Revolver.settings

        