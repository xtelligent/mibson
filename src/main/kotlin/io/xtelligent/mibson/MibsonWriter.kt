package io.xtelligent.mibson

import com.google.gson.GsonBuilder
import io.xtelligent.mibson.mib.MibFile
import java.io.File
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories

class MibsonWriter(private val mibsonFile: MibFile, private val outputPath: Path) {
    public fun writeOutputFile() {
        outputPath.createDirectories()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val strRep = gson.toJson(mibsonFile)
        val rootPath = outputPath.toAbsolutePath()
        val partialPath = Paths.get(File(mibsonFile.fileName).nameWithoutExtension + ".json")
        val resolvedPath = rootPath.resolve(partialPath)
        val writer = PrintWriter(resolvedPath.toString())
        writer.write(strRep)
        writer.close()
    }
}