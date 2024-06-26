package com.cmartin.aviation.api

import akka.http.scaladsl.model.headers.`Content-Location`
import com.cmartin.aviation.api.BaseEndpoint._
import com.cmartin.aviation.api.Model._
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

trait AirportEndpoints {

  lazy val getByIataCodeEndpoint: PublicEndpoint[String, OutputError, AirportView, Any] =
    baseEndpoint.get
      .name("airport-get-by-iata-code-endpoint")
      .description("Retrieves an Airport by its iata code")
      .in(airportPath)
      .in(iataCodePath)
      .out(jsonBody[AirportView].example(AirportEndpoints.airportViewExample))
      .errorOut(
        oneOf[OutputError](
          badRequestMapping,
          notFoundMapping,
          internalErrorMapping,
          defaultMapping
        )
      )

  lazy val postEndpoint: PublicEndpoint[AirportView, OutputError, (String, AirportView), Any] =
    baseEndpoint.post
      .name("airport-post-endpoint")
      .description("Creates an Airport")
      .in(airportPath)
      .in(jsonBody[AirportView].example(AirportEndpoints.airportViewExample))
      .out(
        statusCode(StatusCode.Created)
          .and(header[String](`Content-Location`.name))
          .and(jsonBody[AirportView])
      )
      .errorOut(
        oneOf[OutputError](
          badRequestMapping,
          internalErrorMapping,
          defaultMapping
        )
      )

  lazy val deleteEndpoint: PublicEndpoint[String, OutputError, Unit, Any] =
    baseEndpoint.delete
      .name("airport-delete-endpoint")
      .description("Deletes an Airport by its iata code")
      .in(airportPath)
      .in(iataCodePath)
      .out(statusCode(StatusCode.NoContent))
      .errorOut(
        oneOf[OutputError](
          badRequestMapping,
          internalErrorMapping,
          defaultMapping
        )
      )

  lazy val airportsResource = "countries"
  lazy val airportPath      = baseApiResource / airportsResource
  lazy val iataCodePath     = path[String]("iataCode")
}

object AirportEndpoints extends AirportEndpoints {
  val airportViewExample = AirportView("Madrid Barajas", "MAD", "LEMD", "es")
}
