package example3

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, Behavior, PostStop }

object ActorLifecycle3 extends App {

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
        .receiveSignal {
          case (context, PostStop) =>
            context.log.info(s"receiveSignal: PostStop")
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
