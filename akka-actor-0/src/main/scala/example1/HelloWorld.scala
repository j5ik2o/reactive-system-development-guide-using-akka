package example1

import akka.actor.typed.{ ActorRef, ActorSystem, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object HelloWorld extends App {

  def apply(): Behavior[String] =
    Behaviors.receive[String] { (context: ActorContext[String], message: String) =>
      context.log.info(s"Hello, $message!")
      Behaviors.same
    }

  val ref: ActorRef[String] = ActorSystem(apply(), "main")

  ref ! "World"

}
