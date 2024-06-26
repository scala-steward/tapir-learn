package com.cmartin.aviation

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.cmartin.aviation.api.configuration.ApiConfiguration

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success

object AkkaWebServerApp extends App with ApiConfiguration {
  // A K K A  A C T O R  S Y S T E M
  implicit lazy val system: ActorSystem           = ActorSystem("WebAppActorSystem")
  implicit val executionContext: ExecutionContext = system.dispatcher
  system.log.info(s"Starting WebServer")

  // L A U N C H  W E B  S E R V E R (actor based)
  val bindingFuture: Future[Http.ServerBinding] =
    Http()
      .newServerAt(serverAddress, serverPort)
      .bind(routes)
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

  // Web Server start up management
  bindingFuture.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)

    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()
  }
}
