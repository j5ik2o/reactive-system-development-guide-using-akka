package example1

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

object Controller {

  import Protocol._

  def apply(max: Int): Behavior[GreetReply] =
    Behaviors.setup[GreetReply] { context =>
      val helloWorldRef = context.spawn(HelloWorld(), "thread")
      helloWorldRef ! Greet("message: 0", "user-1", context.self)
      processing(greetingCounter = 0, max)(context)
    }

  private def processing(greetingCounter: Int, max: Int)(
    context: ActorContext[GreetReply]
  ): Behavior[GreetReply] = {
    Behaviors.receiveMessage[GreetReply] { message =>
      val n = greetingCounter + 1
      context.log.info(s"receive from ${message.whom}!")
      if (n == max)
        Behaviors.stopped
      else {
        message.replyTo ! Greet(s"message: $n", message.whom, context.self)
        processing(n, max)(context)
      }
    }
  }
}
