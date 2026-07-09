/* ============================================================
   Auth page controllers (login / register / forgot / reset)
   ============================================================ */

// --- Login ---
const loginForm = document.getElementById('loginForm');
if (loginForm) {
  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = loginForm.email.value.trim();
    const password = loginForm.password.value;
    const btn = document.getElementById('submitBtn');

    let ok = true;
    if (!V.email(email))    { showFieldError(loginForm.email, 'Enter a valid email');        ok = false; }
    else showFieldError(loginForm.email, '');
    if (!V.password(password)) { showFieldError(loginForm.password, 'Minimum 8 characters'); ok = false; }
    else showFieldError(loginForm.password, '');
    if (!ok) return;

    btn.disabled = true; btn.textContent = 'Signing in…';
    try {
      await API.post('/api/auth/login', { email, password });
      toast('Welcome back!', 'success');
      goto('/pages/dashboard/dashboard.html');
    } catch (err) {
      toast(err.message || 'Login failed', 'error');
      btn.disabled = false; btn.textContent = 'Sign in';
    }
  });
}

// --- Register ---
const regForm = document.getElementById('registerForm');
if (regForm) {
  regForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fullName = regForm.fullName.value.trim();
    const email    = regForm.email.value.trim();
    const phone    = regForm.phone.value.trim();
    const password = regForm.password.value;
    const role     = regForm.role.value;

    let ok = true;
    if (!V.required(fullName)) { showFieldError(regForm.fullName,'Name required'); ok=false; } else showFieldError(regForm.fullName,'');
    if (!V.email(email))       { showFieldError(regForm.email,'Valid email required'); ok=false; } else showFieldError(regForm.email,'');
    if (!V.phone(phone))       { showFieldError(regForm.phone,'Valid phone required'); ok=false; } else showFieldError(regForm.phone,'');
    if (!V.password(password)) { showFieldError(regForm.password,'Min 8 characters'); ok=false; } else showFieldError(regForm.password,'');
    if (!ok) return;

    try {
      await API.post('/api/auth/register', { fullName, email, phone, password, role });
      toast('Account created. Please log in.', 'success');
      setTimeout(() => goto('/pages/auth/login.html'), 800);
    } catch (err) { toast(err.message || 'Registration failed', 'error'); }
  });
}

// --- Forgot ---
const forgotForm = document.getElementById('forgotForm');
if (forgotForm) {
  forgotForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = forgotForm.email.value.trim();
    if (!V.email(email)) { showFieldError(forgotForm.email,'Valid email required'); return; }
    try {
      const res = await API.post('/api/auth/forgot-password', { email });
      toast(res.message || 'Reset link sent', 'success');
      if (res.devResetLink) {
        document.getElementById('devLink').innerHTML =
          'Dev-only reset link: <a href="' + esc(res.devResetLink) + '">' + esc(res.devResetLink) + '</a>';
      }
    } catch (err) { toast(err.message, 'error'); }
  });
}

// --- Reset ---
const resetForm = document.getElementById('resetForm');
if (resetForm) {
  const token = new URLSearchParams(location.search).get('token') || '';
  document.getElementById('token').value = token;
  resetForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const pw = resetForm.password.value, cf = resetForm.confirm.value;
    if (!V.password(pw)) { showFieldError(resetForm.password,'Min 8 characters'); return; }
    if (pw !== cf)       { showFieldError(resetForm.confirm,'Passwords do not match'); return; }
    try {
      await API.post('/api/auth/reset-password', { token, password: pw });
      toast('Password updated. Please sign in.', 'success');
      setTimeout(() => goto('/pages/auth/login.html'), 800);
    } catch (err) { toast(err.message, 'error'); }
  });
}