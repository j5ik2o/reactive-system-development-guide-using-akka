package example1

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, Terminated }
import example1.MapActor.Controller.Start
import example1.MapActor.HashMap.{ CombineMap, GetEntry, GetEntryReply, Stop }

object MapActor extends App {

  object HashMap {

    sealed trait Message
    final case class GetEntry[A, B](key: A, replyTo: ActorRef[GetEntryReply[B]]) extends Message
    final case class GetEntryReply[B](value: Option[B])
    final case class CombineMap[K, V](values: Map[K, V]) extends Message
    final case object Stop                               extends Message

    def apply[K, V](initial: Map[K, V] = Map.empty[K, V]): Behavior[Message] =
      Behaviors.setup[Message] { context =>
        var map = initial
        Behaviors.receiveMessage[Message] {
          case Stop => Behaviors.stopped
          case c: CombineMap[K, V] =>
            context.log.info(s"msg = $c")
            map ++= c.values
            Behaviors.same
          case g: GetEntry[K, V] =>
            context.log.info(s"msg = $g")
            g.replyTo ! GetEntryReply(map.get(g.key))
            Behaviors.same
        }
      }
  }

  object Controller {

    sealed trait Message
    case class Start(values: Map[Int, String])                  extends Message
    case class WrappedGetEntryReply[B](value: GetEntryReply[B]) extends Message

    def apply(): Behavior[Message] =
      Behaviors.setup[Message] { context =>
        val hashMapRef: ActorRef[HashMap.Message] =
          context.spawn(HashMap[Int, String](), "hashMap")
        context.watch(hashMapRef)
        Behaviors
          .receiveMessage[Message] {
            case msg: WrappedGetEntryReply[String] =>
              context.log.info(s"msg = $msg")
              hashMapRef ! Stop
              Behaviors.same
            case msg: Start =>
              context.log.info(s"msg = $msg")
              hashMapRef ! CombineMap(msg.values)
              hashMapRef ! GetEntry(
                1,
                context.messageAdapter[GetEntryReply[String]](ref => WrappedGetEntryReply(ref))
              )
              Behaviors.same
          }
          .receiveSignal {
            case (context, Terminated(ref)) if ref == hashMapRef =>
              context.log.info(s"terminate $ref")
              Behaviors.stopped
          }
      }

  }

  def main: Behavior[Any] =
    Behaviors.setup[Any] { context =>
      val controllerRef = context.spawn(Controller(), "controller")
      context.watch(controllerRef)
      controllerRef ! Start(Map(1 -> "abc"))
      Behaviors.receiveSignal {
        case (context, Terminated(ref)) if ref == controllerRef =>
          context.log.info(s"terminate $ref")
          Behaviors.stopped
      }
    }

  ActorSystem(main, "main")

}
