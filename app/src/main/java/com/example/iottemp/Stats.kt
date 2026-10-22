package com.example.iottemp

import java.util.Locale
import kotlin.math.sqrt

data class Lectura(val temp: Double, val ts: Long)

data class Stats(
    val count: Int,
    val last: Double,
    val min: Double,
    val max: Double,
    val avg: Double,
    val std: Double,
    val over: Int
)

fun computeStats(lecturas: List<Lectura>, umbral: Double): Stats? {
    if (lecturas.isEmpty()) return null
    val t = lecturas.map { it.temp }
    val avg = t.average()
    val std = sqrt(t.sumOf { (it - avg) * (it - avg) } / t.size)
    return Stats(t.size, t.last(), t.min(), t.max(), avg, std, t.count { it > umbral })
}

fun fmt(v: Double): String = String.format(Locale.getDefault(), "%.1f °C", v)
