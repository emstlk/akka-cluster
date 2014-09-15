package com.github.emstlk

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import akka.routing.FromConfig
import akka.util.Timeout
import com.github.emstlk.UserActor.AddCoins
import scala.concurrent.ExecutionContext.Implicits.global

object UserManager {
  def props(): Props = Props[UserManager]

  case class UpdateUserCoins(uid: Long, coins: Long)

  case class UpdateCoins(uid: Long, coins: Long) extends ConsistentHashable {
    override def consistentHashKey: Any = uid
  }
}

class UserManager extends Actor with ActorLogging {

  import com.github.emstlk.UserManager._
  import akka.pattern.ask

  val managerRouter = context.actorOf(Props.empty.withRouter(FromConfig), "router")

  override def receive = LoggingReceive {
    case UpdateCoins(uid, coins) =>
      val user = context.child("user-" + uid).getOrElse {
//        log.info("Creating actor for user " + uid)
        context.actorOf(UserActor.props(uid), "user-" + uid)
      }
      user forward AddCoins(coins)

    case UpdateUserCoins(uid, coins) =>
      managerRouter.?(UpdateCoins(uid, coins))(Timeout(1000)).mapTo[Long].map { r =>
        log.info("Got result " + r)
      }
  }
}

object UserActor {
  def props(uid: Long): Props = Props(new UserActor(uid))

  case class AddCoins(amount: Long)
}

class UserActor(uid: Long) extends Actor with ActorLogging {

  import com.github.emstlk.UserActor._

  var coins = 0L

  override def receive = LoggingReceive {
    case AddCoins(amount) =>
      coins = coins + amount
      log.info(s"User $uid got $amount coins, total $coins")
      sender ! coins
  }
}
