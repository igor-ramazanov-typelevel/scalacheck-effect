import org.typelevel.sbt.gha.WorkflowStep.Run
import org.typelevel.sbt.gha.WorkflowStep.Sbt

ThisBuild / tlBaseVersion := "2.1"

ThisBuild / developers += tlGitHubDev("mpilquist", "Michael Pilquist")
ThisBuild / startYear := Some(2021)

ThisBuild / crossScalaVersions := List("3.3.5", "2.13.16")
ThisBuild / tlVersionIntroduced := Map("3" -> "1.0.2")

ThisBuild / githubOwner := "igor-ramazanov-typelevel"
ThisBuild / githubRepository := "scalacheck-effect"

ThisBuild / githubWorkflowPublishPreamble := List.empty
ThisBuild / githubWorkflowUseSbtThinClient := true
ThisBuild / githubWorkflowPublish := List(
  Run(
    commands = List("echo \"$PGP_SECRET\" | gpg --import"),
    id = None,
    name = Some("Import PGP key"),
    env = Map("PGP_SECRET" -> "${{ secrets.PGP_SECRET }}"),
    params = Map(),
    timeoutMinutes = None,
    workingDirectory = None
  ),
  Sbt(
    commands = List("+ publish"),
    id = None,
    name = Some("Publish"),
    cond = None,
    env = Map("GITHUB_TOKEN" -> "${{ secrets.GB_TOKEN }}"),
    params = Map.empty,
    timeoutMinutes = None,
    preamble = true
  )
)
ThisBuild / gpgWarnOnFailure := false

lazy val root = tlCrossRootProject.aggregate(core, munit)

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .settings(
    name := "scalacheck-effect",
    tlFatalWarnings := false,
    publishTo := githubPublishTo.value,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
  )
  .settings(
    libraryDependencies ++= List(
      "org.scalacheck" %%% "scalacheck" % "1.18.1",
      "org.typelevel" %%% "cats-core" % "2.13.0"
    ).map(module => module.excludeAll("org.scala-native", "test-interface"))
  )

lazy val munit = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .settings(
    name := "scalacheck-effect-munit",
    testFrameworks += new TestFramework("munit.Framework"),
    publishTo := githubPublishTo.value,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
  )
  .dependsOn(core)
  .settings(
    libraryDependencies ++= List(
      "org.scalameta" %%% "munit-scalacheck" % "1.1.0",
      "org.typelevel" %%% "cats-effect" % "3.7-4972921" % Test
    ).map(module => module.excludeAll("org.scala-native", "test-interface"))
  )
  .nativeSettings(
    libraryDependencies ++= List(
      "org.scala-native" %%% "test-interface" % "0.5.7"
    )
  )
