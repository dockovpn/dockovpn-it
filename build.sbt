
lazy val root = (project in file("."))
  .settings(
    name := "dockovpn-it",
    version := "0.1",
    scalaVersion := "2.13.3",
    unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/main/resources" },
    fork := true,
    
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "org.testcontainers" % "testcontainers" % "1.17.2",
      "com.github.nscala-time" %% "nscala-time" % "2.30.0",
      "org.scalatest" %% "scalatest" % "3.2.12" % Test,
      "org.scalamock" %% "scalamock" % "5.2.0" % Test,
    )
  )
