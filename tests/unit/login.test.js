const test = require('node:test');
const assert = require('node:assert/strict');
const validation = require('../../client/validation.js');
const { initLoginPage } = require('../../client/login.js');

function makeElement(initial = {}) {
  const element = {
    value: initial.value || '',
    hidden: Boolean(initial.hidden),
    disabled: Boolean(initial.disabled),
    textContent: initial.textContent || '',
    attributes: {},
    classList: {
      classes: new Set(),
      add(...names) { names.forEach((name) => this.classes.add(name)); },
      remove(...names) { names.forEach((name) => this.classes.delete(name)); },
      contains(name) { return this.classes.has(name); },
      toggle(name, force) {
        if (force === undefined) {
          if (this.classes.has(name)) {
            this.classes.delete(name);
            return false;
          }
          this.classes.add(name);
          return true;
        }

        if (force) {
          this.classes.add(name);
          return true;
        }

        this.classes.delete(name);
        return false;
      }
    },
    listeners: {},
    focus() {
      this.focused = true;
    },
    setAttribute(name, value) {
      this.attributes[name] = String(value);
    },
    getAttribute(name) {
      return this.attributes[name];
    },
    addEventListener(eventName, handler) {
      this.listeners[eventName] = handler;
    },
    dispatchEvent(event) {
      if (this.listeners[event.type]) {
        this.listeners[event.type](event);
      }
    }
  };

  return element;
}

test('username validation accepts valid ASCII usernames', () => {
  assert.equal(validation.isValidUsername('User12'), true);
  assert.equal(validation.isValidUsername('abcde'), true);
  assert.equal(validation.isValidUsername('abcde12345'), true);
});

test('username validation rejects blanks, spaces, and short values', () => {
  assert.equal(validation.isValidUsername(''), false);
  assert.equal(validation.isValidUsername('user name'), false);
  assert.equal(validation.isValidUsername('abc'), false);
  assert.equal(validation.isValidUsername('user!'), false);
});

test('password validation accepts strong passwords and rejects weak ones', () => {
  assert.equal(validation.isValidPassword('Abcdef1!'), true);
  assert.equal(validation.isValidPassword('Abcdefg1!'), true);
  assert.equal(validation.isValidPassword('abcde1!'), false);
  assert.equal(validation.isValidPassword('ABCDEF1!'), false);
  assert.equal(validation.isValidPassword('Abcdefgh!'), false);
  assert.equal(validation.isValidPassword('Abcdef1 '), false);
});

test('validateUsername and validatePassword return structured errors', () => {
  assert.deepEqual(validation.validateUsername('bad'), { valid: false, field: 'username', message: 'Invalid user. Use 5-20 letters or digits with no spaces.' });
  assert.deepEqual(validation.validatePassword('Weakpass'), { valid: false, field: 'password', message: 'Invalid password. Use at least 8 characters with uppercase, lowercase, a digit, and a non-whitespace special character.' });
});

test('login page initializes and toggles recovery view', () => {
  const loginForm = makeElement();
  const recoveryForm = makeElement();
  const usernameInput = makeElement({ value: 'user12' });
  const passwordInput = makeElement({ value: 'Abcdef1!' });
  const recoveryUsernameInput = makeElement();
  const loginStatus = makeElement();
  const recoveryStatus = makeElement();
  const forgotPasswordButton = makeElement();
  const backToLoginButton = makeElement();
  const loginView = makeElement();
  const recoveryView = makeElement({ hidden: true });

  const fakeDocument = {
    querySelector(selector) {
      const nodes = {
        '#login-form': loginForm,
        '#recovery-form': recoveryForm,
        '#username': usernameInput,
        '#password': passwordInput,
        '#recovery-username': recoveryUsernameInput,
        '#login-status': loginStatus,
        '#recovery-status': recoveryStatus,
        '#forgot-password-link': forgotPasswordButton,
        '#back-to-login': backToLoginButton,
        '#login-view': loginView,
        '#recovery-view': recoveryView
      };

      return nodes[selector] || null;
    }
  };

  global.LoginValidation = validation;
  const instance = initLoginPage(fakeDocument);

  assert.equal(typeof instance.showLoginView, 'function');
  assert.equal(typeof instance.showRecoveryView, 'function');

  forgotPasswordButton.dispatchEvent({ type: 'click', preventDefault() {} });
  assert.equal(recoveryView.hidden, false);
  assert.equal(loginView.hidden, true);

  backToLoginButton.dispatchEvent({ type: 'click', preventDefault() {} });
  assert.equal(loginView.hidden, false);
  assert.equal(recoveryView.hidden, true);
});

test('submit events reject invalid credentials without exposing password content', () => {
  const loginForm = makeElement();
  const recoveryForm = makeElement();
  const usernameInput = makeElement({ value: 'bad' });
  const passwordInput = makeElement({ value: 'short' });
  const recoveryUsernameInput = makeElement();
  const loginStatus = makeElement();
  const recoveryStatus = makeElement();
  const forgotPasswordButton = makeElement();
  const backToLoginButton = makeElement();
  const loginView = makeElement();
  const recoveryView = makeElement({ hidden: true });

  const fakeDocument = {
    querySelector(selector) {
      const nodes = {
        '#login-form': loginForm,
        '#recovery-form': recoveryForm,
        '#username': usernameInput,
        '#password': passwordInput,
        '#recovery-username': recoveryUsernameInput,
        '#login-status': loginStatus,
        '#recovery-status': recoveryStatus,
        '#forgot-password-link': forgotPasswordButton,
        '#back-to-login': backToLoginButton,
        '#login-view': loginView,
        '#recovery-view': recoveryView
      };
      return nodes[selector] || null;
    }
  };

  global.LoginValidation = validation;
  initLoginPage(fakeDocument);

  loginForm.listeners.submit({ preventDefault() {} });

  assert.match(String(loginStatus.textContent), /Invalid user\.|Invalid password\./);
  assert.equal(String(passwordInput.getAttribute('aria-invalid')), 'true');
  assert.equal(loginStatus.textContent.includes('short'), false);
});
