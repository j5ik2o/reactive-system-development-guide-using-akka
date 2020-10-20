package example5

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Printer {

  sealed trait Message
  case class PrintMe(message: String) extends Message
  case object Stop extends Message

  def apply(): Behavior[Message] =
    Behaviors.receive {
      case (context, Stop) =>
        Behaviors.stopped
      case (context, PrintMe(message)) =>
        context.log.info(message)
        Behaviors.same
    }

}
