package ch.course223.advanced.validation.phonenumber;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null){
      return true;
    }
    return value.matches("^(0|0041|\\+41)\\s?(\\d{2})\\s?(\\d{3})\\s?(\\d{2})\\s?(\\d{2})");
  }
}
