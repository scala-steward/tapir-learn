package com.cmartin.learn.api

import com.cmartin.learn.api.ApiModel.{BuildInfoDto, TransferDto}
import com.cmartin.learn.domain.ApiConverters
import io.circe
import io.circe.generic.auto._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client.circe.asJson
import sttp.client.{Response, ResponseError, basicRequest, _}
import sttp.model.{Method, StatusCode}

class TransferApiClientSpec extends AnyFlatSpec with Matchers {

  val runtime = zio.Runtime.default

  val backend = AsyncHttpClientZioBackend.stub
    .whenRequestMatches { req =>
      req.method == Method.GET && req.uri.path.contains("transfers") && req.uri.path.last == "1"
    }
    .thenRespond(TransferEndpoint.transferExample)
    .whenRequestMatches { req =>
      req.method == Method.GET && req.uri.path.contains("transfers") && req.uri.path.last == "400"
    }
    .thenRespond(Response("BAD_REQUEST", StatusCode.BadRequest))
    .whenRequestMatches { req =>
      req.method == Method.GET && req.uri.path.contains("transfers") && req.uri.path.last == "500"
    }
    .thenRespond(Response("SERVER_ERROR", StatusCode.InternalServerError))

  behavior of "REST API Client"

  it should "respond Ok status stub backend for health request" in {
    val dtoResponse: BuildInfoDto = ApiConverters.modelToApi()

    val backend = AsyncHttpClientZioBackend.stub
      .whenRequestMatches { req =>
        req.method == Method.GET && req.uri.path.last == "health"
      }
      .thenRespond(dtoResponse)

    val request =
      basicRequest
        .get(uri"http://localhost:8080/api/v1.0/health")
        .response(asJson[BuildInfoDto])

    val response: Response[Either[ResponseError[circe.Error], BuildInfoDto]] =
      runtime
        .unsafeRun(
          backend.send(request)
        )

    response.code shouldBe StatusCode.Ok
    response.body shouldBe dtoResponse
  }

  it should "respond Ok for an existent transfer identifier" in {
    val request =
      basicRequest
        .get(uri"http://localhost:8080/api/v1.0/transfers/1")
        .response(asJson[TransferDto])

    val response = runtime.unsafeRun(backend.send(request))

    response.code shouldBe StatusCode.Ok
    response.body shouldBe TransferEndpoint.transferExample
  }

  it should "respond Bad Request for an invalid request" in {
    val request =
      basicRequest
        .get(uri"http://localhost:8080/api/v1.0/transfers/400")
        .response(asJson[TransferDto])

    val response = runtime.unsafeRun(backend.send(request))

    response.code shouldBe StatusCode.BadRequest
    response.body.isLeft shouldBe true
  }

  it should "respond Not Found for a missing transfer" in {
    val request =
      basicRequest
        .get(uri"http://localhost:8080/api/v1.0/transfers/404")
        .response(asJson[TransferDto])

    val response = runtime.unsafeRun(backend.send(request))

    response.code shouldBe StatusCode.NotFound
    response.body.isLeft shouldBe true
  }

  it should "respond Server Error for a server failure" in {
    val request =
      basicRequest
        .get(uri"http://localhost:8080/api/v1.0/transfers/500")
        .response(asJson[TransferDto])

    val response = runtime.unsafeRun(backend.send(request))

    response.code shouldBe StatusCode.InternalServerError
    response.body.isLeft shouldBe true
  }

}
