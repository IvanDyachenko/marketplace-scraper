// scalafmt: { align.tokens.add = [ {code = ":=", owner = "Term.ApplyInfix"}, {code = "~=", owner = "Term.ApplyInfix"}, {code = "++=", owner = "Term.ApplyInfix"} ] }
import com.typesafe.sbt.packager.docker.Cmd

ThisBuild / organization   := "net.dalytics"
ThisBuild / scalaVersion   := "2.13.8"
ThisBuild / homepage       := Some(url("https://github.com/ivandyachenko/marketplace-scraper"))
ThisBuild / publish / skip := true

lazy val `marketplace-common` = (project in file("modules/common"))
  .settings(
    moduleName := "marketplace-common",
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
  .dependsOn(`marketplace-common` % "test->test;compile->compile")
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
  .dependsOn(`marketplace-common` % "test->test;compile->compile")
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
  .dependsOn(`marketplace-common` % "test->test;compile->compile")
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

lazy val `marketplace-enricher` = (project in file("modules/enricher"))
  .settings(
    moduleName := "marketplace-enricher",
    commonSettings,
    commonDependencies,
    compilerOptions,
    compilerDependencies
  )
  .dependsOn(`marketplace-common` % "test->test;compile->compile")
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    Docker / packageName := "marketplace-enricher",
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

lazy val `marketplace-scraper` = (project in file("."))
  .settings(
    moduleName      := "marketplace-scraper",
    skip in publish := true
  )
  .aggregate(
    `marketplace-common`,
    `marketplace-parser`,
    `marketplace-handler`,
    `marketplace-scheduler`,
    `marketplace-enricher`
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
    "io.monix"              %% "monix"                    % Versions.monix,
    "org.typelevel"         %% "cats-core"                % Versions.catsCore,
    "org.typelevel"         %% "cats-effect"              % Versions.catsEffect,
    "tf.tofu"               %% "derevo-core"              % Versions.derevo,
    "tf.tofu"               %% "derevo-circe"             % Versions.derevo,
    "tf.tofu"               %% "derevo-tethys"            % Versions.derevo,
    "tf.tofu"               %% "derevo-pureconfig"        % Versions.derevo,
    "tf.tofu"               %% "derevo-cats-tagless"      % Versions.derevo,
    "tf.tofu"               %% "tofu-core"                % Versions.tofu,
    "tf.tofu"               %% "tofu-core-higher-kind"    % Versions.tofu,
    "tf.tofu"               %% "tofu-env"                 % Versions.tofu,
    "tf.tofu"               %% "tofu-derivation"          % Versions.tofu,
    "tf.tofu"               %% "tofu-optics-core"         % Versions.tofu,
    "tf.tofu"               %% "tofu-optics-macro"        % Versions.tofu,
    "tf.tofu"               %% "tofu-enums"               % Versions.tofu,
    "tf.tofu"               %% "tofu-fs2-interop"         % Versions.tofu,
    "tf.tofu"               %% "tofu-doobie"              % Versions.tofu,
    "tf.tofu"               %% "tofu-logging"             % Versions.tofu,
    "com.github.pureconfig" %% "pureconfig"               % Versions.pureconfig,
    "com.github.pureconfig" %% "pureconfig-enumeratum"    % Versions.pureconfig,
    "com.github.pureconfig" %% "pureconfig-cats-effect"   % Versions.pureconfig,
    "org.rudogma"           %% "supertagged"              % Versions.supertagged,
    "com.beachape"          %% "enumeratum"               % Versions.enumeratum,
    "com.beachape"          %% "enumeratum-cats"          % Versions.enumeratum,
    "com.beachape"          %% "enumeratum-circe"         % Versions.enumeratum,
    "io.circe"              %% "circe-core"               % Versions.circe,
    "io.circe"              %% "circe-parser"             % Versions.circe,
    "io.circe"              %% "circe-generic-extras"     % Versions.circe,
    "io.circe"              %% "circe-derivation"         % Versions.circeDerivation,
    "com.tethys-json"       %% "tethys-core"              % Versions.tethys,
    "com.tethys-json"       %% "tethys-circe"             % Versions.tethys,
    "com.tethys-json"       %% "tethys-jackson"           % Versions.tethys,
    "com.tethys-json"       %% "tethys-derivation"        % Versions.tethys,
    "com.tethys-json"       %% "tethys-enumeratum"        % Versions.tethys,
    "com.github.fd4s"       %% "vulcan"                   % Versions.vulkan,
    "com.github.fd4s"       %% "vulcan-generic"           % Versions.vulkan,
    "com.github.fd4s"       %% "vulcan-enumeratum"        % Versions.vulkan,
    "co.fs2"                %% "fs2-core"                 % Versions.fs2,
    "com.github.fd4s"       %% "fs2-kafka"                % Versions.fs2Kafka,
    "com.github.fd4s"       %% "fs2-kafka-vulcan"         % Versions.fs2Kafka,
    ("org.apache.kafka"      % "kafka-streams"            % Versions.kafkaStreams).exclude("log4j", "log4j").exclude("org.slf4j", "slf4j-log4j12"),
    "com.compstak"          %% "kafka-streams4s-core"     % Versions.kafkaStreams4s,
    "io.confluent"           % "kafka-streams-avro-serde" % Versions.kafkaStreamsAvroSerde,
    "io.confluent"           % "monitoring-interceptors"  % Versions.monitoringInterceptors,
    "org.http4s"            %% "http4s-dsl"               % Versions.http4s,
    "org.http4s"            %% "http4s-circe"             % Versions.http4s,
    "org.http4s"            %% "http4s-blaze-client"      % Versions.http4sBlazeClient,
    "org.tpolecat"          %% "doobie-core"              % Versions.doobie,
    "org.tpolecat"          %% "doobie-hikari"            % Versions.doobie,
    "ru.yandex.clickhouse"   % "clickhouse-jdbc"          % Versions.clickhouseJDBC,
    "org.slf4j"              % "slf4j-api"                % Versions.slf4j,
    "ch.qos.logback"         % "logback-classic"          % Versions.logback,
    "org.scalactic"         %% "scalactic"                % Versions.scalactic               % "test",
    "org.scalatest"         %% "scalatest"                % Versions.scalatest               % "test",
    "org.scalatest"         %% "scalatest-flatspec"       % Versions.scalatest               % "test",
    "org.scalacheck"        %% "scalacheck"               % Versions.scalacheck              % "test",
    "org.scalatestplus"     %% "scalacheck-1-14"          % Versions.scalatestPlusScalacheck % "test"
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
