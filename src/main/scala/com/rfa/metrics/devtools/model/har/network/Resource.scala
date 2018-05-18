package com.rfa.metrics.devtools.model.har.network

case class Timing(requestTime: Double, proxyStart: Double, proxyEnd: Double, dnsStart: Double, dnsEnd: Double,
                  connectStart: Double, connectEnd: Double, sslStart: Double, sslEnd: Double,
                  workerStart: Double, workerEnd: Double, sendStart: Double, sendEnd: Double,
                  pushStart: Double, pushEnd: Double, receiveHeadersEnd: Double)

case class RequestWillBeSent(wallTime: Double, timestamp: Double, request: Map[String,Any])
case class ResponseReceived(timestamp: Double, response: Map[String,Map[String,Any]])

case class Resource(pageId: Int = 0, var `type`: String = "", var requestWillBeSent: RequestWillBeSent = null, var responseReceived: ResponseReceived = null)
