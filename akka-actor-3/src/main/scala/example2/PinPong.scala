package example2

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

object PinPong extends App {

  object Receiver {
    sealed trait Message
    case class Ping(replyTo: ActorRef[Pong]) extends Message
    case class Pong()

    def apply(): Behavior[Message] = Behaviors.setup[Message] { context =>
      Behaviors.receiveMessage {
        case Ping(replyTo) =>
          context.log.info("receive: Ping")
          replyTo ! Pong()
          Behaviors.same
      }
    }

  }

  object Sender {
    sealed trait Message
    case class WrappedPong(msg: Receiver.Pong) extends Message

    def apply(toRef: ActorRef[Receiver.Message], max: Int): Behavior[Message] =
      Behaviors.setup[Message] { context =>
        toRef ! Receiver.Ping(context.messageAdapter(ref => WrappedPong(ref)))
        def handler(counter: Int): Behavior[Message] =
          if (counter == 0)
            Behaviors.stopped
          else {
            Behaviors.receiveMessage {
              case WrappedPong(_) =>
                context.log.info(s"receive: Pong, $counter")
                if (counter - 1 != 0)
                  toRef ! Receiver.Ping(
                    context.messageAdapter(ref => WrappedPong(ref))
                  )
                handler(counter - 1)
            }
          }
        handler(max)
      }
  }

  def main: Behavior[Any] = Behaviors.setup[Any] { context =>
    val receiverRef = context.spawn(Receiver(), "receiver")
    context.watch(receiverRef)
    val senderRef = context.spawn(Sender(receiverRef, 10), "sender")
    context.watch(senderRef)
    def handler(refCount: Int): Behavior[Any] = {
      if (refCount == 0)
        Behaviors.stopped
      else
        Behaviors.receiveSignal {
          case (context, Terminated(ref)) if ref == receiverRef =>
            context.log.info(s"Terminated($ref)")
            handler(refCount - 1)
          case (context, Terminated(ref)) if ref == senderRef =>
            context.stop(receiverRef)
            context.log.info(s"Terminated($ref)")
            handler(refCount - 1)
        }
    }
    handler(2)
  }

  ActorSystem(main, "main")

}
