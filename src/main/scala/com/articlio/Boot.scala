package com.articlio

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import util.Properties

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("spray-actor-system")

  //
  // create and start our service actor
  // 
  // need to create an actor per request and use a dispatcher 
  // (http://doc.akka.io/docs/akka/snapshot/scala/dispatchers.html) that
  // balances requests between multiple copies of that actor, otherwise
  // with one actor handling is serialized. Probably the BalancingDispatcher
  // dispatcher
  //
  // See https://github.com/NET-A-PORTER/spray-actor-per-request/blob/master/src/main/scala/com/netaporter/routing/RestRouting.scala
  // as reference implementation
  //
  val service = system.actorOf(Props[MyServiceActor], "api-handler")

  val logger =  system.actorOf(Props[ArticlioLogger], "logger")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server with our service actor as the handler
  // this will be used to receive request from the front-end server
  //IO(Http) ? Http.Bind(service, interface = "localhost", port = Properties.envOrElse("PORT", "8091").toInt)
  IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = sys.env.get("PORT").map(_.toInt).getOrElse(8091))
}
