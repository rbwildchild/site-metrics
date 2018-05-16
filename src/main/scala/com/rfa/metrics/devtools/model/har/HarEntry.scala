package com.rfa.metrics.devtools.model.har

case class Request()

case class Response()

case class Cache()

case class Timings()

case class HarEntry(var pageref: String = "", var startedDateTime: String = "", var time: Int = 0,
                    var request: Request = Request(), var response: Response = Response(), var cache: Cache = Cache(),
                    var timings: Timings = Timings(), var serverIPAddress: String = "", var connection: String = "",
                    var comment: String = "")
