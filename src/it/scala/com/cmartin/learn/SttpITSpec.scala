package com.cmartin.learn

import com.cmartin.learn.api.ApiModel.{BuildInfoDto, TransferDto}
import com.cmartin.learn.api.TransferEndpoint
import io.circe
import io.circe.generic.auto._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client.asynchttpclient.zio.{AsyncHttpClientZioBackend, SttpClient}
import sttp.client.circe._
import sttp.client.{basicRequest, _}
import sttp.model.StatusCode
import zio.UIO

class SttpITSpec extends AnyFlatSpec with Matchers {

  val runtime = zio.Runtime.default

  behavior of "REST API"

  it should "respond Ok status for a health GET request" in {
    val request =
      basicRequest
        .get(uri"http://localhost:8080/api/v1.0/health")
        .response(asJson[BuildInfoDto])

    val doGet = sendRequest[BuildInfoDto](request)

    val layeredDoGet = doGet.provideCustomLayer(AsyncHttpClientZioBackend.layer())

    val response = runtime.unsafeRun(layeredDoGet)

    response.code shouldBe StatusCode.Ok
  }

  it should "respond Ok status for a get Transfer GET request" in {
    val id = 1
    val request =
      basicRequest
        .get(uri"http://localhost:8080/api/v1.0/transfers/$id")
        .response(asJson[TransferDto])

    val doGet = sendRequest[TransferDto](request)

    val layeredDoGet = doGet.provideCustomLayer(AsyncHttpClientZioBackend.layer())

    val response = runtime.unsafeRun(layeredDoGet)

    response.code shouldBe StatusCode.Ok
  }

  it should "respond NotFound status for a non-existent Transfer GET  request" in {
    val id = 404
    val request =
      basicRequest
        .get(uri"http://localhost:8080/api/v1.0/transfers/$id")
        .response(asJson[TransferDto])

    val doGet = sendRequest[TransferDto](request)

    val layeredDoGet = doGet.provideCustomLayer(AsyncHttpClientZioBackend.layer())

    val response = runtime.unsafeRun(layeredDoGet)

    response.code shouldBe StatusCode.NotFound
  }

  it should "respond InternalServerError status for a simulated error in a Transfer GET request" in {
    val id = 500
    val request =
      basicRequest
        .get(uri"http://localhost:8080/api/v1.0/transfers/$id")
        .response(asJson[TransferDto])

    val doGet = sendRequest[TransferDto](request)

    val layeredDoGet = doGet.provideCustomLayer(AsyncHttpClientZioBackend.layer())

    val response = runtime.unsafeRun(layeredDoGet)

    response.code shouldBe StatusCode.InternalServerError
  }

  it should "TODO: respond Created status for a Transfer POST request" in {
    val request: Request[Either[String, String], Nothing] =
      basicRequest
        .body(TransferEndpoint.transferExample)
        .post(uri"http://localhost:8080/api/v1.0/transfers/")

    val doPost       = sendPostRequest[TransferDto](request)
    val layeredDoGet = doPost.provideCustomLayer(AsyncHttpClientZioBackend.layer())

    val response = runtime.unsafeRun(layeredDoGet)

    response.code shouldBe StatusCode.Created
  }

  def sendRequest[T](request: RequestT[Identity, Either[ResponseError[circe.Error], T], Nothing]) =
    for {
      response <- SttpClient.send(request)
      _        <- UIO(info(s"response.code: ${response.code}"))
      _        <- UIO(info(s"response.body: ${response.body}"))
    } yield response

  def sendPostRequest[T](request: Request[Either[String, String], Nothing]) =
    for {
      response <- SttpClient.send(request)
      _        <- UIO(info(s"response.code: ${response.code}"))
      _        <- UIO(info(s"response.body: ${response.body}"))
    } yield response

}
