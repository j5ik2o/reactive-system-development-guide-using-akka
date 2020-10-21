package example1

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import example1.MapActor.{MapInput, MapOutput}
import example1.ReduceActor.{ReduceInput, ReduceOutput}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration._

object MasterActor extends App {

  sealed trait Message
  trait Reply
  case class CountReply(result: Map[String, Int]) extends Reply
  case class Count(data: List[String], replyTo: ActorRef[CountReply])
      extends Message
  case class WrappedMapOutput(response: MapOutput) extends Message
  case class WrappedReduceOutput(response: ReduceOutput) extends Message

  def apply(mapN: Int, reduceN: Int): Behavior[Message] = {
    Behaviors.setup[Message] { context =>
      @scala.annotation.tailrec
      def split(data: List[(String, Int)],
                result: Vector[ReduceInput]): Vector[ReduceInput] = {
        data match {
          case Nil => result
          case x :: xs =>
            val spannedXs = xs.span(_._1 == x._1)
            split(spannedXs._2, result :+ createReduceInput(x, spannedXs._1))
        }
      }
      def createReduceInput(head: (String, Int),
                            remainder: List[(String, Int)]): ReduceInput = {
        val entries = ListBuffer.empty[(String, Int)] += head
        entries ++= remainder
        val mapper: ActorRef[ReduceOutput] =
          context.messageAdapter[ReduceOutput](rsp => WrappedReduceOutput(rsp))
        val reduceInput = ReduceInput(head._1, entries.toList, mapper)
        reduceInput
      }
      def reduced(reduceOutputs: List[ReduceOutput],
                  replyTo: ActorRef[CountReply]): Behavior[Message] =
        Behaviors.setup[Message] { context =>
          context.children.foreach { c =>
            context.stop(c)
          }
          val result = reduceOutputs.map(e => (e.key, e.value)).toMap
          for (o <- reduceOutputs) printf("%-10s:%8d\n", o.key, o.value)
          replyTo ! CountReply(result)
          init
        }
      def reducing(reduceInputs: List[ReduceInput],
                   replyTo: ActorRef[CountReply]) = {
        val reduceOutputs = ListBuffer.empty[ReduceOutput]
        Behaviors.receiveMessage[Message] {
          case WrappedReduceOutput(ro) =>
            reduceOutputs += ro
            if (reduceInputs.length == reduceOutputs.length)
              reduced(reduceOutputs.toList, replyTo)
            else
              Behaviors.same
        }
      }
      def mapped(mapOutputs: List[MapOutput],
                 replyTo: ActorRef[CountReply]): Behavior[Message] = {
        val reduceInputs = ListBuffer.empty[ReduceInput]
        Behaviors.setup[Message] { context =>
          context.children.foreach { c =>
            context.stop(c)
          }
          val reduceActors: Seq[ActorRef[ReduceInput]] = for (i <- 1 to reduceN)
            yield context.spawn(ReduceActor(), s"reducer-$i")
          val sortedMapOutputs =
            mapOutputs.flatMap(_.entries).sortWith(_._1 < _._1).toList
          val splited = split(sortedMapOutputs, Vector.empty)
          reduceInputs ++= splited
          splited.zipWithIndex.foreach {
            case (e, index) =>
              reduceActors(index % reduceActors.length) ! e
          }
          reducing(reduceInputs.toList, replyTo)
        }
      }
      def mapping(data: List[String], replyTo: ActorRef[CountReply]) = {
        val mapOutputs = ListBuffer.empty[MapOutput]
        Behaviors.receiveMessage[Message] {
          case WrappedMapOutput(mo) =>
            mapOutputs += mo
            if (mapOutputs.length == data.length)
              mapped(mapOutputs.toList, replyTo)
            else
              Behaviors.same
        }
      }
      def init: Behavior[Message] = Behaviors.receiveMessage[Message] {
        case Count(data, replyTo) =>
          val mapActors: Seq[ActorRef[MapInput]] = for (i <- 1 to mapN)
            yield context.spawn(MapActor(), s"mapper-$i")
          val mapper: ActorRef[MapOutput] =
            context.messageAdapter[MapOutput](rsp => WrappedMapOutput(rsp))
          data.zipWithIndex.foreach {
            case (e, index) =>
              mapActors(index % mapActors.length) ! MapInput(e, mapper)
          }
          mapping(data, replyTo)
      }
      init
    }
  }

  implicit val system = ActorSystem(apply(3, 3), "master")
  implicit val timeout: Timeout = 3.seconds
  val future1 = system.ask[CountReply](
    ref =>
      Count(List("Hello World", "Hello Scala World", "Hello Java World"), ref)
  )
  val result1 = Await.result(future1, Duration.Inf)
  println(result1)
  val future2 = system.ask[CountReply](
    ref =>
      Count(List("Hello World", "Hello Scala World", "Hello Kotlin World"), ref)
  )
  val result2 = Await.result(future2, Duration.Inf)
}
