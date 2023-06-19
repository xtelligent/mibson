package io.xtelligent.mibson.constraints

import com.google.gson.annotations.SerializedName

class ReferenceToMibTypeIntegerConstraint : Constraint {
    constructor(name: String) {
        this.name = name
    }

    @SerializedName("name")
    var name: String = ""
}
