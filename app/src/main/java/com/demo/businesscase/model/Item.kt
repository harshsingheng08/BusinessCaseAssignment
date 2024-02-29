package com.demo.businesscase.model

data class Item(
    val type: String,
    val content: Content
)

data class Content(
    var image: String? = null,
    var name: String? = null,
    var email: String? = null,
    var title: String? = null,
    var pin: String? = null,
    var lat: String? = null,
    var lng: String? = null,
    var titleData: String? = null,
    var source: String? = null,
    var value: String? = null
)