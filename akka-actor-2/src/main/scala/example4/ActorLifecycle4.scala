package example4

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, PostStop, Terminated}

object ActorLifecycle4 extends App {

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

  def main: Behavior[Any] = Behaviors.setup { context =>
    val childRef = context.spawn(child, "child")
    context.watch(childRef) // childRefを監視対象にする
    childRef ! "test"
    Thread.sleep(1000) // しばらく待つ
    childRef ! "stop"
    Behaviors.receiveSignal {
      case (context, Terminated(ref)) if ref == childRef => // 子アクターの終了を検知
        context.log.info(s"receiveSignal: childRef has been terminated")
        Behaviors.stopped
    }
  }

  ActorSystem(main, "main")

}
