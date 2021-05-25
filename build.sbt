
lazy val root = (project in file("."))
  .settings(
    name := "TestContainers",
    version := "0.1",
    scalaVersion := "2.13.3",
    unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/main/resources" },
    
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.12",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.12",
      "com.typesafe.akka" %% "akka-stream" % "2.6.6",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.6",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.github.pureconfig" %% "pureconfig" % "0.12.2",
      "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.39.1",
      "com.dimafeng" %% "testcontainers-scala-mysql" % "0.39.1",
      "com.github.nscala-time" %% "nscala-time" % "2.28.0",
      "org.scalatest" %% "scalatest" % "3.1.1" % Test,
      "org.scalamock" %% "scalamock" % "4.4.0" % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.6.6" % Test
    )
  )
