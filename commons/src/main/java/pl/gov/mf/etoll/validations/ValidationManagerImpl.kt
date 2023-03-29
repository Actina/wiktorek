package pl.gov.mf.etoll.validations

class ValidationManagerImpl : ValidationManager {

    override fun areEqual(first: String, second: String): Boolean = first == second

    override fun minLength(value: String, minLength: Int): Boolean = value.length >= minLength

    override fun containsDigit(value: String): Boolean = value.toCharArray().any { it.isDigit() }

    override fun containsLetter(value: String): Boolean = value.toCharArray().any { it.isLetter() }
}