package example1

import akka.actor.typed.ActorRef

object Protocol {

  final case class Greet(text: String, whom: String, replyTo: ActorRef[GreetReply])

  final case class GreetReply(whom: String, replyTo: ActorRef[Greet])

}
