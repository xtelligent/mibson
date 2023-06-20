package io.xtelligent.mibson.mib

import com.google.gson.annotations.SerializedName
import io.xtelligent.mibson.log.Log
import java.util.*

class MibFile {
    @SerializedName("file_name")
    var fileName: String = ""

    @SerializedName("header_comment")
    var headerComment: String = ""

    @SerializedName("footer_comment")
    var footerComment: String = ""

    @SerializedName("smi_version")
    var smiVersion: String = ""

    @SerializedName("flat_mib_tree")
    var flatMibTree: LinkedList<MibSymbol> = LinkedList()

    @SerializedName("mib_tree")
    var mibTree: MibSymbol = MibSymbol()

    @SerializedName("oid_to_name_map")
    var oidToNameMap: Map<String, String> = HashMap()

    @SerializedName("name_to_oid_map")
    var nameToOidMap: Map<String, String> = HashMap()

    @SerializedName("mib_type_integer_enumerations")
    var mibTypeIntegerEnumeration = LinkedList<MibTypeIntegerEnumeration>()

    @SerializedName("mib_type_sequences")
    var mibTypeSequence = LinkedList<MibTypeSequence>()

    @SerializedName("log")
    var log: Log = Log()
}