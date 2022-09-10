package com.bignerdranch.android.tetris.constans
//2 enumа содержащие константы, первый необходим для игрового поля, стандарнтый размер 10х20, но мжет меняь
enum class FieldConstants(val value: Int) {
    COLUMN_COUNT(10), ROW_COUNT(20);
}
//2 кностанты определяющие является ли ячейка поля занятой
enum class CellConstants(val value: Byte) {
    EMPTY(0), EPHEMERAL(1)
}