package com.cmartin.learn

import com.cmartin.learn.api.ApiModel.ApiBuildInfo
import io.circe.generic.auto._
import sttp.client._
import sttp.client.asynchttpclient.zio._
import sttp.client.circe._
import zio._
import zio.console.Console
import zio.duration._

object GetAndParseJsonZioCirce extends App {
  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {

    case class HttpBinResponse(origin: String, headers: Map[String, String])

    val request =
      basicRequest.
        //get(uri"https://httpbin.org/get")
        //.response(asJson[HttpBinResponse])
        get(uri"http://localhost:8080/api/v1.0/health")
        .response(asJson[ApiBuildInfo])

    val program: ZIO[Console with SttpClient, Throwable, Unit] = for {
      response <- SttpClient.send(request)
      _ <- console.putStrLn(s"response code: ${response.code}")
      _ <- console.putStrLn(response.body.toString)
    } yield ()

    program
      .repeat(
        Schedule.spaced(1.second) *>
        Schedule.recurs(5)
      )
      .provideCustomLayer(AsyncHttpClientZioBackend.layer())
      .fold(
        _ => 1,
        _ => 0
      )
  }
}