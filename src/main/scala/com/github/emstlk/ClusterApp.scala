package com.github.emstlk

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.github.emstlk.UserManager.UpdateUserCoins

import scala.util.Random

object ClusterApp {
  def main(args: Array[String]): Unit = {
    val port = if (args.isEmpty) 2555 else args(0).toInt

    val config = ConfigFactory.parseString( s"""
      akka.remote.netty.tcp.port="$port"
      """).withFallback(ConfigFactory.load)
    val system = ActorSystem("app", config)

    val userManager = system.actorOf(UserManager.props(), "userManager")

    if (port != 2555) {
      Thread.sleep(3000)

      1 to 5 foreach { uid =>
        userManager ! UpdateUserCoins(uid, Random.nextInt(100))
        Thread.sleep(200)
      }
    }
  }
}
