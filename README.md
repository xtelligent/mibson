# mibson

MIB to JSON

[![Java CI with Gradle](https://github.com/xtelligent/mibson/actions/workflows/gradle.yml/badge.svg)](https://github.com/xtelligent/mibson/actions/workflows/gradle.yml)

### example
```kotlin
val mibson: Mibson = Mibson()
val mibFile: File = File("./test/mibs/apple/AIRPORT-BASESTATION-3-MIB.txt")
val outputPath: Path = Path.of("./test/mibs/apple/output")
val mibTree = mibson.parseMibFile(mibFile)
val writer: MibsonWriter = MibsonWriter(mibTree, outputPath)
writer.writeOutputFile()
```