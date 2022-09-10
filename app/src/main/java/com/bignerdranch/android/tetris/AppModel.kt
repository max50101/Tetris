package com.bignerdranch.android.tetris

import com.bignerdranch.android.tetris.constans.FieldConstants
import com.bignerdranch.android.tetris.helpers.array2dOfByte
import android.graphics.Point
import android.widget.Toast
import com.bignerdranch.android.tetris.constans.CellConstants
import com.bignerdranch.android.tetris.models.Figure
import  com.bignerdranch.android.tetris.storage.AppPreferences
// Класс содержащий всю основную логику игры
class AppModel {
    var score: Int = 0 //текущий счёт
    var currentBlock: Figure? = null
    private var preferences: AppPreferences? =null
    var currentState: String = Statuses.AWAITING_START.name

    enum class Statuses {// Константы состоянмя игры
        AWAITING_START, ACTIVE, INACTIVE, OVER
    }

    enum class Motions {  //Константы движений
        LEFT, RIGHT, DOWN, ROTATE
    }
    //Игровое поле
    private var field: Array<ByteArray> = array2dOfByte(
        FieldConstants.ROW_COUNT.value,
        FieldConstants.COLUMN_COUNT.value
    )
    // Читаем файл общего рекорда
    fun setPreferences(preferences: AppPreferences?) {
        this.preferences = preferences
    }
    //Состояние ячейки
    fun getCellStatus(row: Int, column: Int): Byte? {
        return field[row][column]
    }
    //Изменение состояния ячейки
    private fun setCellStatus(row: Int, column: Int, status: Byte?) {
        if (status != null) {
            field[row][column] = status
        }
    }
    //Проверка завершена ли игра
    fun isGameOver(): Boolean {
        return currentState == Statuses.OVER.name
    }
    //Идёт ли игра
    fun isGameActive(): Boolean {
        return currentState == Statuses.ACTIVE.name
    }


    fun isGameAwaitingStart(): Boolean {
        return currentState == Statuses.AWAITING_START.name
    }
    //Изменение рекорда
    private fun boostScore() {
        score += 10
        if (score > preferences?.getHighScore() as Int)
            preferences?.saveHighScore(score)
    }
    //Создание следующей Фигуры
    private fun generateNextBlock() {
        currentBlock = Figure.createBlock()
    }
    //Вспомогательная функция для проверки возможности движения или создания нового блока
    private fun validTranslation(position: Point, shape: Array<ByteArray>): Boolean {
        return if (position.y < 0 || position.x < 0) {
            false
        } else if (position.y + shape.size > FieldConstants.ROW_COUNT.value) {
            false
        } else if (position.x + shape[0].size > FieldConstants.COLUMN_COUNT.value) {
            false
        } else {
            //Цикл проверяет не выходит ли фигура за границы поля
            for (i in shape.indices) {
                for (j in shape[i].indices) {
                    val y = position.y + i
                    val x = position.x + j
                    if (CellConstants.EMPTY.value != shape[i][j] &&
                        CellConstants.EMPTY.value != field[y][x]
                    ) {
                        return false
                    }
                }
            }
            true
        }
    }
    //Проверка возможно ли движение фигуры
    private fun moveValid(position: Point, frameNumber: Int?): Boolean {
        val shape: Array<ByteArray>? = currentBlock?.getShape(frameNumber as Int)
        return validTranslation(position, shape as Array<ByteArray>)
    }

