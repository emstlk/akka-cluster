package com.github.emstlk

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.github.emstlk.UserActor.GetCoins
import com.github.emstlk.UserManager.UpdateUserCoins
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

object ClusterApp {

  def main(args: Array[String]): Unit = {
    if (args.isEmpty || args.length != 2) {
      println("usage: ./sbt \"run <seed_address>[:port] <own_address>[:port]\"")
    } else {
      val (seedAddr, seedPort) = parseAddr(args(0))
      val (ownAddr, ownPort) = parseAddr(args(1))

      val config = ConfigFactory.parseString( s"""
        akka.cluster.seed-nodes=["akka.tcp://backend@$seedAddr:$seedPort"]
        akka.remote.netty.tcp.hostname="$ownAddr"
        akka.remote.netty.tcp.port="$ownPort"
        """).withFallback(ConfigFactory.load)
      val system = ActorSystem("backend", config)

      val bindModule = newBindingModule { module =>
        module.bind[ActorSystem] toSingle system

        val userManager = system.actorOf(UserManager.props(module), "userManager")
        module.bind[ActorRef] idBy UserManager toSingle userManager

        Thread.sleep(2000)

        if (seedAddr != ownAddr || seedPort != ownPort) {
          import akka.pattern.ask

          implicit val t = Timeout(1, SECONDS)

          1 to 5 foreach { uid =>
            (userManager ? UpdateUserCoins(uid, Random.nextInt(100))).mapTo[Long]
            (userManager ? GetCoins(uid)).mapTo[Long].foreach(coins => println(s"user $uid has $coins coins"))
            Thread.sleep(200)
          }
        }
      }

      bindModule.bindings
    }
  }

  def parseAddr(addr: String) = {
    val splitted = addr.split(':')
    if (splitted.length == 2) (splitted(0), splitted(1))
    else (splitted(0), "2552")
  }
}
