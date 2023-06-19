package io.xtelligent.mibson.constraints

import com.google.gson.annotations.SerializedName

open class SizeConstraint : Constraint {
    constructor(lower: String, upper: String) {
        this.lower = lower
        this.upper = upper
    }

    @SerializedName("lower")
    var lower: String = "0"

    @SerializedName("upper")
    var upper: String = "0"
}