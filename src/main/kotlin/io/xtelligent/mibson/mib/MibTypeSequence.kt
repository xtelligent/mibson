package io.xtelligent.mibson.mib

import com.google.gson.annotations.SerializedName

class MibTypeSequence {
    constructor(name: String, description: String?, elements: Map<String, String>) {
        this.elements = elements
        this.description = description
        this.name = name
    }

    @SerializedName("name")
    var name: String = ""

    @SerializedName("description")
    var description: String? = ""

    @SerializedName("elements")
    var elements: Map<String, String> = HashMap()
}