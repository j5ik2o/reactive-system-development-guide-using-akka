package example3

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, SupervisorStrategy }
import example3.CounterMain.Counter.{ GetCount, Increment }

import scala.language.postfixOps

object CounterMain extends App {

  object Counter {
    sealed trait Command
    case class Increment(nr: String)            extends Command
    case class GetCount(replyTo: ActorRef[Int]) extends Command

    def apply(): Behavior[Command] =
      Behaviors
        .supervise(
          Behaviors
            .supervise(counter(0))
            .onFailure[NumberFormatException](SupervisorStrategy.restart)
        )
        .onFailure[IllegalArgumentException](SupervisorStrategy.stop)

    private def counter(count: Int): Behavior[Command] =
      Behaviors.receiveMessage {
        case Increment(nrStr) =>
          val nr = nrStr.toInt // NumberFormatException
          require(nr > 0) // IllegalArgumentException
          counter(count + nr)
        case GetCount(replyTo) =>
          replyTo ! count
          Behaviors.same
      }
  }

  sealed trait Command
  case class WrappedGetCount(nr: Int) extends Command

  def main: Behavior[Command] =
    Behaviors.setup[CounterMain.Command] { context =>
      val ref = context.spawn(Counter(), "counter")
      ref ! Increment("1")
      ref ! GetCount(context.messageAdapter[Int](ref => WrappedGetCount(ref)))
      ref ! Increment("a")
      ref ! Increment("1")
      ref ! GetCount(context.messageAdapter[Int](ref => WrappedGetCount(ref)))
      def running(messageCount: Int): Behavior[Command] =
        Behaviors.receiveMessage {
          case WrappedGetCount(nr) =>
            context.log.info(s"nr = $nr")
            if (messageCount == 0)
              Behaviors.stopped
            else
              running(messageCount - 1)
        }
      running(1)
    }

  ActorSystem(main, "main")

}
