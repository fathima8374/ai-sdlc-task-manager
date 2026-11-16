(function (global) {
  function normalizeUsername(value) {
    return typeof value === 'string' ? value.trim() : '';
  }

  function isValidUsername(value) {
    const normalized = normalizeUsername(value);
    return /^[A-Za-z0-9]{5,20}$/.test(normalized);
  }

  function isValidPassword(value) {
    if (typeof value !== 'string') {
      return false;
    }

    const password = value;
    if (password.length < 8) {
      return false;
    }

    if (/\s/.test(password)) {
      return false;
    }

    if (!/[A-Z]/.test(password)) {
      return false;
    }

    if (!/[a-z]/.test(password)) {
      return false;
    }

    if (!/\d/.test(password)) {
      return false;
    }

    if (!/[^\p{L}\p{N}\s]/u.test(password)) {
      return false;
    }

    return true;
  }

  function validateUsername(value) {
    if (isValidUsername(value)) {
      return { valid: true, value: normalizeUsername(value) };
    }

    return {
      valid: false,
      field: 'username',
      message: 'Invalid user. Use 5-20 letters or digits with no spaces.'
    };
  }

  function validatePassword(value) {
    if (isValidPassword(value)) {
      return { valid: true, value };
    }

    return {
      valid: false,
      field: 'password',
      message: 'Invalid password. Use at least 8 characters with uppercase, lowercase, a digit, and a non-whitespace special character.'
    };
  }

  const api = {
    normalizeUsername,
    isValidUsername,
    isValidPassword,
    validateUsername,
    validatePassword
  };

  if (typeof module !== 'undefined' && module.exports) {
    module.exports = api;
  }

  global.LoginValidation = api;
})(typeof window !== 'undefined' ? window : globalThis);
