package io.xtelligent.mibson.log

import com.google.gson.annotations.SerializedName

class LogEntry(
    @SerializedName("type")
    var type: String = "",
    @SerializedName("message")
    var message: String = "",
    @SerializedName("file")
    var file: String = "",
    @SerializedName("line_number")
    var lineNumber: Int = 0,
    @SerializedName("line")
    var line: String
)