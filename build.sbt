
lazy val commonSettings = Seq(
  name := "sag-taxi" ,
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.12.7",
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",   // source files are in UTF-8
    "-deprecation",         // warn about use of deprecated APIs
    "-unchecked",           // warn about unchecked type parameters
    "-feature",             // warn about misused language features
    "-language:higherKinds",// allow higher kinded types without `import scala.language.higherKinds`
    "-Xlint",               // enable handy linter warnings
    "-Xfatal-warnings",     // turn compiler warnings into errors
    "-Ypartial-unification" // allow the compiler to unify type constructors of different arities
  )
)

lazy val resolverSettings = Seq(
  resolvers ++= Seq(
    Resolver.jcenterRepo,
    "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
  )
)

val akkaVersion       = "2.6.6"
val catsVersion       = "1.4.0"
val scalaGraphVersion = "1.13.1"
val scalafxVersion    = "12.0.2-R18"

lazy val root = (project in file ("."))
  .settings(commonSettings)
  .settings(resolverSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit"   % akkaVersion,
      "org.typelevel"     %% "cats-core"      % catsVersion,
      "org.scala-graph"   %% "graph-core"     % scalaGraphVersion,
      "org.scalafx"       %% "scalafx"        % scalafxVersion
    )
  )
