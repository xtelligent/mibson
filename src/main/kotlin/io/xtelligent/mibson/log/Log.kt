package io.xtelligent.mibson.log

import com.google.gson.annotations.SerializedName
import java.util.*

class Log {
    @SerializedName("warnings")
    var warnings: LinkedList<LogEntry> = LinkedList()

    @SerializedName("errors")
    var errors: LinkedList<LogEntry> = LinkedList()

    @SerializedName("internal_errors")
    var internalErrors: LinkedList<LogEntry> = LinkedList()
}