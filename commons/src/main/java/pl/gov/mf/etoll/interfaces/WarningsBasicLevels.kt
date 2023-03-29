package pl.gov.mf.etoll.interfaces

import androidx.annotation.Keep

enum class WarningsBasicLevels(val priority: Int) {
    @Keep
    GREEN(1),

    @Keep
    YELLOW(2),

    @Keep
    RED(3),

    @Keep
    UNKNOWN(0);

    companion object {
        const val PRIORITY_GOOD = 1

        fun fromString(name: String): WarningsBasicLevels = when (name) {
            GREEN.getName() -> GREEN
            YELLOW.getName() -> YELLOW
            RED.getName() -> RED
            else -> UNKNOWN
        }
    }

    fun isRed(): Boolean = this == RED

    fun isUnknown(): Boolean = this == UNKNOWN

    fun getName(): String = this.name
}