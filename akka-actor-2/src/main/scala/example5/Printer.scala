package example5

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Printer {

  case class PrintMe(message: String)

  def apply(): Behavior[PrintMe] =
    Behaviors.receive {
      case (context, PrintMe(message)) =>
        context.log.info(message)
        Behaviors.same
    }

}
