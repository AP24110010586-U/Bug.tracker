/* login.html */

const form = document.getElementById('login-form');
const errorBox = document.getElementById('login-error');
const loginButton = document.getElementById('login-btn');

// If the user is already logged in, skip the login page.
api('/api/auth/me').then(function () {
  window.location.href = 'dashboard.html';
}).catch(function () {
  /* not logged in - stay on this page */
});

// Clicking a demo account fills in the form.
document.querySelectorAll('.demo-btn').forEach(function (button) {
  button.addEventListener('click', function () {
    document.getElementById('username').value = button.dataset.user;
    document.getElementById('password').value = button.dataset.pass;
    errorBox.classList.add('d-none');
  });
});

form.addEventListener('submit', async function (event) {
  event.preventDefault();
  errorBox.classList.add('d-none');

  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;

  if (!username || !password) {
    errorBox.textContent = 'Please enter your username and password.';
    errorBox.classList.remove('d-none');
    return;
  }

  loginButton.disabled = true;
  loginButton.textContent = 'Signing in...';

  try {
    await api('/api/auth/login', 'POST', { username: username, password: password });
    window.location.href = 'dashboard.html';
  } catch (error) {
    errorBox.textContent = error.message;
    errorBox.classList.remove('d-none');
    loginButton.disabled = false;
    loginButton.textContent = 'Sign in';
  }
});
