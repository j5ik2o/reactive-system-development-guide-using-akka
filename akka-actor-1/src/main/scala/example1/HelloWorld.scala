package example1

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

object HelloWorld {
  import Protocol._

  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info(s"${message.text}, ${message.whom}!")
    message.replyTo ! GreetReply(message.whom, context.self)
    Behaviors.same
  }

}
