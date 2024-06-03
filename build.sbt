ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "cqs-akka-backup"
    )

lazy val akkaVersion = "2.6.14"
lazy val leveldbVersion = "0.7"
lazy val leveldbjniVersion = "1.8"
lazy val postgresVersion = "42.5.0"
lazy val cassandraVersion = "1.0.5"

scalacOptions += "-deprecation"
resolvers += Resolver.bintrayRepo("akka", "snapshots")

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % postgresVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra" % cassandraVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % cassandraVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-coordination" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "3.0.3",

  //  local levelDB stores
  "org.iq80.leveldb" % "leveldb" % leveldbVersion,
  "org.fusesource.leveldbjni" % "leveldbjni-all" % leveldbjniVersion
  )

libraryDependencies += "com.typesafe" % "config" % "1.4.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
