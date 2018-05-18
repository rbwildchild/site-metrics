package com.rfa.metrics.devtools.model.har

case class PageTimings(onContentLoad: Int, onLoad: Int, comment: String)

case class HarPage(startedDateTime: String = "", id: String = "", title: String = "", pageTimings: PageTimings = null, comment: String = "")
