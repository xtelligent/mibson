package io.xtelligent.mibson.constraints

import com.google.gson.annotations.SerializedName

class EnumerationConstraint : Constraint {
    constructor(symbols: Map<String, Int>) : super(
    ) {
        this.symbols = symbols
    }

    @SerializedName("symbols")
    var symbols: Map<String, Int> = HashMap()
}