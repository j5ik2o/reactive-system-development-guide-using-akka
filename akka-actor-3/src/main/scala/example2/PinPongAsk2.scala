package example2

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, Terminated }
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.language.postfixOps

object PinPongAsk2 extends App {

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

  def sender(toRef: ActorRef[Receiver.Message], max: Int)(implicit
      system: ActorSystem[_],
      ec: ExecutionContext
  ): Future[Option[Receiver.Pong]] = {
    if (max == 0) {
      Future.successful(None)
    } else {
      implicit val timeout = Timeout(3 seconds)
      // アスク
      val future =
        toRef
          .ask[PinPongAsk2.Receiver.Pong](ref => Receiver.Ping(ref))
          .map(Some(_))
      future.flatMap(_ => sender(toRef, max - 1))
    }
  }

  def main: Behavior[Any] =
    Behaviors.setup[Any] { context =>
      implicit val system = context.system
      implicit val ec     = context.executionContext
      val receiverRef     = context.spawn(Receiver(), "receiver")
      context.watch(receiverRef)
      Await.result(sender(receiverRef, 10), Duration.Inf)
      context.stop(receiverRef)
      def handler(refCount: Int): Behavior[Any] = {
        // counterがゼロになったら終了する
        if (refCount == 0)
          Behaviors.stopped
        else
          Behaviors.receiveSignal {
            case (context, Terminated(ref)) if ref == receiverRef =>
              context.log.info(s"Terminated($ref)")
              handler(refCount - 1)
          }
      }
      handler(1)
    }

  ActorSystem(main, "main")

}
