package io.xtelligent.mibson.mib

import com.google.gson.annotations.SerializedName

class MibTypeIntegerEnumeration(name: String, var description: String?, var symbols: Map<String, Int>) {
    @SerializedName("name")
    var name: String? = name
}