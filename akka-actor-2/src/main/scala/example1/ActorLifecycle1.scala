package example1

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, Behavior }

object ActorLifecycle1 extends App {

  def child: Behavior[String] =
    Behaviors.setup { context =>
      Behaviors
        .receiveMessage[String] {
          case "stop" =>
            context.log.info(s"msg = stop")
            Behaviors.stopped
          case msg =>
            context.log.info(s"msg = $msg")
            Behaviors.same
        }
    }

  def main: Behavior[Any] =
    Behaviors.setup { context =>
      val childRef = context.spawn(child, "child")
      childRef ! "test"
      Thread.sleep(1000) // しばらく待つ
      childRef ! "stop"
      Behaviors.stopped
    }

  ActorSystem(main, "main")

}
