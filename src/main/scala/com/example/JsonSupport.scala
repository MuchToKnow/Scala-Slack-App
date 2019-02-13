package com.example

import com.example.SampleEloActor.ReportGame

//#json-support
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat2(User)
  implicit val teamJsonFormat = jsonFormat2(Team)

  implicit val reportGameJsonFormat = jsonFormat3(ReportGame)
}
