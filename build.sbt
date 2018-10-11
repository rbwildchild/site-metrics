name := "site-metrics"

version := "0.1"

scalaVersion := "2.12.7"

description := "GraphQL server with akka-http and sangria"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-server" % "2.44.0",
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "io.spray" %%  "spray-json" % "1.3.3",
  "com.github.nscala-time" %% "nscala-time" % "2.20.0"
)

Revolver.settings

        