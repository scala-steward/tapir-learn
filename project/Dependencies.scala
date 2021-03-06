import sbt._

object Dependencies {

  val mainAndTest = Seq(
    // T A P I R
    "com.softwaremill.sttp.tapir" %% "tapir-core"                 % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server"     % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe"           % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"         % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml"   % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-akka-http" % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-json4s"          % Versions.tapir,
    "com.typesafe.akka"           %% "akka-slf4j"                 % Versions.akka,
    "ch.qos.logback"               % "logback-classic"            % Versions.logback,
    "com.github.mlangc"           %% "slf4zio"                    % Versions.slf4zio,
    "dev.zio"                     %% "zio"                        % Versions.zio,
    "org.json4s"                  %% "json4s-ext"                 % Versions.json4s,
    "org.json4s"                  %% "json4s-native"              % Versions.json4s,
    /*
       T E S T
     */
    "org.scalatest"     %% "scalatest"         % Versions.scalatest % "it, test",
    "com.typesafe.akka" %% "akka-http-testkit" % Versions.akkaHttp  % Test,
    "com.typesafe.akka" %% "akka-testkit"      % Versions.akka      % Test,
    "dev.zio"           %% "zio-test"          % Versions.zio       % "test",
    "dev.zio"           %% "zio-test-sbt"      % Versions.zio       % "test",
    // S T T P
    "com.softwaremill.sttp.client3" %% "core"                          % Versions.sttp % "it, test",
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % Versions.sttp % "it, test",
    "com.softwaremill.sttp.client3" %% "circe"                         % Versions.sttp % "it, test"
  )
}
