package io.xtelligent.mibson.mib

import com.google.gson.annotations.SerializedName
import java.util.*

open class MibSymbol {
    @SerializedName("name")
    var name: String = ""

    @SerializedName("symbol_type")
    var symbolType: String = ""

    @SerializedName("oid_node")
    var oidNode = ""

    @SerializedName("oid")
    var oid: String = ""

    @SerializedName("access")
    var access: String = ""

    @SerializedName("status")
    var status: String = ""

    @SerializedName("default_value")
    var defaultValue = ""

    @SerializedName("description")
    var description: String? = ""

    @SerializedName("children")
    var children: LinkedList<MibSymbol> = LinkedList()

    @SerializedName("constraint")
    var constraint: ConstraintInfo = ConstraintInfo()

    @SerializedName("indexes")
    var indexes: ArrayList<Index> = ArrayList()

    @SerializedName("is_scalar")
    var isScalar: Boolean = false

    @SerializedName("is_table")
    var isTable: Boolean = false

    @SerializedName("is_table_column")
    var isTableColumn: Boolean = false

    @SerializedName("is_table_row")
    var isTableRow: Boolean = false


    constructor()
    constructor(name: String, oid: String, description: String?) {
        this.name = name
        this.description = description
        this.oid = oid
    }
}