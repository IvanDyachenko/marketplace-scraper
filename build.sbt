// scalafmt: { align.tokens.add = [ {code = ":=", owner = "Term.ApplyInfix"}, {code = "~=", owner = "Term.ApplyInfix"}, {code = "++=", owner = "Term.ApplyInfix"} ] }
import com.typesafe.sbt.packager.docker.Cmd

ThisBuild / organization   := "net.dalytics"
ThisBuild / scalaVersion   := "2.13.5"
ThisBuild / homepage       := Some(url("https://github.com/ivandyachenko/marketplace-scraper"))
ThisBuild / publish / skip := true

lazy val `marketplace-domain` = (project in file("modules/domain"))
  .settings(
    moduleName := "marketplace-domain",
    commonSettings,
    commonDependencies,
    compilerOptions,
    compilerDependencies
  )

lazy val `marketplace-parser` = (project in file("modules/parser"))
  .settings(
    moduleName := "marketplace-parser",
    commonSettings,
    commonDependencies,
    compilerOptions,
    compilerDependencies
  )
  .dependsOn(`marketplace-domain` % "test->test;compile->compile")
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    Docker / packageName := "marketplace-parser",
    Docker / version     := sys.env.getOrElse("GITHUB_SHA", default = "latest"),
    Docker / maintainer  := "Ivan Dyachenko <vandyachen@gmail.com>",
    dockerUsername       := Some("ivandyachenko"),
    dockerBaseImage      := "openjdk:11"
  )
  .settings(
    Universal / javaOptions ++= Seq(
      "-Dlogback.configurationFile=/src/resources/logback.xml",
      "-J-XX:+UseContainerSupport",
      "-J-XX:InitialRAMPercentage=25",
      "-J-XX:MaxRAMPercentage=75"
    )
  )

lazy val `marketplace-handler` = (project in file("modules/handler"))
  .settings(
    moduleName := "marketplace-handler",
    commonSettings,
    commonDependencies,
    compilerOptions,
    compilerDependencies
  )
  .dependsOn(`marketplace-domain` % "test->test;compile->compile")
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    Docker / packageName := "marketplace-handler",
    Docker / version     := sys.env.getOrElse("GITHUB_SHA", default = "latest"),
    Docker / maintainer  := "Ivan Dyachenko <vandyachen@gmail.com>",
    dockerUsername       := Some("ivandyachenko"),
    dockerBaseImage      := "openjdk:11",
    dockerBuildOptions  ++= Seq(
      "--secret",
      s"id=mitmproxycert,src=${sys.env.get("GITHUB_WORKSPACE").get}/mitmproxy-ca-cert.pem"
    ),
    dockerCommands       ~= { cmds =>
      val imageConfig = Cmd("# syntax = docker/dockerfile:1.0-experimental")
      imageConfig +: cmds
    },
    dockerCommands      ++= Seq(
      Cmd("USER", "root"),
      Cmd(
        "RUN",
        "--mount=type=secret,id=mitmproxycert",
        "${JAVA_HOME}/bin/keytool -noprompt -importcert -trustcacerts",
        "-keystore ${JAVA_HOME}/lib/security/cacerts",
        "-storepass changeit",
        "-alias mitmproxycert",
        "-file /run/secrets/mitmproxycert"
      )
    )
  )
  .settings(
    Universal / javaOptions ++= Seq(
      "-Dlogback.configurationFile=/src/resources/logback.xml",
      "-J-XX:+UseContainerSupport",
      "-J-XX:InitialRAMPercentage=25",
      "-J-XX:MaxRAMPercentage=75"
    )
  )

lazy val `marketplace-scheduler` = (project in file("modules/scheduler"))
  .settings(
    moduleName := "marketplace-scheduler",
    commonSettings,
    commonDependencies,
    compilerOptions,
    compilerDependencies
  )
  .dependsOn(`marketplace-domain` % "test->test;compile->compile")
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    Docker / packageName := "marketplace-scheduler",
    Docker / version     := sys.env.getOrElse("GITHUB_SHA", default = "latest"),
    Docker / maintainer  := "Ivan Dyachenko <vandyachen@gmail.com>",
    dockerUsername       := Some("ivandyachenko"),
    dockerBaseImage      := "openjdk:11",
    dockerBuildOptions  ++= Seq(
      "--secret",
      s"id=mitmproxycert,src=${sys.env.get("GITHUB_WORKSPACE").get}/mitmproxy-ca-cert.pem"
    ),
    dockerCommands       ~= { cmds =>
      val imageConfig = Cmd("# syntax = docker/dockerfile:1.0-experimental")
      imageConfig +: cmds
    },
    dockerCommands      ++= Seq(
      Cmd("USER", "root"),
      Cmd(
        "RUN",
        "--mount=type=secret,id=mitmproxycert",
        "${JAVA_HOME}/bin/keytool -noprompt -importcert -trustcacerts",
        "-keystore ${JAVA_HOME}/lib/security/cacerts",
        "-storepass changeit",
        "-alias mitmproxycert",
        "-file /run/secrets/mitmproxycert"
      )
    )
  )
  .settings(
    Universal / javaOptions ++= Seq(
      "-Dlogback.configurationFile=/src/resources/logback.xml",
      "-J-XX:+UseContainerSupport",
      "-J-XX:InitialRAMPercentage=25",
      "-J-XX:MaxRAMPercentage=75"
    )
  )

