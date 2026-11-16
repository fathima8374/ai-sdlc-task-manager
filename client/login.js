(function () {
  function getElement(rootDocument, selector) {
    if (!rootDocument || typeof rootDocument.querySelector !== 'function') {
      throw new Error('A document-like object with querySelector is required to initialize the page.');
    }

    return rootDocument.querySelector(selector);
  }

  function setErrorState(element, isInvalid) {
    if (!element) {
      return;
    }

    element.setAttribute('aria-invalid', String(Boolean(isInvalid)));
  }

  function setStatus(message, statusRegion, isError) {
    if (!statusRegion) {
      return;
    }

    statusRegion.textContent = message || '';
    statusRegion.setAttribute('aria-live', isError ? 'assertive' : 'polite');
    statusRegion.classList.toggle('error', Boolean(isError));
    statusRegion.classList.toggle('success', !isError && Boolean(message));
  }

  function initLoginPage(rootDocument) {
    const documentRef = rootDocument || document;
    const validationApi = typeof window !== 'undefined' ? window.LoginValidation : globalThis.LoginValidation;
    if (!validationApi) {
      throw new Error('Validation dependency missing.');
    }

    const loginForm = getElement(documentRef, '#login-form');
    const recoveryForm = getElement(documentRef, '#recovery-form');
    const usernameInput = getElement(documentRef, '#username');
    const passwordInput = getElement(documentRef, '#password');
    const recoveryUsernameInput = getElement(documentRef, '#recovery-username');
    const loginStatus = getElement(documentRef, '#login-status');
    const recoveryStatus = getElement(documentRef, '#recovery-status');
    const forgotPasswordButton = getElement(documentRef, '#forgot-password-link');
    const backToLoginButton = getElement(documentRef, '#back-to-login');
    const loginView = getElement(documentRef, '#login-view');
    const recoveryView = getElement(documentRef, '#recovery-view');

    const missing = [
      loginForm,
      recoveryForm,
      usernameInput,
      passwordInput,
      recoveryUsernameInput,
      loginStatus,
      recoveryStatus,
      forgotPasswordButton,
      backToLoginButton,
      loginView,
      recoveryView
    ].filter((node) => !node);

    if (missing.length > 0) {
      throw new Error('Missing required login elements: ' + missing.length);
    }

    function clearSuccessStates() {
      setStatus('', loginStatus, false);
      setStatus('', recoveryStatus, false);
      loginStatus.classList.remove('success');
      recoveryStatus.classList.remove('success');
    }

    function showLoginView() {
      loginView.hidden = false;
      recoveryView.hidden = true;
      usernameInput.focus();
    }

    function showRecoveryView() {
      loginView.hidden = true;
      recoveryView.hidden = false;
      recoveryUsernameInput.focus();
      setErrorState(recoveryUsernameInput, false);
      setStatus('', recoveryStatus, false);
    }

    function validateLogin() {
      const usernameValidation = validationApi.validateUsername(usernameInput.value);
      const passwordValidation = validationApi.validatePassword(passwordInput.value);
      const messages = [];

      if (!usernameValidation.valid) {
        messages.push(usernameValidation.message);
        setErrorState(usernameInput, true);
      } else {
        setErrorState(usernameInput, false);
      }

      if (!passwordValidation.valid) {
        messages.push(passwordValidation.message);
        setErrorState(passwordInput, true);
      } else {
        setErrorState(passwordInput, false);
      }

      return { usernameValidation, passwordValidation, messages };
    }

    function handleLoginSubmit(event) {
      event.preventDefault();
      clearSuccessStates();

      const result = validateLogin();
      if (result.messages.length > 0) {
        setStatus(result.messages.join(' '), loginStatus, true);
        return;
      }

      setStatus('Login successful', loginStatus, false);
      loginStatus.classList.add('success');
    }

    function handleRecoverySubmit(event) {
      event.preventDefault();
      const validation = validationApi.validateUsername(recoveryUsernameInput.value);
      if (!validation.valid) {
        setErrorState(recoveryUsernameInput, true);
        setStatus(validation.message, recoveryStatus, true);
        return;
      }

      setErrorState(recoveryUsernameInput, false);
      setStatus('', recoveryStatus, false);
      recoveryStatus.classList.remove('success');
    }

    loginForm.addEventListener('submit', handleLoginSubmit);
    recoveryForm.addEventListener('submit', handleRecoverySubmit);

    forgotPasswordButton.addEventListener('click', function (event) {
      event.preventDefault();
      showRecoveryView();
    });

    backToLoginButton.addEventListener('click', function (event) {
      event.preventDefault();
      showLoginView();
    });

    [usernameInput, passwordInput].forEach(function (input) {
      input.addEventListener('input', function () {
        if (loginStatus.classList.contains('success')) {
          setStatus('', loginStatus, false);
          loginStatus.classList.remove('success');
        }
      });
    });

    return {
      showLoginView,
      showRecoveryView,
      handleLoginSubmit,
      handleRecoverySubmit
    };
  }

  if (typeof window !== 'undefined') {
    window.addEventListener('DOMContentLoaded', function () {
      if (!window.LoginValidation) {
        const status = document.querySelector('#login-status');
        if (status) {
          setStatus('Validation dependency missing.', status, true);
        }
        return;
      }

      try {
        initLoginPage(document);
      } catch (error) {
        const status = document.querySelector('#login-status');
        if (status) {
          setStatus(error.message, status, true);
        }
      }
    });
  }

  if (typeof module !== 'undefined' && module.exports) {
    module.exports = { initLoginPage, setErrorState, setStatus };
  }
})();
