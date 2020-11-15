ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala2_13

lazy val `marketplace-crawler` = (project in file("."))
  .settings(
    name := "marketplace-crawler",
    githubOwner := "ivandyachenko",
    githubTokenSource := TokenSource.Or(TokenSource.GitConfig("github.token"), TokenSource.Environment("GITHUB_TOKEN")),
    projectSettings,
    compilerOptions,
    compilerDependencies,
    coreDependencies,
    testDependencies
  )

lazy val projectSettings = Seq(
  organization := "io.ivandyachenko",
  homepage := Some(url("https://github.com/IvanDyachenko/marketplace-crawler")),
  developers := List(
    Developer("IvanDyachenko", "Ivan Dyachenko", "vandyachen@gmail.com", url("https://ivandyachenko.io"))
  ),
  scalaVersion := scala2_13,
  scalafmtOnCompile := true,
  fork in Global := true, // https://github.com/sbt/sbt/issues/2274
  cancelable in Global := true
)

lazy val compilerDependencies =
  libraryDependencies ++= List(
    compilerPlugin("com.olegpy" %% "better-monadic-for" % betterMonadicForVersion),
    compilerPlugin(("org.typelevel" %% "kind-projector" % kindProjectorVersion).cross(CrossVersion.full))
  )

githubOwner := "ivandyachenko"
githubTokenSource := TokenSource.Or(TokenSource.GitConfig("github.token"), TokenSource.Environment("GITHUB_TOKEN"))

resolvers += Resolver.githubPackages("ivandyachenko")

lazy val coreDependencies =
  libraryDependencies ++= List(
    "org.rudogma"        %% "supertagged"              % supertaggedVersion,
    "com.beachape"       %% "enumeratum"               % enumeratumVersion,
    "com.beachape"       %% "enumeratum-circe"         % enumeratumCirceVersion,
    "com.beachape"       %% "enumeratum-cats"          % enumeratumCatsVersion,
    "ru.tinkoff"         %% "tofu-core"                % tofuVersion,
    "ru.tinkoff"         %% "tofu-core-higher-kind"    % tofuVersion,
    "ru.tinkoff"         %% "tofu-env"                 % tofuVersion,
    "ru.tinkoff"         %% "tofu-derivation"          % tofuVersion,
    "ru.tinkoff"         %% "tofu-optics-core"         % tofuVersion,
    "ru.tinkoff"         %% "tofu-optics-macro"        % tofuVersion,
    "ru.tinkoff"         %% "tofu-enums"               % tofuVersion,
    "ru.tinkoff"         %% "tofu-fs2-interop"         % tofuVersion,
    "ru.tinkoff"         %% "tofu-logging"             % tofuVersion,
    "org.manatki"        %% "derevo-cats-tagless"      % derevoVersion,
    "io.circe"           %% "circe-core"               % circeVersion,
    "io.circe"           %% "circe-derivation"         % circeDerivationVersion,
    "org.http4s"         %% "http4s-dsl"               % http4sVersion,
    "org.http4s"         %% "http4s-circe"             % http4sVersion,
    "org.http4s"         %% "http4s-blaze-client"      % http4sVersion,
    "org.asynchttpclient" % "async-http-client"        % asyncHttpClientVersion,
    "org.http4s"         %% "http4s-async-http-client" % http4sVersion,
    "co.fs2"             %% "fs2-core"                 % fs2Version,
    "io.ivandyachenko"   %% "beru4s"                   % beru4sVersion,
    "ch.qos.logback"      % "logback-classic"          % logbackVersion
  )

lazy val testDependencies =
  libraryDependencies ++= List(
    "org.scalactic"     %% "scalactic"          % scalacticVersion               % "test",
    "org.scalatest"     %% "scalatest"          % scalatestVersion               % "test",
    "org.scalatest"     %% "scalatest-flatspec" % scalatestVersion               % "test",
    "org.scalacheck"    %% "scalacheck"         % scalacheckVersion              % "test",
    "org.scalatestplus" %% "scalacheck-1-14"    % scalatestPlusScalacheckVersion % "test"
  )

lazy val compilerOptions =
  scalacOptions ++= Seq(
    "-Xfatal-warnings",              // Fail the compilation if there are any warnings
    "-deprecation",                  // Emit warning and location for usages of deprecated APIs
    "-explaintypes",                 // Explain type errors in more detail
    "-feature",                      // Emit warning and location for usages of features that should be imported explicitly
    "-language:higherKinds",         // Allow higher-kinded types
    "-language:postfixOps",          // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused
    "-Ywarn-unused:imports",         // Warn if an import selector is not referenced
    "-Ywarn-unused:locals",          // Warn if a local definition is unused
    "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused
    "-Ywarn-unused:privates",        // Warn if a private member is unused
    "-Ywarn-value-discard",          // Warn when non-Unit expression results are unused
    "-Ymacro-annotations"
  )

lazy val scala2_13 = "2.13.3"

lazy val tofuVersion            = "0.8.0"
lazy val derevoVersion          = "0.11.5"
lazy val supertaggedVersion     = "2.0-RC2"
lazy val enumeratumVersion      = "1.6.1"
lazy val enumeratumCatsVersion  = "1.6.1"
lazy val enumeratumCirceVersion = "1.6.1"
lazy val circeVersion           = "0.13.0"
lazy val circeDerivationVersion = "0.13.0-M4"
lazy val http4sVersion          = "0.21.8"
lazy val asyncHttpClientVersion = "2.12.1"
lazy val fs2Version             = "2.4.4"
lazy val beru4sVersion          = "0.1.2"
lazy val logbackVersion         = "1.2.3"

lazy val scalacticVersion               = "3.2.0"
lazy val scalatestVersion               = "3.2.0"
lazy val scalacheckVersion              = "1.14.1"
lazy val scalatestPlusScalacheckVersion = "3.2.2.0"

lazy val betterMonadicForVersion = "0.3.1"
lazy val kindProjectorVersion    = "0.11.0"
