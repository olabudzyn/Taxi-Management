organization := "com.teamg.taxi"
version      := "0.0.1-SNAPSHOT"
scalaVersion := "2.12.10"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-encoding", "UTF-8", // source files are in UTF-8
    "-deprecation", // warn about use of deprecated APIs
    "-unchecked", // warn about unchecked type parameters
    "-feature", // warn about misused language features
    "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
    "-Xlint", // enable handy linter warnings
    "-Xfatal-warnings", // turn compiler warnings into errors
    "-Ypartial-unification" // allow the compiler to unify type constructors of different arities
  ),
  target := { baseDirectory.value / "target"}
)

lazy val resolverSettings = Seq(
  resolvers ++= Seq(
    Resolver.jcenterRepo,
    "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
  )
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

// Add dependency on JavaFX libraries, OS dependent
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")

val akkaV = "2.6.6"
val akkaHttpV = "10.1.12"
val akkaHttpCirceV = "1.27.0"
val catsV = "1.4.0"
val circeV = "0.11.1"
val scalaGraphV = "1.13.1"
val scalafxV = "14-R19"
val scalaTestV = "3.0.8"


lazy val core = (project in file("core"))
  .settings(commonSettings)
  .settings(resolverSettings)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "de.heikoseeberger"   %% "akka-http-circe"      % akkaHttpCirceV,
      "com.typesafe.akka"   %% "akka-actor"           % akkaV,
      "com.typesafe.akka"   %% "akka-slf4j"           % akkaV,
      "com.typesafe.akka"   %% "akka-testkit"         % akkaV,
      "com.typesafe.akka"   %% "akka-http"            % akkaHttpV ,
      "com.typesafe.akka"   %% "akka-stream"          % akkaV ,
      "io.circe"            %% "circe-core"           % circeV,
      "io.circe"            %% "circe-parser"         % circeV,
      "io.circe"            %% "circe-generic"        % circeV,
      "io.circe"            %% "circe-generic-extras" % circeV,
      "org.typelevel"       %% "cats-core"            % catsV,
      "org.scala-graph"     %% "graph-core"           % scalaGraphV,

      "org.scalatest"       %% "scalatest"            % "3.0.8"             % Test
    )
  )

lazy val gui = (project in file("gui"))
  .settings(commonSettings)
  .settings(resolverSettings)
  .settings(
    name := "gui",
    libraryDependencies ++= Seq(
      "org.typelevel"       %% "cats-core"      % catsV,
      "org.scalafx"         %% "scalafx"        % scalafxV,
    ) ++ javaFXModules.map( m =>
      "org.openjfx"         % s"javafx-$m"      % "14.0.1" classifier osName
    )
  )
  .dependsOn(core)

lazy val integration_test = (project in file("integration-test"))
  .settings(commonSettings)
  .settings(resolverSettings)
  .settings(
    name := "integration-test",
    libraryDependencies ++= Seq(
      "org.scalatest"       %% "scalatest"        % scalaTestV    % Test

    )
  ).dependsOn(gui, core)

lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .settings(name := "root")
  .aggregate(core, gui, integration_test)
