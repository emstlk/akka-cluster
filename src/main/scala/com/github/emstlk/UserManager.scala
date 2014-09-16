package com.github.emstlk

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import akka.routing.FromConfig
import com.escalatesoft.subcut.inject.{BindingId, BindingModule, Injectable}
import com.github.emstlk.UserActor.GetCoins
import com.github.emstlk.UserManager.UpdateUserCoins

object UserManager extends BindingId {
  def props(implicit bm: BindingModule) = Props(new UserManager)

  case class UpdateUserCoins(uid: Long, coins: Long) extends UidHashable
}

class UserManager(implicit bm: BindingModule) extends Actor with ActorLogging {

  val managerRouter = context.actorOf(Props.empty.withRouter(FromConfig), "router")

  override def receive = LoggingReceive {
    case r@ForwardedMsg(uid: Long, msg) =>
      context.child("user-" + uid) getOrElse {
        context.actorOf(UserActor.props(uid), "user-" + uid)
      } forward msg

    case msg: UidHashable =>
//      managerRouter forward ForwardedMsg(msg.uid, msg)
      managerRouter.tell(ForwardedMsg(msg.uid, msg), sender)
  }
}

object UserActor {
  def props(uid: Long)(implicit bm: BindingModule) = Props(new UserActor(uid))

  case class GetCoins(uid: Long) extends UidHashable
}

class UserActor(uid: Long)(implicit val bindingModule: BindingModule)
  extends Actor
  with ActorLogging
  with Injectable {

  var coins = 0L

  override def receive = LoggingReceive {
    case UpdateUserCoins(_, amount) =>
      coins = coins + amount
      log.info(s"User $uid got $amount coins, total $coins")
      sender ! coins

    case _: GetCoins =>
      sender ! coins
  }
}