lazy val `marketplace-scraper` = (project in file("."))
  .settings(
    moduleName      := "marketplace-scraper",
    skip in publish := true
  )
  .aggregate(
    `marketplace-domain`,
    `marketplace-parser`,
    `marketplace-handler`,
    `marketplace-scheduler`
  )

lazy val commonSettings = Seq(
  Global / fork           := true,
  Global / cancelable     := true,
  Compile / doc / sources := List.empty,
  resolvers              ++= Seq(
    "confluent".at("https://packages.confluent.io/maven/")
  ),
  scalafmtOnCompile       := true
)

lazy val commonDependencies =
  libraryDependencies ++= List(
    "io.monix"              %% "monix"                  % Versions.monix,
    "tf.tofu"               %% "derevo-core"            % Versions.derevo,
    "tf.tofu"               %% "derevo-circe"           % Versions.derevo,
    "tf.tofu"               %% "derevo-pureconfig"      % Versions.derevo,
    "tf.tofu"               %% "derevo-cats-tagless"    % Versions.derevo,
    "ru.tinkoff"            %% "tofu-core"              % Versions.tofu,
    "ru.tinkoff"            %% "tofu-core-higher-kind"  % Versions.tofu,
    "ru.tinkoff"            %% "tofu-env"               % Versions.tofu,
    "ru.tinkoff"            %% "tofu-derivation"        % Versions.tofu,
    "ru.tinkoff"            %% "tofu-optics-core"       % Versions.tofu,
    "ru.tinkoff"            %% "tofu-optics-macro"      % Versions.tofu,
    "ru.tinkoff"            %% "tofu-enums"             % Versions.tofu,
    "ru.tinkoff"            %% "tofu-fs2-interop"       % Versions.tofu,
    "ru.tinkoff"            %% "tofu-doobie"            % Versions.tofu,
    "ru.tinkoff"            %% "tofu-logging"           % Versions.tofu,
    "com.github.pureconfig" %% "pureconfig"             % Versions.pureconfig,
    "com.github.pureconfig" %% "pureconfig-enumeratum"  % Versions.pureconfig,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % Versions.pureconfig,
    "org.rudogma"           %% "supertagged"            % Versions.supertagged,
    "com.beachape"          %% "enumeratum"             % Versions.enumeratum,
    "com.beachape"          %% "enumeratum-circe"       % Versions.enumeratum,
    "com.beachape"          %% "enumeratum-cats"        % Versions.enumeratum,
    "io.circe"              %% "circe-core"             % Versions.circe,
    "io.circe"              %% "circe-parser"           % Versions.circe,
    "io.circe"              %% "circe-generic-extras"   % Versions.circe,
    "io.circe"              %% "circe-derivation"       % Versions.circeDerivation,
    "com.github.fd4s"       %% "vulcan"                 % Versions.vulkan,
    "com.github.fd4s"       %% "vulcan-generic"         % Versions.vulkan,
    "com.github.fd4s"       %% "vulcan-enumeratum"      % Versions.vulkan,
    "co.fs2"                %% "fs2-core"               % Versions.fs2,
    "com.github.fd4s"       %% "fs2-kafka"              % Versions.fs2Kafka,
    "com.github.fd4s"       %% "fs2-kafka-vulcan"       % Versions.fs2Kafka,
    "org.http4s"            %% "http4s-dsl"             % Versions.http4s,
    "org.http4s"            %% "http4s-circe"           % Versions.http4s,
    "org.http4s"            %% "http4s-blaze-client"    % Versions.http4sBlazeClient,
    "org.tpolecat"          %% "doobie-core"            % Versions.doobie,
    "org.tpolecat"          %% "doobie-hikari"          % Versions.doobie,
    "ru.yandex.clickhouse"   % "clickhouse-jdbc"        % Versions.clickhouseJDBC,
    "ch.qos.logback"         % "logback-classic"        % Versions.logback,
    "org.scalactic"         %% "scalactic"              % Versions.scalactic               % "test",
    "org.scalatest"         %% "scalatest"              % Versions.scalatest               % "test",
    "org.scalatest"         %% "scalatest-flatspec"     % Versions.scalatest               % "test",
    "org.scalacheck"        %% "scalacheck"             % Versions.scalacheck              % "test",
    "org.scalatestplus"     %% "scalacheck-1-14"        % Versions.scalatestPlusScalacheck % "test"
  )

lazy val compilerDependencies =
  libraryDependencies ++= List(
    "com.olegpy"     %% "better-monadic-for" % Versions.betterMonadicFor,
    ("org.typelevel" %% "kind-projector"     % Versions.kindProjector).cross(CrossVersion.full)
  ).map(compilerPlugin)

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
    "-Ymacro-annotations",
    "-Wunused:params"
  )