    fun generateField(action: String) {
        if (isGameActive()) {
            resetField()
            var frameNumber: Int? = currentBlock?.frameNumber
            val coordinate: Point? = Point()
            coordinate?.x = currentBlock?.position?.x
            coordinate?.y = currentBlock?.position?.y
            when (action) {
                Motions.LEFT.name -> {
                    coordinate?.x = currentBlock?.position?.x?.minus(1)
                }
                Motions.RIGHT.name -> {
                    coordinate?.x = currentBlock?.position?.x?.plus(1)
                }
                Motions.DOWN.name -> {
                    coordinate?.y = currentBlock?.position?.y?.plus(1)
                }
                Motions.ROTATE.name -> {
                    frameNumber = frameNumber?.plus(1)
                    if (frameNumber != null) {
                        if (frameNumber >= currentBlock?.frameCount as Int) {
                            frameNumber = 0
                        }
                    }
                }
            }
            if (!moveValid(coordinate as Point, frameNumber)) {
                translateBlock(
                    currentBlock?.position as Point,
                    currentBlock?.frameNumber as Int
                )
                if (Motions.DOWN.name == action) {
                    boostScore()
                    persistCellData()
                    assessField()
                    generateNextBlock()
                    if (!blockAdditionPossible()) {
                        currentState = Statuses.OVER.name;
                        currentBlock = null;
                        score=0
                        resetField(false);
                    }
                }
            } else {
                if (frameNumber != null) {
                    translateBlock(coordinate, frameNumber)
                    currentBlock?.setState(frameNumber, coordinate)
                }
            }
        }
    }
    //Обнуление занятых ячеек
    private fun resetField(ephemeralCellsOnly: Boolean = true) {
        for (i in 0 until FieldConstants.ROW_COUNT.value) {
            (0 until FieldConstants.COLUMN_COUNT.value)
                .filter { !ephemeralCellsOnly || field[i][it] ==
                        CellConstants.EPHEMERAL.value } //отбираем только занятые ячейки
                .forEach { field[i][it] = CellConstants.EMPTY.value }// обнуляем их


        }
    }
    //Заполнение ячеек фигурой
    private fun persistCellData() {
        for (i in 0 until field.size) {
            for (j in 0 until field[i].size) {
                var status = getCellStatus(i, j)
                if (status == CellConstants.EPHEMERAL.value) {
                    status = currentBlock?.staticValue
                    setCellStatus(i, j, status)
                }
            }
        }
    }
//Проверяет заполнена ли вся полоска снизу
    private fun assessField() {
        for (i in 0 until field.size) {
            var emptyCells = 0;
            for (j in 0 until field[i].size) {
                val isEmpty = CellConstants.EMPTY.value == getCellStatus(i,j)
                if (isEmpty)
                    emptyCells++
            }
            if (emptyCells == 0)
                shiftRows(i)
        }
    }
    //Двигает на  позицию
    private fun translateBlock(position: Point, frameNumber: Int) {
        synchronized(field) {
            val shape: Array<ByteArray>? = currentBlock?.getShape(frameNumber)
            if (shape != null) {
                for (i in shape.indices) {
                    for (j in 0 until shape[i].size) {
                        val y = position.y + i
                        val x = position.x + j
                        if (CellConstants.EMPTY.value != shape[i][j]) {
                            field[y][x] = shape[i][j]
                        }
                    }
                }
            }
        }
    }
    //Проверяет не занято ли пространство для движения нового блока
    private fun blockAdditionPossible(): Boolean {
        if (!moveValid(currentBlock?.position as Point,
                currentBlock?.frameNumber)) {
            return false
        }
        return true
    }
    //Очищает заполненую строку
    private fun shiftRows(nToRow: Int) {
        if (nToRow > 0) {
            for (j in nToRow - 1 downTo 0) {
                for (m in 0 until field[j].size) {
                    setCellStatus(j + 1, m, getCellStatus(j, m))
                }
            }
        }
        for (j in 0 until field[0].size) {
            setCellStatus(0, j, CellConstants.EMPTY.value)
        }
    }
    //Начало игры
    fun startGame() {
        if (!isGameActive()) {
            currentState = Statuses.ACTIVE.name
            generateNextBlock()
        }
    }
    //Запускает новую игру
    fun restartGame() {
        resetModel()
        score=0;
        startGame()
    }
    //Окончание игры
    fun endGame() {
        score = 0
        currentState = AppModel.Statuses.OVER.name
    }
    //Очистка поля при рестарте игры
    private fun resetModel() {
        score = 0
        resetField(false)
        currentState = Statuses.AWAITING_START.name
    }
}