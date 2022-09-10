package com.bignerdranch.android.tetris.helpers
//Вспомогательная функция для создания двухмерного байтового массива( игровое поле, тетрономина)
fun array2dOfByte(sizeOuter: Int, sizeInner: Int): Array<ByteArray>
        = Array(sizeOuter) { ByteArray(sizeInner) }