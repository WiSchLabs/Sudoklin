package sudoklin.solver

import sudoklin.data.NewShinySudoku
import sudoklin.data.Sudoku
import java.io.File

class SudokuSolver(initialSudoku: Sudoku, val log: Boolean = false) {
    val workingStack = ArrayList<Sudoku>()
    val timestamp: String = (System.currentTimeMillis() / (60 * 1000)).toString()

    init {
        workingStack.add(initialSudoku.clone())
    }

    fun getMissingNumbersForRow(rowIndex: Int): List<Int> {
        val puzzle = workingStack.last().puzzle
        val missingNumbers: MutableList<Int> = mutableListOf<Int>()
        for (number in 1..9) {
            if (!puzzle.fixedNumberIsInRow(rowIndex, number)) {
                missingNumbers.add(number)
            }
        }
        return missingNumbers
    }

    fun getMissingNumbersForColumn(columnIndex: Int): List<Int> {
        val puzzle = workingStack.last().puzzle
        val missingNumbers: MutableList<Int> = mutableListOf<Int>()
        for (number in 1..9) {
            if (!puzzle.fixedNumberIsInColumn(columnIndex, number)) {
                missingNumbers.add(number)
            }
        }
        return missingNumbers
    }

    fun getMissingNumbersForGroup(groupIndex: Int): List<Int> {
        val puzzle = workingStack.last().puzzle
        val missingNumbers: MutableList<Int> = mutableListOf<Int>()
        for (number in 1..9) {
            if (!puzzle.fixedNumberIsInGroup(groupIndex, number)) {
                missingNumbers.add(number)
            }
        }
        return missingNumbers
    }

    fun getCandidatesForCell(rowIndex: Int, columnIndex: Int): Array<Int> {
        val missingNumbersForRow = getMissingNumbersForRow(rowIndex)
        val missingNumbersForColumn = getMissingNumbersForColumn(columnIndex)
        val groupIndex = workingStack.last().puzzle.getGroupIndexForCell(rowIndex, columnIndex)
        val missingNumbersForGroup = getMissingNumbersForGroup(groupIndex)

        return missingNumbersForRow.intersect(missingNumbersForColumn).intersect(missingNumbersForGroup).toTypedArray()
    }

    fun getPossiblePositionsForNumberInRow(number: Int, rowIndex: Int): List<Int> {
        val workingPuzzle = workingStack.last().puzzle
        val row = workingPuzzle.getRow(rowIndex)
        val possiblePositions: MutableList<Int> = mutableListOf<Int>()
        val alreadyInRow = workingPuzzle.fixedNumberIsInRow(rowIndex, number)

        if (log) File("./$timestamp.log").appendText("$number in row $rowIndex: " + row.joinToString() + "   $alreadyInRow")

        if (!alreadyInRow) {
            for (columnIndex in 0..8) {
                if (!workingPuzzle.cellIsFixed(rowIndex, columnIndex)) {
                    val alreadyInColumn = workingPuzzle.fixedNumberIsInColumn(columnIndex, number)

                    val groupIndex = workingPuzzle.getGroupIndexForCell(rowIndex, columnIndex)
                    val alreadyInGoup = workingPuzzle.fixedNumberIsInGroup(groupIndex, number)

                    if (! (alreadyInColumn || alreadyInGoup)) {
                        possiblePositions.add(columnIndex)
                    }
                }
            }
        }

        if (log) File("./$timestamp.log").appendText("   $possiblePositions \r\n")

        return possiblePositions
    }

    fun getPossiblePositionsForNumberInColumn(number: Int, columnIndex: Int): List<Int> {
        val workingPuzzle = workingStack.last().puzzle
        val column = workingPuzzle.getColumn(columnIndex)
        val possiblePositions: MutableList<Int> = mutableListOf<Int>()
        val alreadyInColumn = workingPuzzle.fixedNumberIsInColumn(columnIndex, number)

        if (log) File("./$timestamp.log").appendText("$number in column $columnIndex: " + column.joinToString() + "   $alreadyInColumn")

        if (!alreadyInColumn) {
            for (rowIndex in 0..8) {
                if (!workingPuzzle.cellIsFixed(rowIndex, columnIndex)) {
                    val alreadyInRow = workingPuzzle.fixedNumberIsInRow(rowIndex, number)

                    val groupIndex = workingPuzzle.getGroupIndexForCell(rowIndex, columnIndex)
                    val alreadyInGoup = workingPuzzle.fixedNumberIsInGroup(groupIndex, number)

                    if (! (alreadyInRow || alreadyInGoup)) {
                        possiblePositions.add(rowIndex)
                    }
                }
            }
        }

        if (log) File("./$timestamp.log").appendText("   $possiblePositions \r\n")

        return possiblePositions
    }

