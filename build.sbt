import _root_.io.github.nafg.mergify.dsl._

libraryDependencies ++= List(
  "org.slf4j" % "slf4j-nop" % "2.0.12",
  "com.h2database" % "h2" % "2.2.224",
  "org.scalatest" %% "scalatest" % "3.2.18" % Test
)

scalacOptions += "-deprecation"
run / fork := true
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.5.0"

mergifyExtraConditions := Seq(
  (Attr.Author :== "scala-steward") ||
    (Attr.Author :== "slick-scala-steward[bot]") ||
    (Attr.Author :== "renovate[bot]")
)

// based on https://stackoverflow.com/a/63780833/333643
lazy val runAll = taskKey[Unit]("Run all main classes")

def runAllIn(config: Configuration) = Def.task {
    val s = streams.value
    val cp = (config / fullClasspath).value
    val r = (config / run / runner).value
    val classes = (config / discoveredMainClasses).value
    classes.foreach { className =>
      r.run(className, cp.files, Seq(), s.log).get
    }
}

runAll := {
  runAllIn(Compile).value
  runAllIn(Test).value
}

ThisBuild / githubWorkflowBuild += WorkflowStep.Sbt(List("runAll"), name = Some(s"Run all main classes"))

ThisBuild / githubWorkflowPublishTargetBranches := Seq()
