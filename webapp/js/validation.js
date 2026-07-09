/* Reusable client-side validators */
const V = {
  email: (s) => /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(s || ''),
  phone: (s) => /^[0-9+\-\s]{7,20}$/.test(s || ''),
  password: (s) => (s || '').length >= 8,
  required: (s) => (s || '').trim().length > 0
};

function showFieldError(input, msg) {
  const grp = input.closest('.form-group');
  if (!grp) return;
  let err = grp.querySelector('.error-text');
  if (!err) { err = document.createElement('div'); err.className = 'error-text'; grp.appendChild(err); }
  err.textContent = msg || '';
  input.style.borderColor = msg ? 'var(--st-lost)' : '';
}