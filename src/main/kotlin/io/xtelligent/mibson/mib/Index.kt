package io.xtelligent.mibson.mib

import com.google.gson.annotations.SerializedName

class Index {
    @SerializedName("name")
    var name: String = ""

    @SerializedName("oid")
    var oid: String = ""

    @SerializedName("description")
    var description: String = ""

    @SerializedName("constraint")
    var constraint: ConstraintInfo = ConstraintInfo()
}