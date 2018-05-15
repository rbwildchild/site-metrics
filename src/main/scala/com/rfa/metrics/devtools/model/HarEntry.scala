package com.rfa.metrics.devtools.model

case class Request()

case class Response()

case class Cache()

case class Timings()

object HarEntry {
  def apply(page: HarPage, events: List[CdpResponse]): HarEntry = {
    return null;
  }
}

case class HarEntry(pageref: String, startedDateTime: String, time: Int, request: Request, response: Response,
                    cache: Cache, timings: Timings, serverIPAddress: String, connection: String, comment: String)