    fun getPossiblePositionsForNumberInGroup(number: Int, groupIndex: Int): List<Pair<Int, Int>> {
        val workingPuzzle = workingStack.last().puzzle
        val group = workingPuzzle.getGroup(groupIndex)
        val possiblePositions: MutableList<Pair<Int, Int>> = mutableListOf<Pair<Int, Int>>()
        val alreadyInGoup = workingPuzzle.fixedNumberIsInGroup(groupIndex, number)

        if (log) File("./$timestamp.log").appendText("$number in group $groupIndex: " + group.joinToString() + "   $alreadyInGoup")

        if (!alreadyInGoup) {
            for (rowIndex in 0..8) {
                for (columnIndex in 0..8) {
                    if (workingPuzzle.getGroupIndexForCell(rowIndex, columnIndex) == groupIndex) {
                        if (!workingPuzzle.cellIsFixed(rowIndex, columnIndex)) {
                            val alreadyInRow = workingPuzzle.fixedNumberIsInRow(rowIndex, number)
                            val alreadyInColumn = workingPuzzle.fixedNumberIsInColumn(columnIndex, number)

                            if (! (alreadyInRow || alreadyInColumn)) {
                                possiblePositions.add(Pair(rowIndex, columnIndex))
                            }
                        }
                    }
                }
            }
        }

        if (log) File("./$timestamp.log").appendText("   $possiblePositions \r\n")

        return possiblePositions
    }

    fun solve(): Sudoku {
        var iteration = 0
        if (log) File("./$timestamp.log").appendText("INITIAL" + workingStack.last().puzzle.toString())
        while(! workingStack.last().puzzle.isSolved()) {
            // TODO refactor:
            // use simple method until no more progress,
            // then use next more complicated method,
            // anytime a number is found, start over from step 1 again

//            /*1*/ fillOnlyPossibleValueForCells()
//            if (log) File("./$timestamp.log").appendText("Step $iteration a:" + workingStack.last().puzzle.toString())

            /*2*/ fillNumbersToTheirOnlyPossibleCell()
            if (log) File("./$timestamp.log").appendText("Step $iteration b:" + workingStack.last().puzzle.toString())
//            /*3*/ findPairsInCandidatesAndEliminateOthers()
//            if (log) File("./$timestamp.log").appendText("Step $iteration c:" + workingStack.last().puzzle.toString())
            iteration++
        }
        return workingStack.last()
    }

    private fun fillNumbersToTheirOnlyPossibleCell() {
        for (number in 1..9) {
            for (i in 0..8) {
                val possiblePositionsInRow = getPossiblePositionsForNumberInRow(number, i)
                if (possiblePositionsInRow.size == 1) {
                    workingStack.last().puzzle.matrix[i][possiblePositionsInRow[0]] = IntArray(1, {number})
                }
                if (log) File("./$timestamp.log").appendText(" I: $number in row" + workingStack.last().puzzle.toString())

                val possiblePositionsInColumn = getPossiblePositionsForNumberInColumn(number, i)
                if (possiblePositionsInColumn.size == 1) {
                    workingStack.last().puzzle.matrix[possiblePositionsInColumn[0]][i] = IntArray(1, {number})
                }
                if (log) File("./$timestamp.log").appendText(" II: $number in column" + workingStack.last().puzzle.toString())

                val possiblePositionsInGroup = getPossiblePositionsForNumberInGroup(number, i)
                if (possiblePositionsInGroup.size == 1) {
                    workingStack.last().puzzle.matrix[possiblePositionsInGroup[0].first][possiblePositionsInGroup[0].second] = IntArray(1, {number})
                }
                if (log) File("./$timestamp.log").appendText(" III: $number in group" + workingStack.last().puzzle.toString())
            }
        }
    }
}

