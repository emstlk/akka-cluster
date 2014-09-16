package com.github

import akka.routing.ConsistentHashingRouter.ConsistentHashable

package object emstlk {

  case class ForwardedMsg[T](key: T, msg: Any) extends ConsistentHashable {
    val consistentHashKey = key
  }

  trait UidHashable extends ConsistentHashable {
    val uid: Long
    def consistentHashKey: Any = uid
  }

}
