package example1

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, Behavior, Terminated }

object Main extends App {

  def apply(max: Int): Behavior[Any] =
    Behaviors.setup[Any] { context =>
      val controllerRef = context.spawn(Controller(max), "controller")
      context.watch(controllerRef)
      Behaviors.receiveSignal {
        case (context, Terminated(ref)) if ref == controllerRef =>
          context.log.info("The child actor have stopped, then will stop self.")
          Behaviors.stopped
      }
    }

  ActorSystem(apply(10), "main")

}
