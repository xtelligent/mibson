import io.xtelligent.mibson.Mibson
import io.xtelligent.mibson.MibsonWriter
import java.io.File
import java.nio.file.Path
import kotlin.test.Test

class MibsonTest {
    @Test
    fun parseSampleMibFile() {
        val mibson: Mibson = Mibson()
        val mibFile: File = File("./test/mibs/apple/AIRPORT-BASESTATION-3-MIB.txt")
        val outputPath: Path = Path.of("./test/mibs/apple/output")
        val mibTree = mibson.parseMibFile(mibFile)
        val writer: MibsonWriter = MibsonWriter(mibTree, outputPath)
        writer.writeOutputFile()
    }
}