package io.xtelligent.mibson

import com.google.gson.GsonBuilder
import io.xtelligent.mibson.mib.*
import io.xtelligent.mibson.mib.MibSymbol
import net.percederberg.mibble.*
import net.percederberg.mibble.snmp.SnmpIndex
import net.percederberg.mibble.snmp.SnmpObjectType
import net.percederberg.mibble.type.*
import net.percederberg.mibble.value.ObjectIdentifierValue
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter
import java.nio.file.Paths
import java.util.*

class Mibson<Index> {

    fun parseMibFiles(mibFiles: LinkedList<File>) {
        for (mibFile in mibFiles) {
            parseMibFile(mibFile)
        }
    }

    fun parseMibFile(mibFile: File) {
        try {
            val loader = MibLoader()
            val loadedFile = loadMib(loader, mibFile)
            val mibSymbols: MutableList<net.percederberg.mibble.MibSymbol>? = loadedFile?.allSymbols
            if (mibSymbols != null) {
                val mibTypeIntegerEnumerationList = LinkedList<MibTypeIntegerEnumeration>()
                val mibTypeSequenceList = LinkedList<MibTypeSequence>()
                for (mibSymbol in loadedFile.allSymbols) {
                    getMibTypeLists(loadedFile, mibSymbol, mibTypeIntegerEnumerationList, mibTypeSequenceList)
                }
                var mibTreeRoot: net.percederberg.mibble.MibSymbol = loadedFile.rootSymbol
                if (mibTreeRoot.name == "null") {
                    for (mibSymbol in loadedFile.allSymbols) {
                        if (mibSymbol.name != "null") {
                            mibTreeRoot = mibSymbol
                            break
                        }
                    }
                }

                val mibTree = mibSymbolToMibsonSymbol(loadedFile, mibTreeRoot) as MibSymbol
                val nameToOidLookup: HashMap<String, String> = HashMap()
                val oidToNameLookup: HashMap<String, String> = HashMap()
                walkMib(
                    loadedFile,
                    mibTreeRoot,
                    mibTree,
                    mibTypeIntegerEnumerationList,
                    mibTypeSequenceList,
                    nameToOidLookup,
                    oidToNameLookup
                )

                val flatMibTree: LinkedList<MibSymbol> = LinkedList()
                for (mibSymbol in loadedFile.allSymbols) {
                    when (mibSymbol) {
                        is MibValueSymbol -> {
                            flatMibTree.add(
                                mibSymbolToMibsonSymbol(
                                    loadedFile,
                                    mibSymbol
                                ) as MibSymbol
                            )
                        }
                    }
                }

                val obj = MibFile()
                loadedFile.headerComment?.let {
                    obj.headerComment = loadedFile.headerComment
                }
                loadedFile.footerComment?.let {
                    obj.footerComment = loadedFile.footerComment
                }
                obj.smiVersion = loadedFile.smiVersion.toString()
                obj.mibTree = mibTree
                obj.flatMibTree = flatMibTree
                obj.oidToNameMap = nameToOidLookup
                obj.nameToOidMap = oidToNameLookup
                obj.mibTypeIntegerEnumeration = mibTypeIntegerEnumerationList
                obj.mibTypeSequence = mibTypeSequenceList
                obj.log = logEntriesToMibsonLog(loadedFile.log)
                writeOutputFile(mibFile, obj)
            }
        } catch (e: MibLoaderException) {
            val obj = MibFile()
            obj.log = logEntriesToMibsonLog(e.log)
            writeOutputFile(mibFile, obj)
        } catch (e: Exception) {
            println("failed")
            println(e.message)
        }
    }

