lazy val commonSettings = Seq(
  organization := "io.throughaglass",
  version := "1.0",
  scalaVersion := "2.11.8"
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,

    name := "semantix-spark-challenge",

    sparkVersion := "2.3.1",
    sparkComponents := Seq("sql"),

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
      "com.holdenkarau" %% "spark-testing-base" % "2.3.0_0.9.0" % "test"
    ),

    // append several options to the list of options passed to the Java compiler
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    // append -deprecation to the options passed to the Scala compiler
    scalacOptions ++= Seq("-deprecation", "-unchecked"),

    // fork a new JVM for 'run' and 'test:run'
    fork := true,
    // fork a new JVM for 'test:run', but not 'run'
    fork in Test := true,
    // add a JVM option to use when forking a JVM for 'run'
    javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled"),

    // only use a single thread for building
    parallelExecution := false,
    // Execute tests in the current project serially
    //   Tests from other projects may still run concurrently.
    parallelExecution in Test := false,

    // uses compile classpath for the run task, including "provided" jar (cf http://stackoverflow.com/a/21803413/3827)
    run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated
  )
