package io.xtelligent.mibson.mib

import com.google.gson.annotations.SerializedName
import io.xtelligent.mibson.constraints.Constraint
import io.xtelligent.mibson.constraints.EnumerationConstraint
import io.xtelligent.mibson.constraints.ReferenceToMibTypeIntegerConstraint
import io.xtelligent.mibson.constraints.SizeConstraint

class ConstraintInfo {
    @SerializedName("constraint_type")
    var constraintType: String = ""

    @SerializedName("base_constraint")
    var baseConstraint: Constraint? = null

    @SerializedName("size_constraint")
    var sizeConstraint: SizeConstraint? = null

    @SerializedName("enumeration_constraint")
    var enumerationConstraint: EnumerationConstraint? = null

    @SerializedName("reference_to_mib_type_integer_constraint")
    var referenceToMibTypeIntegerConstraint: ReferenceToMibTypeIntegerConstraint? = null
}