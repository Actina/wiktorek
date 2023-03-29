package pl.gov.mf.etoll.validations

interface ValidationManager {

    fun areEqual(first: String, second: String): Boolean

    fun minLength(value: String, minLength: Int): Boolean

    fun containsDigit(value: String): Boolean

    fun containsLetter(value: String): Boolean
}