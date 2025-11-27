package ru.nnbk.siberianweather

fun getPortStatusOld(str: String, mask: String): Char {
    val index = str.indexOf(mask)
    return if (index >= 3) {
        str[index - 3]
    } else {
        ' '
    }
}

fun getPortStatus(str: String, mask: String): Char {
    val index = str.indexOf(mask)
    if (index == -1) return ' '
    var pos = index - 1
    while (pos >= 0 && str[pos] == ' ') {
        pos--
    }
    return if (pos >= 0) str[pos] else ' '
}