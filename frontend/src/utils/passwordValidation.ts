/**
 * Password validation utility for adapter configurations
 */

export interface PasswordValidationResult {
  isValid: boolean;
  errors: string[];
}

export interface PasswordValidationOptions {
  minLength?: number;
  requireUppercase?: boolean;
  requireLowercase?: boolean;
  requireNumbers?: boolean;
  requireSpecialChars?: boolean;
  allowEmpty?: boolean;
}

/**
 * Validates password based on configurable rules
 */
export function validatePassword(
  password: string,
  options: PasswordValidationOptions = {}
): PasswordValidationResult {
  const {
    minLength = 8,
    requireUppercase = false,
    requireLowercase = false,
    requireNumbers = false,
    requireSpecialChars = false,
    allowEmpty = true,
  } = options;

  const errors: string[] = [];

  // Allow empty passwords if configured (for optional fields)
  if (allowEmpty && (!password || password.trim().length === 0)) {
    return { isValid: true, errors: [] };
  }

  // Check minimum length
  if (password.length < minLength) {
    errors.push(`Password must be at least ${minLength} characters long`);
  }

  // Check for uppercase letters
  if (requireUppercase && !/[A-Z]/.test(password)) {
    errors.push('Password must contain at least one uppercase letter');
  }

  // Check for lowercase letters
  if (requireLowercase && !/[a-z]/.test(password)) {
    errors.push('Password must contain at least one lowercase letter');
  }

  // Check for numbers
  if (requireNumbers && !/\d/.test(password)) {
    errors.push('Password must contain at least one number');
  }

  // Check for special characters
  if (requireSpecialChars && !/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    errors.push('Password must contain at least one special character');
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * Default password validation for adapter configurations
 * Uses relaxed rules suitable for system integrations
 */
export function validateAdapterPassword(password: string): PasswordValidationResult {
  return validatePassword(password, {
    minLength: 6,
    requireUppercase: false,
    requireLowercase: false,
    requireNumbers: false,
    requireSpecialChars: false,
    allowEmpty: true,
  });
}

/**
 * Strict password validation for sensitive systems
 */
export function validateStrictPassword(password: string): PasswordValidationResult {
  return validatePassword(password, {
    minLength: 12,
    requireUppercase: true,
    requireLowercase: true,
    requireNumbers: true,
    requireSpecialChars: true,
    allowEmpty: false,
  });
}