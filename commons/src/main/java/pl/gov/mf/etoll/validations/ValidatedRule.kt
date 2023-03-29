package pl.gov.mf.etoll.validations

enum class ValidatedRule {
    NONE,
    EQUAL,
    NOT_EMPTY,
    MIN_LENGTH,
    CONTAINS_DIGIT,
    CONTAINS_LETTER
}