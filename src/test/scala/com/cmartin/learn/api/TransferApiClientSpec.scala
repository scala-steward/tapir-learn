package com.cmartin.learn.api

import com.cmartin.learn.api.Model.BuildInfoDto
import com.cmartin.learn.api.Model.TransferDto
import com.cmartin.learn.domain.ApiConverters
import io.circe
import io.circe.generic.auto._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.Response
import sttp.client3.ResponseError
import sttp.client3._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.basicRequest
import sttp.client3.circe._
import sttp.client3.circe.asJson
import sttp.model.Method
import sttp.model.StatusCode
import org.scalatest.Ignore

@Ignore
class TransferApiClientSpec extends AnyFlatSpec with Matchers {

  import TransferApiClientSpec._

  behavior of "REST API Client"

  "GET method" should "respond Ok status stub backend for health request" in {
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

    val response =
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

  it should "WIP respond Not Found for a missing transfer" in {
    val request =
      basicRequest
        .get(uri"http://localhost:8080/api/v1.0/transfers/404")
        .response(asJson[TransferDto])

    val response = runtime.unsafeRun(backend.send(request))

    response.code shouldBe StatusCode.NotFound
    response.body.isLeft shouldBe true
  }

  it should "respond Server Error for a server failure (GET)" in {
    val request =
      basicRequest
        .get(uri"http://localhost:8080/api/v1.0/transfers/500")
        .response(asJson[TransferDto])

    val response = runtime.unsafeRun(backend.send(request))

    response.code shouldBe StatusCode.InternalServerError
    response.body.isLeft shouldBe true
  }

  "POST method" should "respond Created for a new transfer entity" in {
    val request =
      basicRequest
        .body(TransferEndpoint.transferExample)
        .post(uri"http://localhost:8080/api/v1.0/transfers/")

    val response = runtime.unsafeRun(backend.send(request))

    response.code shouldBe StatusCode.Created
    response.body shouldBe TransferEndpoint.transferExample
  }

  it should "respond Bad Request for an invalid transfer entity" in {
    val request =
      basicRequest
        .body(""" { "transfer" : "invalid" }""")
        .post(uri"http://localhost:8080/api/v1.0/transfers/")

    val response = runtime.unsafeRun(backend.send(request))

    response.code shouldBe StatusCode.BadRequest
    response.body.isLeft shouldBe true
  }

  it should "respond Conflict for a duplicate transfer entity" in {
    val request =
      basicRequest
        .body(""" { "transfer" : "duplicate" }""")
        .post(uri"http://localhost:8080/api/v1.0/transfers/")

    val response = runtime.unsafeRun(backend.send(request))

    response.code shouldBe StatusCode.Conflict
    response.body.isLeft shouldBe true
  }

  it should "respond Server Error for a server failure (POST)" in {
    val request =
      basicRequest
        .body(""" { "transfer" : "server-error" }""")
        .post(uri"http://localhost:8080/api/v1.0/transfers/")

    val response = runtime.unsafeRun(backend.send(request))

    response.code shouldBe StatusCode.InternalServerError
    response.body.isLeft shouldBe true
  }

}

object TransferApiClientSpec {
  val runtime = zio.Runtime.default

  val backend = AsyncHttpClientZioBackend.stub
    .whenRequestMatches { req =>
      req.method == Method.GET && req.uri.path.contains("transfers") && req.uri.path.last == "1"
    }
    .thenRespond(TransferEndpoint.transferExample)
    //
    .whenRequestMatches { req =>
      req.method == Method.GET && req.uri.path.contains("transfers") && req.uri.path.last == "400"
    }
    .thenRespond(Response("BAD_REQUEST", StatusCode.BadRequest))
    //
    .whenRequestMatches { req =>
      req.method == Method.GET && req.uri.path.contains("transfers") && req.uri.path.last == "404"
    }
    .thenRespond(Response("NOT_FOUND", StatusCode.NotFound))
    //
    .whenRequestMatches { req =>
      req.method == Method.GET && req.uri.path.contains("transfers") && req.uri.path.last == "500"
    }
    .thenRespond(Response("SERVER_ERROR", StatusCode.InternalServerError))
    /*
       P O S T
     */
    .whenRequestMatches { req =>
      req.method == Method.POST && req.uri.path.contains("transfers") &&
      req.body.toString.contains("invalid")
    }
    .thenRespond(Response("BAD_REQUEST", StatusCode.BadRequest))
    //
    .whenRequestMatches { req =>
      req.method == Method.POST && req.uri.path.contains("transfers") &&
      req.body.toString.contains("duplicate")
    }
    .thenRespond(Response("CONFLICT", StatusCode.Conflict))
    //
    .whenRequestMatches { req =>
      req.method == Method.POST && req.uri.path.contains("transfers") &&
      req.body.toString.contains("server-error")
    }
    .thenRespond(Response("SERVER_ERROR", StatusCode.InternalServerError))
    //
    .whenRequestMatches { req =>
      req.method == Method.POST && req.uri.path.contains("transfers")
    }
    .thenRespond(Response(TransferEndpoint.transferExample, StatusCode.Created))

}
