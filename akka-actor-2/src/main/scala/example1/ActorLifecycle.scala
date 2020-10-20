package example1

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

object ActorLifecycle extends App {

  def child: Behavior[String] =
    Behaviors
      .receiveMessage[String] {
        case "stop" =>
          println(s"msg = stop")
          Behaviors.stopped
        case msg =>
          println(s"msg = $msg")
          Behaviors.same
      }

  def main: Behavior[Any] = Behaviors.setup { context =>
    val childRef = context.spawn(child, "child")
    childRef ! "test"
    Thread.sleep(1000) // しばらく待つ
    childRef ! "stop"
    Behaviors.same
  }

  ActorSystem(main, "main")

}
