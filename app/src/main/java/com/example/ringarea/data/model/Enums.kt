package com.example.ringarea.data.model

enum class RingCalculationMode(val label: String) {
    CALCULATE("Calculate"),
    FIND_INNER_RADIUS("Find Inner Radius"),
    FIND_OUTER_RADIUS("Find Outer Radius"),
    FIND_RING_WIDTH("Find Ring Width")
}

enum class RadiusInputMode(val label: String) {
    RADIUS("Radius"),
    DIAMETER("Diameter")
}

enum class MeasurementUnit(
    val symbol: String,
    val areaSymbol: String,
    val label: String
) {
    MILLIMETER("mm", "mm²", "Millimeter"),
    CENTIMETER("cm", "cm²", "Centimeter"),
    METER("m", "m²", "Meter"),
    INCH("in", "in²", "Inch"),
    FOOT("ft", "ft²", "Foot")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}
