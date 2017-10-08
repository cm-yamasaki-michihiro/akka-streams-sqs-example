name := "akka-streams-sqs-s3"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-sqs" % "1.11.205",
  "com.typesafe.akka" %% "akka-stream" % "2.5.6",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.6" % Test,
  "com.lightbend.akka" %% "akka-stream-alpakka-sqs" % "0.13",
  "io.circe" %% "circe-core" % "0.8.0",
  "io.circe" %% "circe-generic" % "0.8.0",
  "io.circe" %% "circe-parser" % "0.8.0",
  "org.slf4j" % "slf4j-api" % "1.7.22",
  "org.slf4j" % "slf4j-log4j12" % "1.7.22",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
)
