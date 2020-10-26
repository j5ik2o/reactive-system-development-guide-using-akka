package example5

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ReduceActor {
  case class ReduceInput(key: String,
                         entries: List[(String, Int)],
                         replyTo: ActorRef[ReduceOutput])
  case class ReduceOutput(key: String, value: Int)

  def apply(): Behavior[ReduceInput] = Behaviors.setup { context =>
    Behaviors.receiveMessage[ReduceInput] { message =>
      val key = message.key
      val value = message.entries.foldLeft(0)(_ + _._2)
      context.log.info(s"reduce: $key, $value")
      message.replyTo ! ReduceOutput(key, value)
      Behaviors.same
    }

  }

}
