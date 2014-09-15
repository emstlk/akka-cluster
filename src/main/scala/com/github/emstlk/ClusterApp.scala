package com.github.emstlk

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.github.emstlk.UserManager.UpdateUserCoins

import scala.util.Random

object ClusterApp {

  def main(args: Array[String]): Unit = {
    if (args.isEmpty || args.length != 2) {
      println("usage: ./sbt \"run <seed_address>[:port] <own_address>[:port]\"")
    } else {
      val (seedAddr, seedPort) = parseAddr(args(0))
      val (ownAddr, ownPort) = parseAddr(args(1))

      val config = ConfigFactory.parseString( s"""
      akka.remote.netty.tcp.hostname="$ownAddr"
      akka.remote.netty.tcp.port="$ownPort"
      akka.cluster.seed-nodes=["akka.tcp://ClusterApp@$seedAddr:$seedPort"]
      """).withFallback(ConfigFactory.load)
      val system = ActorSystem("ClusterApp", config)

      val userManager = system.actorOf(UserManager.props(), "userManager")

      if (seedAddr != ownAddr || seedPort != ownPort) {
        Thread.sleep(3000)

        1 to 5 foreach { uid =>
          userManager ! UpdateUserCoins(uid, Random.nextInt(100))
          Thread.sleep(200)
        }
      }
    }
  }

  def parseAddr(addr: String) = {
    val splitted = addr.split(':')
    if (splitted.length == 2) (splitted(0), splitted(1))
    else (splitted(0), "2552")
  }
}
