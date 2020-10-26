package example5

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object MapActor {
  case class MapInput(value: String, replyTo: ActorRef[MapOutput])
  case class MapOutput(entries: List[(String, Int)])

  def apply(): Behavior[MapInput] = Behaviors.setup { context =>
    Behaviors.receiveMessage[MapInput] { message =>
      context.log.info(s"map: $message")
      val values = message.value.split(' ').map(v => v -> 1).toList
      message.replyTo ! MapOutput(values)
      Behaviors.same
    }
  }

}
