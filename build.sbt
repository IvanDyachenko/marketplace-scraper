ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala2_13

lazy val scala2_13 = "2.13.3"

lazy val `marketplace-crawler` =
  project
    .in(file("."))
    .settings(name := "marketplace-crawler")
    .settings(organization := "io.ivandyachenko")
    .settings(homepage := Some(url("https://github.com/ivandyachenko/marketplace-crawler")))
    .settings(
      projectSettings,
      projectDependencies,
      testDependencies,
      compilerOptions,
      compilerDependencies
    )

lazy val projectSettings = Seq(
  scalaVersion := scala2_13,
  scalafmtOnCompile := true,
  fork in Global := true, // https://github.com/sbt/sbt/issues/2274
  cancelable in Global := true
)

lazy val projectDependencies =
  libraryDependencies ++= List(
    "io.monix"              %% "monix"                    % Versions.monix,
    "org.rudogma"           %% "supertagged"              % Versions.supertagged,
    "com.beachape"          %% "enumeratum"               % Versions.enumeratum,
    "com.beachape"          %% "enumeratum-circe"         % Versions.enumeratum,
    "com.beachape"          %% "enumeratum-cats"          % Versions.enumeratum,
    "ru.tinkoff"            %% "tofu-core"                % Versions.tofu,
    "ru.tinkoff"            %% "tofu-core-higher-kind"    % Versions.tofu,
    "ru.tinkoff"            %% "tofu-env"                 % Versions.tofu,
    "ru.tinkoff"            %% "tofu-derivation"          % Versions.tofu,
    "ru.tinkoff"            %% "tofu-optics-core"         % Versions.tofu,
    "ru.tinkoff"            %% "tofu-optics-macro"        % Versions.tofu,
    "ru.tinkoff"            %% "tofu-enums"               % Versions.tofu,
    "ru.tinkoff"            %% "tofu-fs2-interop"         % Versions.tofu,
    "ru.tinkoff"            %% "tofu-doobie"              % Versions.tofu,
    "ru.tinkoff"            %% "tofu-logging"             % Versions.tofu,
    "org.manatki"           %% "derevo-cats-tagless"      % Versions.derevo,
    "io.circe"              %% "circe-core"               % Versions.circe,
    "io.circe"              %% "circe-derivation"         % Versions.circeDerivation,
    "co.fs2"                %% "fs2-core"                 % Versions.fs2,
    "org.http4s"            %% "http4s-dsl"               % Versions.http4s,
    "org.http4s"            %% "http4s-circe"             % Versions.http4s,
    "org.http4s"            %% "http4s-blaze-client"      % Versions.http4s,
    "org.http4s"            %% "http4s-async-http-client" % Versions.http4s,
    "org.asynchttpclient"    % "async-http-client"        % Versions.asyncHttpClient,
    "org.tpolecat"          %% "doobie-core"              % Versions.doobie,
    "org.tpolecat"          %% "doobie-hikari"            % Versions.doobie,
    "ru.yandex.clickhouse"   % "clickhouse-jdbc"          % Versions.clickhouseJDBC,
    "com.github.pureconfig" %% "pureconfig"               % Versions.pureconfig,
    "com.github.pureconfig" %% "pureconfig-cats-effect"   % Versions.pureconfig,
    "ch.qos.logback"         % "logback-classic"          % Versions.logback
  )

lazy val testDependencies =
  libraryDependencies ++= List(
    "org.scalactic"     %% "scalactic"          % Versions.scalactic               % "test",
    "org.scalatest"     %% "scalatest"          % Versions.scalatest               % "test",
    "org.scalatest"     %% "scalatest-flatspec" % Versions.scalatest               % "test",
    "org.scalacheck"    %% "scalacheck"         % Versions.scalacheck              % "test",
    "org.scalatestplus" %% "scalacheck-1-14"    % Versions.scalatestPlusScalacheck % "test"
  )

lazy val compilerDependencies =
  libraryDependencies ++= List(
    compilerPlugin("com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor),
    compilerPlugin(("org.typelevel" %% "kind-projector" % Versions.kindProjector).cross(CrossVersion.full))
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
