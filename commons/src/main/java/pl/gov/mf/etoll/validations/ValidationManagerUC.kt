package pl.gov.mf.etoll.validations

sealed class ValidationManagerUC {

    class ValidatePinsUseCase(private val validationManager: ValidationManager) :
        ValidationManagerUC() {
        private companion object {
            private const val PIN_LENGTH = 4
        }

        fun execute(pin: String, confirmationPin: String): ValidationResult {
            val areEqual = validationManager.areEqual(pin, confirmationPin)
            if (!areEqual)
                return ValidationResult(isValid = false, validatedRule = ValidatedRule.EQUAL)

            val validLength = validationManager.minLength(pin, PIN_LENGTH)
            if (!validLength)
                return ValidationResult(isValid = false, validatedRule = ValidatedRule.MIN_LENGTH)

            return ValidationResult()
        }
    }

    class ValidatePasswordsUseCase(private val validationManager: ValidationManager) :
        ValidationManagerUC() {
        private companion object {
            private const val PASSWORD_MIN_LENGTH = 8
        }

        fun execute(password: String, confirmationPassword: String): ValidationResult {
            val areEqual = validationManager.areEqual(password, confirmationPassword)
            if (!areEqual)
                return ValidationResult(isValid = false, validatedRule = ValidatedRule.EQUAL)

            val validLength = validationManager.minLength(password, PASSWORD_MIN_LENGTH)
            if (!validLength)
                return ValidationResult(isValid = false, validatedRule = ValidatedRule.MIN_LENGTH)

            val containsDigit = validationManager.containsDigit(password)
            if (!containsDigit)
                return ValidationResult(
                    isValid = false,
                    validatedRule = ValidatedRule.CONTAINS_DIGIT
                )

            val containsLetter = validationManager.containsLetter(password)
            if (!containsLetter)
                return ValidationResult(
                    isValid = false,
                    validatedRule = ValidatedRule.CONTAINS_LETTER
                )

            return ValidationResult()
        }
    }
}