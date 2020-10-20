package example6

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

object Main6 extends App {

  case class Request(query: String, replyTo: ActorRef[Response])
  case class Response(result: String)

  object CookieFabric {
    def apply(): Behavior[Request] =
      Behaviors.setup { context =>
        Behaviors.receiveMessage[Request] {
          case r @ Request(query, replyTo) =>
            context.log.info(s"request: $r")
            replyTo ! Response(s"Here are the cookies for [$query]!")
            Behaviors.same
        }
      }
  }

  object Requestor {
    def apply(cookieFabric: ActorRef[Request]): Behavior[Response] =
      Behaviors.setup[Response] { context =>
        cookieFabric ! Request("give me cookies", context.self)
        Behaviors.receiveMessage { message =>
          context.log.info(s"response: $message")
          Behaviors.stopped
        }
      }
  }

  def apply(): Behavior[Any] = Behaviors.setup[Any] { context =>
    val cookieFabric = context.spawn(CookieFabric(), "cookieFabric")
    val requestor = context.spawn(Requestor(cookieFabric), "requestor")
    context.watch(requestor)
    Behaviors.receiveSignal {
      case (_, Terminated(ref)) if ref == requestor =>
        Behaviors.stopped
    }
  }

  ActorSystem(apply(), "main")

}
