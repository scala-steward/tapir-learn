package com.cmartin.learn.domain

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import zio.Task

object ProcessorModel {

  sealed abstract class Base(ip:String="")

  class Derived(ip:String) extends Base(ip) {
    val x = ip
  }

  val x = new Derived("")

  sealed trait ProcessorDefinition {
    val inputPath: String
    val resultPath: String
    val outputPath: String
    val name: String
  }

  final case class FilterDefinition(
      predicate: String,
      inputPath: String = "",
      resultPath: String = "",
      outputPath: String = "",
      name: String = "filter"
  ) extends ProcessorDefinition

  final case class JsltDefinition(
      transform: String,
      inputPath: String = "",
      resultPath: String = "",
      outputPath: String = "",
      name: String = "jslt"
  ) extends ProcessorDefinition

  final case class RestDefinition(
      method: String,
      url: String,
      inputPath: String = "",
      resultPath: String = "",
      outputPath: String = "",
      name: String = "rest"
  ) extends ProcessorDefinition

  sealed trait Processor[E <: ProcessorDefinition] {

    def doTask(message: String): Task[String]

  }

  abstract class BaseProcessor(definition: ProcessorDefinition) extends Processor[ProcessorDefinition] {
    final def handle(message: String): Task[String] =
      for {
        ipMsg   <- doInputPath(message)
        taskMsg <- doTask(ipMsg)
        rpMsg   <- doResultPath(taskMsg)
        opMsg   <- doOutputPath(rpMsg)
      } yield opMsg

    private def doInputPath(message: String): Task[String] =
      Task {
        val processedMessage = message + ".input#"
        debugMessage(s".inputPath - ${definition.inputPath}", processedMessage)
        processedMessage
      }

    private def doResultPath(message: String): Task[String] =
      Task {
        val processedMessage = message + ".result#"
        debugMessage(s".resultPath - ${definition.resultPath}", processedMessage)
        processedMessage
      }

    private def doOutputPath(message: String): Task[String] =
      Task {
        val processedMessage = message + ".output#"
        debugMessage(s".outputPath - ${definition.outputPath}", processedMessage)
        processedMessage
      }

  }

  final class FilterProcessor(definition: FilterDefinition) extends BaseProcessor(definition) {
    override def doTask(message: String): Task[String] =
      Task {
        val processedMessage = message + ".filtered#"
        debugMessage(s".doFilter - predicate: ${definition.predicate}, message", processedMessage)
        processedMessage
      }
  }

  object FilterProcessor {
    def apply(definition: FilterDefinition): FilterProcessor = new FilterProcessor(definition)
  }

  final class JsltProcessor(definition: JsltDefinition) extends Processor[ProcessorDefinition] {
    override def doTask(message: String): Task[String] =
      Task {
        val processedMessage = message + ".transformed#"
        debugMessage(s".doTransform - transformation: ${definition.transform}, message", processedMessage)
        processedMessage
      }
  }

  object JsltProcessor {
    def apply(definition: JsltDefinition): JsltProcessor = new JsltProcessor(definition)
  }

  final class RestProcessor(definition: RestDefinition) extends Processor[ProcessorDefinition] {
    override def doTask(message: String): Task[String] =
      Task {
        val processedMessage = message + ".response#"
        debugMessage(s".doMethod - url: ${definition.url}, message", processedMessage)
        processedMessage
      }
  }

  object RestProcessor {
    def apply(definition: RestDefinition): RestProcessor = new RestProcessor(definition)
  }

  // h e l p e r s
  def debugMessage(prefix: String, message: String) =
    println(s"$prefix:\n |-> $message")

  object GenericDerivation {

    implicit val eventEncoder: Encoder[ProcessorDefinition] = Encoder.instance {
      case filter @ FilterDefinition(_, _, _, _, _) => filter.asJson
      case jslt @ JsltDefinition(_, _, _, _, _)     => jslt.asJson
      case rest @ RestDefinition(_, _, _, _, _, _)  => rest.asJson
    }

    implicit val eventDecoder: Decoder[ProcessorDefinition] =
      List[Decoder[ProcessorDefinition]](
        Decoder[FilterDefinition].widen,
        Decoder[JsltDefinition].widen,
        Decoder[RestDefinition].widen
      ).reduceLeft(_ or _)
  }

}