class NewSudokuSolver(val sudoku: NewShinySudoku, val log: Boolean = false) {
    val timestamp: String = (System.currentTimeMillis() / (60 * 1000)).toString()

    fun getMissingNumbersForRow(rowIndex: Int): List<Int> {
        val puzzle = sudoku
        val missingNumbers: MutableList<Int> = mutableListOf<Int>()
        for (number in 1..9) {
            if (!puzzle.rows[rowIndex]!!.containsSolvedNumber(number)) {
                missingNumbers.add(number)
            }
        }
        return missingNumbers
    }

    fun getMissingNumbersForColumn(columnIndex: Int): List<Int> {
        val missingNumbers: MutableList<Int> = mutableListOf<Int>()
        for (number in 1..9) {
            if (!sudoku.columns[columnIndex]!!.containsSolvedNumber(number)) {
                missingNumbers.add(number)
            }
        }
        return missingNumbers
    }

    fun getMissingNumbersForGroup(groupIndex: Int): List<Int> {
        val missingNumbers: MutableList<Int> = mutableListOf<Int>()
        for (number in 1..9) {
            if (!sudoku.groups[groupIndex]!!.containsSolvedNumber(number)) {
                missingNumbers.add(number)
            }
        }
        return missingNumbers
    }

    fun getCandidatesForCell(rowIndex: Int, columnIndex: Int): Array<Int> {
        val missingNumbersForRow = getMissingNumbersForRow(rowIndex)
        val missingNumbersForColumn = getMissingNumbersForColumn(columnIndex)
        val groupIndex = sudoku.getCell(rowIndex, columnIndex).groupIndex
        val missingNumbersForGroup = getMissingNumbersForGroup(groupIndex)

        return missingNumbersForRow.intersect(missingNumbersForColumn).intersect(missingNumbersForGroup).toTypedArray()
    }

    fun getPossiblePositionsForNumberInRow(number: Int, rowIndex: Int): List<Int> {
        val row = sudoku.rows[rowIndex]
        val possiblePositions: MutableList<Int> = mutableListOf<Int>()
        val alreadyInRow = sudoku.rows[rowIndex]!!.containsSolvedNumber(number)

        if (log) File("./$timestamp.log").appendText("$number in row $rowIndex: " + row.toString() + "   $alreadyInRow")

        if (!alreadyInRow) {
            for (columnIndex in 0..8) {
                if (!sudoku.getCell(rowIndex, columnIndex).isSolved()) {
                    val alreadyInColumn = sudoku.columns[columnIndex]!!.containsSolvedNumber(number)

                    val groupIndex = sudoku.getCell(rowIndex, columnIndex).groupIndex
                    val alreadyInGoup = sudoku.groups[groupIndex]!!.containsSolvedNumber(number)

                    if (! (alreadyInColumn || alreadyInGoup)) {
                        possiblePositions.add(columnIndex)
                    }
                }
            }
        }

        if (log) File("./$timestamp.log").appendText("   $possiblePositions \r\n")

        return possiblePositions
    }

    fun getPossiblePositionsForNumberInColumn(number: Int, columnIndex: Int): List<Int> {
        val column = sudoku.columns[columnIndex]
        val possiblePositions: MutableList<Int> = mutableListOf<Int>()
        val alreadyInColumn = sudoku.columns[columnIndex]!!.containsSolvedNumber(number)

        if (log) File("./$timestamp.log").appendText("$number in column $columnIndex: " + column.toString() + "   $alreadyInColumn")

        if (!alreadyInColumn) {
            for (rowIndex in 0..8) {
                if (!sudoku.getCell(rowIndex, columnIndex).isSolved()) {
                    val alreadyInRow = sudoku.rows[rowIndex]!!.containsSolvedNumber(number)

                    val groupIndex = sudoku.getCell(rowIndex, columnIndex).groupIndex
                    val alreadyInGoup = sudoku.groups[groupIndex]!!.containsSolvedNumber(number)

                    if (! (alreadyInRow || alreadyInGoup)) {
                        possiblePositions.add(rowIndex)
                    }
                }
            }
        }

        if (log) File("./$timestamp.log").appendText("   $possiblePositions \r\n")

        return possiblePositions
    }

