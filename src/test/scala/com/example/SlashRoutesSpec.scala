package com.example

//#user-routes-spec
//#test-top
import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

//#set-up
class SlashRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with SlashRoutes {

  override val sampleEloActor: ActorRef =
    system.actorOf(SampleEloActor.props, "eloSample")

  lazy val routes = appRoutes

  //#set-up

  //#actual-test
  "SlashRoutes" should {
    "return 200 when hitting /slash/report" in {
      val request = Post(uri = "/slash/report")

      request ~> routes ~> check {
        status should === (StatusCodes.OK)
      }
    }
    //#actual-test
  }
  //#actual-test
  //#set-up
}
//#set-up
//#user-routes-spec
