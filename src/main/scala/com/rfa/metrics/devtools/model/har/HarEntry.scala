package com.rfa.metrics.devtools.model.har

import com.rfa.metrics.devtools.model.har.network.RequestWillBeSent

case class Request()

case class Response()

case class Cache()

case class Timings()

case class Aux(var walltime: Long = 0, var referenceTime: Long = 0)

case class HarEntry(var pageref: String = "", var startedDateTime: Long = 0, var time: Int = 0,
                    var request: Request = Request(), var response: Response = Response(), var cache: Cache = Cache(),
                    var timings: Timings = Timings(), var serverIPAddress: String = "", var connection: String = "",
                    var comment: String = "", aux: Aux = Aux())
