package example2

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, Terminated }
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

object PinPongAsk extends App {

  object Receiver {
    sealed trait Message
    case class Ping(replyTo: ActorRef[Pong]) extends Message
    case class Pong()

    def apply(): Behavior[Message] =
      Behaviors.setup[Message] { context =>
        Behaviors.receiveMessage {
          case Ping(replyTo) =>
            context.log.info("receive: Ping")
            // Thread.sleep(5000)
            replyTo ! Pong()
            Behaviors.same
        }
      }

  }

  object Sender {
    sealed trait Message
    case class PongSucceeded(msg: Receiver.Pong) extends Message
    case class PongFailed(ex: Throwable)         extends Message

    def apply(toRef: ActorRef[Receiver.Message], max: Int): Behavior[Message] =
      Behaviors.setup[Message] { context =>
        implicit val timeout = Timeout(3 seconds)
        context.log.info("send: Ping")
        // アスク
        context.ask(toRef, Receiver.Ping) {
          case Success(value) => PongSucceeded(value)
          case Failure(ex)    => PongFailed(ex)
        }
        def handler(counter: Int): Behavior[Message] =
          // counterがゼロになったら終了する
          if (counter == 0)
            Behaviors.stopped
          else {
            Behaviors.receiveMessage {
              case PongFailed(ex) =>
                context.log.error("occurred error", ex)
                Behaviors.stopped
              case PongSucceeded(_) =>
                context.log.info(s"receive: Pong, $counter")
                if (counter - 1 != 0) {
                  context.log.info("send: Ping")
                  // アスク
                  context.ask(toRef, Receiver.Ping) {
                    case Success(value) => PongSucceeded(value)
                    case Failure(ex)    => PongFailed(ex)
                  }
                }
                handler(counter - 1)
            }
          }
        handler(max)
      }
  }

  def main: Behavior[Any] =
    Behaviors.setup[Any] { context =>
      val receiverRef = context.spawn(Receiver(), "receiver")
      context.watch(receiverRef)
      val senderRef = context.spawn(Sender(receiverRef, 10), "sender")
      context.watch(senderRef)
      def handler(refCount: Int): Behavior[Any] = {
        // counterがゼロになったら終了する
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
