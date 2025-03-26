package it.tref.dynamicpricing.aws.lambda.validation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

/**
 * Provides methods for validating objects using Jakarta Bean Validation.
 */
@ApplicationScoped
public class ValidationService {

    private final Validator validator;

    /**
     * Constructs a new ValidationService.
     * <p>
     * Initializes the Validator using the default validation factory.
     * </p>
     */
    public ValidationService() {
        try (ValidatorFactory validationFactory = Validation.buildDefaultValidatorFactory()) {
            this.validator = validationFactory.getValidator();
        }
    }

    /**
     * Validates the given object and returns a set of constraint violations.
     *
     * @param <T>    the type of the object to validate.
     * @param object the object to validate.
     * @return a set of constraint violations; an empty set indicates no validation errors.
     */
    public <T> Set<ConstraintViolation<T>> validate(T object) {
        return validator.validate(object);
    }
}
