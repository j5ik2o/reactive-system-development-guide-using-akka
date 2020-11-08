package example5

import akka.actor.typed.{ ActorRef, ActorSystem }

object Main5 extends App {
  val system = ActorSystem(Printer(), "fire-and-forget-sample")

  // note how the system is also the top level actor ref
  val printer: ActorRef[Printer.Message] = system

  // these are all fire and forget
  printer ! Printer.PrintMe("message 1")
  printer ! Printer.PrintMe("not message 2")
  printer ! Printer.Stop
}
