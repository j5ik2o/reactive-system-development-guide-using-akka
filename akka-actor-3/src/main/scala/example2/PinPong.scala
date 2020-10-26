package example2

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

object PinPong extends App {
  sealed trait Message
  trait Reply
  case class Pong() extends Reply
  case class Ping(replyTo: ActorRef[Pong]) extends Message

  def receiver(): Behavior[Message] = Behaviors.setup[Message] { context =>
    Behaviors.receiveMessage {
      case Ping(replyTo) =>
        context.log.info("receive: Ping")
        replyTo ! Pong()
        Behaviors.same
    }
  }

  def sender(toRef: ActorRef[Message], max: Int): Behavior[Pong] =
    Behaviors.setup[Pong] { context =>
      toRef ! Ping(context.self)
      def handler(counter: Int): Behavior[Pong] =
        if (counter == 0)
          Behaviors.stopped
        else {
          Behaviors.receiveMessage {
            case Pong() =>
              context.log.info(s"receive: Pong, $counter")
              if (counter - 1 != 0)
                toRef ! Ping(context.self)
              handler(counter - 1)
          }
        }
      handler(max)
    }

  def main: Behavior[Any] = Behaviors.setup[Any] { context =>
    val receiverRef = context.spawn(receiver(), "receiver")
    context.watch(receiverRef)
    val senderRef = context.spawn(sender(receiverRef, 10), "sender")
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