    fun getPossiblePositionsForNumberInGroup(number: Int, groupIndex: Int): List<Pair<Int, Int>> {
        val group = sudoku.groups[groupIndex]
        val possiblePositions: MutableList<Pair<Int, Int>> = mutableListOf<Pair<Int, Int>>()
        val alreadyInGoup = sudoku.groups[groupIndex]!!.containsSolvedNumber(number)

        if (log) File("./$timestamp.log").appendText("$number in group $groupIndex: " + group.toString() + "   $alreadyInGoup")

        if (!alreadyInGoup) {
            for (rowIndex in 0..8) {
                for (columnIndex in 0..8) {
                    if (sudoku.getCell(rowIndex, columnIndex).groupIndex == groupIndex) {
                        if (!sudoku.getCell(rowIndex, columnIndex).isSolved()) {
                            val alreadyInRow = sudoku.rows[rowIndex]!!.containsSolvedNumber(number)
                            val alreadyInColumn = sudoku.columns[columnIndex]!!.containsSolvedNumber(number)

                            if (! (alreadyInRow || alreadyInColumn)) {
                                possiblePositions.add(Pair(rowIndex, columnIndex))
                            }
                        }
                    }
                }
            }
        }

        if (log) File("./$timestamp.log").appendText("   $possiblePositions \r\n")

        return possiblePositions
    }

    fun solve(): NewShinySudoku {
        var iteration = 0
        if (log) File("./$timestamp.log").appendText("INITIAL" + sudoku.toString())
        while(! sudoku.isSolved()) {
            // TODO refactor:
            // use simple method until no more progress,
            // then use next more complicated method,
            // anytime a number is found, start over from step 1 again

//            /*1*/ fillOnlyPossibleValueForCells()
//            if (log) File("./$timestamp.log").appendText("Step $iteration a:" + workingStack.last().puzzle.toString())

            /*2*/ fillNumbersToTheirOnlyPossibleCell()
            if (log) File("./$timestamp.log").appendText("Step $iteration b:" + sudoku.toString())
//            /*3*/ findPairsInCandidatesAndEliminateOthers()
//            if (log) File("./$timestamp.log").appendText("Step $iteration c:" + workingStack.last().puzzle.toString())
            iteration++
        }
        return sudoku
    }

    private fun fillNumbersToTheirOnlyPossibleCell() {
        for (number in 1..9) {
            for (i in 0..8) {
                val possiblePositionsInRow = getPossiblePositionsForNumberInRow(number, i)
                if (possiblePositionsInRow.size == 1) {
                    sudoku.rows[i]!!.cells[possiblePositionsInRow[0]].candidates.clear()
                    sudoku.rows[i]!!.cells[possiblePositionsInRow[0]].candidates.add(number)
                }
                if (log) File("./$timestamp.log").appendText(" I: $number in row" + sudoku.toString())

                val possiblePositionsInColumn = getPossiblePositionsForNumberInColumn(number, i)
                if (possiblePositionsInColumn.size == 1) {
                    sudoku.columns[i]!!.cells[possiblePositionsInColumn[0]].candidates.clear()
                    sudoku.columns[i]!!.cells[possiblePositionsInColumn[0]].candidates.add(number)
                }
                if (log) File("./$timestamp.log").appendText(" II: $number in column" + sudoku.toString())

                val possiblePositionsInGroup = getPossiblePositionsForNumberInGroup(number, i)
                if (possiblePositionsInGroup.size == 1) {
                    sudoku.getCell(possiblePositionsInGroup[0].first,
                                   possiblePositionsInGroup[0].second).candidates.clear()
                    sudoku.getCell(possiblePositionsInGroup[0].first,
                                   possiblePositionsInGroup[0].second).candidates.add(number)
                }
                if (log) File("./$timestamp.log").appendText(" III: $number in group" + sudoku.toString())
            }
        }
    }
}