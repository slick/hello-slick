import _root_.io.github.nafg.mergify.dsl.*

ThisBuild / scalaVersion := "2.12.20"

libraryDependencies ++= List(
  "org.slf4j" % "slf4j-nop" % "2.0.19",
  "com.h2database" % "h2" % "2.4.240",
  "org.scalatest" %% "scalatest" % "3.2.20" % Test
)

scalacOptions += "-deprecation"
run / fork := true
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.6.1"

mergifyExtraConditions := Seq(
  (Attr.Author :== "scala-steward") ||
    (Attr.Author :== "slick-scala-steward[bot]") ||
    (Attr.Author :== "renovate[bot]")
)

// based on https://stackoverflow.com/a/63780833/333643
@transient
lazy val runAll = taskKey[Unit]("Run all main classes")

def runAllIn(config: Configuration) = Def.task {
  val s = streams.value
  val cp = (config / fullClasspath).value
  val r = (config / run / runner).value
  val classes = (config / discoveredMainClasses).value
  given FileConverter = fileConverter.value
  classes.foreach { className =>
    r.run(className, cp.files, Seq(), s.log).get
  }
}

runAll := {
  runAllIn(Compile).value
  runAllIn(Test).value
}

ThisBuild / githubWorkflowPublishTargetBranches := Seq()
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.zulu("17"))
ThisBuild / githubWorkflowBuild += WorkflowStep.Sbt(
  List("runAll"),
  name = Some(s"Run all main classes")
)
