package pl.gov.mf.etoll.validations

data class ValidationResult(
    val isValid: Boolean = true,
    val validatedRule: ValidatedRule = ValidatedRule.NONE,
)