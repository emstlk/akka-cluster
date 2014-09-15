
val akkaVersion = "2.3.4"

val project = Project(
  id = "akka-cluster",
  base = file("."),
  settings = Project.defaultSettings ++ Seq(
    name := "akka-cluster",
    version := "0.1",
    scalaVersion := "2.10.4",
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
    javacOptions in Compile ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint:unchecked", "-Xlint:deprecation"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
      "com.twitter" %% "finagle-http" % "6.18.0",
      "com.escalatesoft.subcut" %% "subcut" % "2.1"),
    javaOptions in run ++= Seq("-Xms128m", "-Xmx1024m"),
    mainClass in(Compile, run) := Some("com.github.emstlk.ClusterApp")
  )
)