    private fun writeOutputFile(mibFile: File, obj: MibFile) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val strRep = gson.toJson(obj)
        println(strRep)
        val rootPath = Paths.get(mibFile.parentFile.absolutePath)
        val partialPath = Paths.get("output/" + mibFile.nameWithoutExtension + ".json")
        val resolvedPath = rootPath.resolve(partialPath)
        val writer = PrintWriter(resolvedPath.toString())
        writer.write(strRep)
        writer.close()
    }

    private fun logEntriesToMibsonLog(log: MibLoaderLog): io.xtelligent.mibson.log.Log {
        val mibsonLog = io.xtelligent.mibson.log.Log()
        for (logEntry in log.entries()) {
            val entryTypeMap = mapOf(3 to "warning", 1 to "internal_error", 2 to "error")
            val entryType = entryTypeMap.get(logEntry.type)
            var messageType = ""
            if (entryType != null) {
                messageType = entryType
            }
            logEntry.lineNumber
            val newEntry = io.xtelligent.mibson.log.LogEntry(
                messageType,
                logEntry.message,
                logEntry.file.toString(),
                logEntry.lineNumber,
                logEntry.readLine()
            )
            when (logEntry.type) {
                3 -> {
                    mibsonLog.warnings.add(newEntry)
                }

                1 -> {
                    mibsonLog.internalErrors.add(newEntry)
                }

                2 -> {
                    mibsonLog.errors.add(newEntry)
                }
            }

        }
        return mibsonLog
    }

    @Throws(FileNotFoundException::class, MibLoaderException::class)
    fun loadMib(loader: MibLoader, file: File): Mib? {
        // The MIB file may import other MIBs (often in same dir)
        loader.addDir(file.parentFile)
        // Once initialized, MIB loading is straight-forward
        return loader.load(file)
    }

    private fun getConstraint(
        constraint: Constraint?,
        mibType: net.percederberg.mibble.MibType
    ): io.xtelligent.mibson.constraints.Constraint {
        if (constraint == null) {
            return io.xtelligent.mibson.constraints.Constraint()
        } else {
            when (constraint) {
                is ValueRangeConstraint -> {
                    val lower = constraint.lowerBound.toString()
                    val upper = constraint.upperBound.toString()
                    return io.xtelligent.mibson.constraints.SizeConstraint(lower, upper)
                }

                is CompoundConstraint -> {
                    if (mibType.referenceSymbol == null) {
                        when (mibType) {
                            is IntegerType -> {
                                val symbols = HashMap<String, Int>()
                                for (s in mibType.allSymbols) {
                                    symbols[s.name] = s.value.toString().toInt()
                                }
                                symbols.toList().sortedBy { (_, value: Int) -> value }.toMap()
                                return io.xtelligent.mibson.constraints.EnumerationConstraint(symbols)
                            }

                            else -> {
                                throw Exception()
                            }
                        }
                    } else {
                        when (mibType.referenceSymbol) {
                            is MibTypeSymbol -> {
                                return io.xtelligent.mibson.constraints.ReferenceToMibTypeIntegerConstraint(mibType.referenceSymbol.name.toString())
                            }
                        }
                    }

                }

                is SizeConstraint -> {
                    for (v in constraint.values) {
                        when (v) {
                            is ValueRangeConstraint -> {
                                val lower = v.lowerBound.toString()
                                val upper = v.upperBound.toString()
                                return io.xtelligent.mibson.constraints.SizeConstraint(lower, upper)
                            }

                            is ValueConstraint -> {
                                val lower = v.value.toString()
                                val upper = v.value.toString()
                                return io.xtelligent.mibson.constraints.SizeConstraint(lower, upper)
                            }

                            else -> {
                                throw Exception()
                            }
                        }
                    }

                }

                is ValueConstraint -> {
                    val lower = constraint.value.toString()
                    val upper = constraint.value.toString()
                    return io.xtelligent.mibson.constraints.SizeConstraint(lower, upper)
                }

                else -> {
                    throw Exception("index with other type of constraint")
                }
            }
        }
        throw Exception("Unknown constraint")
    }

    private fun getConstraintInfo(constraint: io.xtelligent.mibson.constraints.Constraint): ConstraintInfo {
        val constraintInfo = ConstraintInfo()
        when (constraint) {
            is io.xtelligent.mibson.constraints.SizeConstraint -> {
                constraintInfo.constraintType = "size_constraint"
                constraintInfo.sizeConstraint = constraint
            }

            is io.xtelligent.mibson.constraints.EnumerationConstraint -> {
                constraintInfo.constraintType = "enumeration_constraint"
                constraintInfo.enumerationConstraint = constraint
            }

            is io.xtelligent.mibson.constraints.ReferenceToMibTypeIntegerConstraint -> {
                constraintInfo.constraintType = "reference_to_mib_type_integer_constraint"
                constraintInfo.referenceToMibTypeIntegerConstraint = constraint
            }

            else -> {
                constraintInfo.constraintType = "constraint"
                constraintInfo.baseConstraint = constraint
            }
        }
        return constraintInfo
    }

    private fun parseIndex(idx: SnmpIndex): io.xtelligent.mibson.mib.Index {
        val name = idx.value.name
        val oid = idx.value.toString()
        when (idx.value) {
            is ObjectIdentifierValue -> {
                when ((idx.value as ObjectIdentifierValue).symbol) {
                    is MibValueSymbol -> {
                        when ((idx.value as ObjectIdentifierValue).symbol.type) {
                            is SnmpObjectType -> {
                                val description =
                                    ((idx.value as ObjectIdentifierValue).symbol.type as SnmpObjectType).description
                                        ?: ""
                                val indexSyntax =
                                    (((idx.value as ObjectIdentifierValue).symbol as MibValueSymbol).type as SnmpObjectType).syntax
                                when (indexSyntax) {
                                    is IntegerType -> {
                                        val constraint = getConstraint(indexSyntax.constraint, indexSyntax)
                                        val index: io.xtelligent.mibson.mib.Index = Index()
                                        index.constraint = getConstraintInfo(constraint)
                                        index.name = name
                                        index.oid = oid
                                        index.description = description
                                        return index
                                    }

                                    else -> {
                                        throw Exception()
                                    }
                                }
                            }

                            else -> {
                                throw Exception()
                            }
                        }
                    }

                    else -> {
                        throw Exception()
                    }
                }
            }

            else -> {
                throw Exception()
            }
        }
    }

    private fun mibValueSymbolToMibsonSymbol(symbol: MibValueSymbol): MibSymbol {
        when (symbol.value) {
            is ObjectIdentifierValue -> {
                when (symbol.type) {
                    is SnmpObjectType -> {
                        val jMibSymbol = MibSymbol(
                            symbol.value.name,
                            symbol.value.toString(),
                            (symbol.type as SnmpObjectType).description
                        )
                        jMibSymbol.status = (symbol.type as SnmpObjectType).status.toString()
                        jMibSymbol.access = (symbol.type as SnmpObjectType).access.toString()
                        jMibSymbol.oidNode = (symbol.value as ObjectIdentifierValue).value.toString()
                        jMibSymbol.isTable = symbol.isTable
                        jMibSymbol.isTableColumn = symbol.isTableColumn
                        jMibSymbol.isTableRow = symbol.isTableRow
                        jMibSymbol.isScalar = symbol.isScalar
                        (symbol.type as SnmpObjectType).defaultValue?.let {
                            jMibSymbol.defaultValue =
                                (symbol.type as SnmpObjectType).defaultValue.toString()
                        }


                        val typeSyntax = (symbol.type as SnmpObjectType).syntax
                        jMibSymbol.symbolType = typeSyntax.name
                        when (typeSyntax) {
                            is SequenceType -> {
                                for (idx: SnmpIndex in (symbol.type as SnmpObjectType).index) {
                                    val index: io.xtelligent.mibson.mib.Index = parseIndex(idx)
                                    jMibSymbol.indexes.add(index)
                                }
                            }

                            is SequenceOfType -> {
                                // table
                            }

                            is StringType -> {
                                val jConstraint = getConstraint(typeSyntax.constraint, symbol.type)
                                jMibSymbol.constraint = getConstraintInfo(jConstraint)
                            }

                            is IntegerType -> {
                                val obj: IntegerType = (symbol.type as SnmpObjectType).syntax as IntegerType
                                val jConstraint = getConstraint(typeSyntax.constraint, obj)
                                jMibSymbol.constraint = getConstraintInfo(jConstraint)
                            }

                            is ObjectIdentifierType -> {

                            }

                            else -> {
                                throw Exception("Unknown Syntax")
                            }
                        }
                        return jMibSymbol
                    }

                    is ObjectIdentifierType -> {

                        val newSymbol =
                            MibSymbol(symbol.name, symbol.value.toString(), symbol.comment)
                        newSymbol.oidNode = (symbol.value as ObjectIdentifierValue).value.toString()

                        newSymbol.symbolType = (symbol.type as ObjectIdentifierType).name
                        return newSymbol
                    }

                    else -> {
                        throw Exception()
                    }
                }
            }
        }

        throw Exception("Unknown symbol value")
    }

    private fun mibTypeSymbolToMibsonSymbol(loadedFile: Mib, symbol: MibTypeSymbol): Any {
        when (symbol.type) {
            is IntegerType -> {
                val symbols = HashMap<String, Int>()
                for (s in (symbol.type as IntegerType).allSymbols) {
                    symbols[s.name] = s.value.toString().toInt()
                }
                return (MibTypeIntegerEnumeration(
                    symbol.name,
                    symbol.type.comment,
                    symbols.toList().sortedBy { (_, value: Int) -> value }.toMap()
                ))
            }

            is SequenceType -> {
                val elements = HashMap<String, String>()
                for (element in (symbol.type as SequenceType).allElements) {
                    elements[element.name] =
                        (loadedFile.getSymbol(element.name) as MibValueSymbol).value.toString()
                }
                return (MibTypeSequence(
                    symbol.name,
                    symbol.type.comment,
                    elements.toList().sortedBy { (_, value: String) -> value }.toMap()
                ))
            }

            else -> {
                throw Exception("Unknown MibTypeSymbol")
            }
        }
    }

    private fun mibSymbolToMibsonSymbol(
        loadedFile: Mib,
        symbol: net.percederberg.mibble.MibSymbol
    ): Any {
        return when (symbol) {
            is MibTypeSymbol -> {
                mibTypeSymbolToMibsonSymbol(loadedFile, symbol)
            }

            is MibValueSymbol -> {
                mibValueSymbolToMibsonSymbol(symbol)
            }

            else -> {
                throw Exception("Unknown Mib Symbol")
            }
        }

    }

    private fun walkMib(
        loadedFile: Mib,
        symbol: net.percederberg.mibble.MibSymbol,
        outputMibSymbol: MibSymbol,
        mibTypeIntegerEnumerationList: LinkedList<MibTypeIntegerEnumeration>,
        mibTypeSequenceList: LinkedList<MibTypeSequence>,
        nameToOidLookup: HashMap<String, String>,
        oidToNameLookup: HashMap<String, String>
    ) {
        when (symbol) {
            is MibValueSymbol -> {
                for (child in (symbol.value as ObjectIdentifierValue).allChildren) {
                    val jChild: MibSymbol =
                        mibSymbolToMibsonSymbol(loadedFile, child.symbol) as MibSymbol
                    outputMibSymbol.children.add(jChild)
                    nameToOidLookup[symbol.value.name] = symbol.value.toString()
                    oidToNameLookup[symbol.value.toString()] = symbol.value.name
                    walkMib(
                        loadedFile,
                        child.symbol,
                        jChild,
                        mibTypeIntegerEnumerationList,
                        mibTypeSequenceList,
                        nameToOidLookup,
                        oidToNameLookup
                    )
                }
            }
        }
    }

    private fun getMibTypeLists(
        loadedFile: Mib,
        symbol: net.percederberg.mibble.MibSymbol,
        mibTypeIntegerEnumerationList: LinkedList<MibTypeIntegerEnumeration>,
        mibTypeSequenceList: LinkedList<MibTypeSequence>
    ) {
        when (symbol) {
            is MibTypeSymbol -> {
                when (symbol.type) {
                    is IntegerType -> {
                        mibTypeIntegerEnumerationList.add(
                            mibSymbolToMibsonSymbol(
                                loadedFile,
                                symbol
                            ) as MibTypeIntegerEnumeration
                        )
                    }

                    is SequenceType -> {
                        mibTypeSequenceList.add(
                            mibSymbolToMibsonSymbol(
                                loadedFile,
                                symbol
                            ) as MibTypeSequence
                        )
                    }
                }
            }
        }
    }
}