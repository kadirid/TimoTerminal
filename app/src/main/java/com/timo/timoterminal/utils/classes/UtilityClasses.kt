package com.timo.timoterminal.utils.classes

import org.json.JSONArray
import org.json.JSONObject

data class ResponseToJSON(
    val obj : JSONObject?,
    val array : JSONArray?,
    val string: String?
)