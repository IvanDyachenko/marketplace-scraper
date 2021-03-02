import com.typesafe.sbt.packager.docker.Cmd

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
    .enablePlugins(DockerPlugin)
    .enablePlugins(AshScriptPlugin)
    .enablePlugins(JavaAppPackaging)
    .settings(
      packageName in Docker := "marketplace-crawler",
      version in Docker := sys.env.getOrElse("GITHUB_SHA", default = "latest"),
      maintainer in Docker := "Ivan Dyachenko <vandyachen@gmail.com>",
      dockerBaseImage := "openjdk:11",
      dockerExposedPorts := Seq(9000),
      dockerUsername := Some("ivandyachenko"),
      dockerBuildOptions ++= Seq(
        "--secret",
        s"id=mitmproxycert,src=${sys.env.get("GITHUB_WORKSPACE").get}/mitmproxy-ca-cert.pem"
      ),
      dockerCommands ~= { cmds =>
        val imageConfig = Cmd("# syntax = docker/dockerfile:1.0-experimental")
        imageConfig +: cmds
      },
      dockerCommands ++= Seq(
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
      ),
      javaOptions in Universal ++= Seq(
        "-J-XX:+UnlockExperimentalVMOptions",
        "-J-XX:+UseCGroupMemoryLimitForHeap"
      )
    )

lazy val projectSettings = Seq(
  scalaVersion := "2.13.5",
  resolvers += Resolver.sonatypeRepo("snapshots"),
  resolvers += "confluent".at("https://packages.confluent.io/maven/"),
  fork in Global := true, // https://github.com/sbt/sbt/issues/2274
  cancelable in Global := true,
  scalafmtOnCompile := true
)

lazy val projectDependencies =
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
    "org.http4s"            %% "http4s-jdk-http-client" % Versions.http4sJDKHttpClient,
    "org.tpolecat"          %% "doobie-core"            % Versions.doobie,
    "org.tpolecat"          %% "doobie-hikari"          % Versions.doobie,
    "ru.yandex.clickhouse"   % "clickhouse-jdbc"        % Versions.clickhouseJDBC,
    "ch.qos.logback"         % "logback-classic"        % Versions.logback
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
    "-Ymacro-annotations",
    "-Wunused:params"
  )
