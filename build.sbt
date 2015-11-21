name := "fission"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "hseeberger at bintray" at "http://dl.bintray.com/hseeberger/maven"

libraryDependencies += "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.0-M1"

libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.4.0"

libraryDependencies += "de.heikoseeberger" %% "akka-http-json4s" % "1.2.1"

libraryDependencies += "org.json4s" % "json4s-native_2.11" % "3.3.0"

libraryDependencies += "org.scaldi" %% "scaldi-akka" % "0.5.6"

// TODO Remove the below

libraryDependencies += "org.iq80.leveldb" % "leveldb" % "0.7"

libraryDependencies += "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
