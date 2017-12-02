package sudoklin.impex

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import sudoklin.data.Sudoku
import sudoklin.data.SudokuPuzzle
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SudokuFileImporterTest : Spek({
    describe("An importer") {
        val fileImporter = SudokuFileImporter()

        it("should return a Sudoku object") {
            val sudoku = fileImporter.import("src/test/resources/example.sdk")
            assertTrue(sudoku is Sudoku)
        }

        it("should return a Sudoku object with a puzzle") {
            val sudoku = fileImporter.import("src/test/resources/example.sdk")
            assertTrue(sudoku.puzzle is SudokuPuzzle)
        }

        it("should return a Sudoku object with a puzzle with first cell filled correctly") {
            val sudoku = fileImporter.import("src/test/resources/example.sdk")
            assertEquals(2, sudoku.puzzle.getCell(0, 0)[0])
            assertEquals(1, sudoku.puzzle.getCell(0, 0).size)
        }
    }
})
