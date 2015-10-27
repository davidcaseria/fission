name := "fission"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "hseeberger at bintray" at "http://dl.bintray.com/hseeberger/maven"

libraryDependencies += "com.typesafe.akka" % "akka-http-experimental_2.11" % "1.0"

libraryDependencies += "de.heikoseeberger" %% "akka-http-json4s" % "1.1.0"

libraryDependencies += "org.json4s" % "json4s-native_2.11" % "3.3.0"
