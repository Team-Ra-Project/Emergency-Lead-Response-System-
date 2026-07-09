/* ============================================================
   ELRS - Shared JS (API layer + app shell + sidebar behaviour)
   Merged: my API utilities + teammate's sidebar/mobile-drawer UX
   ============================================================ */

/* -------- Context path (works at /elrs or /) -------- */
const CTX = (() => {
  const parts = location.pathname.split('/');
  return parts.length > 1 && parts[1] ? '/' + parts[1] : '';
})();

/* -------- API client (unchanged wire format) -------- */
const API = {
  async request(method, path, body) {
    const opts = { method, credentials: 'same-origin', headers: { 'Accept': 'application/json' } };
    if (body !== undefined) {
      opts.headers['Content-Type'] = 'application/json';
      opts.body = JSON.stringify(body);
    }
    const res = await fetch(CTX + path, opts);
    let data = {};
    try { data = await res.json(); } catch (_) {}
    if (!res.ok || data.success === false) {
      const err = new Error(data.message || ('HTTP ' + res.status));
      err.status = res.status;
      throw err;
    }
    return data.data;
  },
  get:  (p)    => API.request('GET',    p),
  post: (p, b) => API.request('POST',   p, b || {}),
  put:  (p, b) => API.request('PUT',    p, b || {}),
  del:  (p)    => API.request('DELETE', p)
};

/* -------- Toast -------- */
function toast(msg, type) {
  let el = document.querySelector('.toast');
  if (!el) { el = document.createElement('div'); el.className = 'toast'; document.body.appendChild(el); }
  el.className = 'toast ' + (type || '');
  el.textContent = msg;
  requestAnimationFrame(() => el.classList.add('show'));
  clearTimeout(el._t);
  el._t = setTimeout(() => el.classList.remove('show'), 2800);
}

/* -------- HTML escape -------- */
function esc(s) {
  return String(s == null ? '' : s)
    .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
    .replace(/"/g,'&quot;').replace(/'/g,'&#39;');
}

function goto(path) { location.href = CTX + path; }

async function requireAuth() {
  try { return await API.get('/api/auth/me'); }
  catch (e) { goto('/pages/auth/login.html'); throw e; }
}

/* -------- Logout confirmation modal (replaces native confirm()) -------- */
function openLogoutModal() {
  let backdrop = document.getElementById('logoutModalBackdrop');
  if (backdrop) {
    backdrop.classList.add('is-open');
    document.body.style.overflow = 'hidden';
    backdrop.querySelector('#logoutConfirmBtn').focus();
    return;
  }

  backdrop = document.createElement('div');
  backdrop.id = 'logoutModalBackdrop';
  backdrop.className = 'confirm-backdrop';
  backdrop.innerHTML = `
    <div class="confirm-modal" role="alertdialog" aria-modal="true"
         aria-labelledby="logoutModalTitle" aria-describedby="logoutModalMsg" tabindex="-1">
      <div class="confirm-modal__icon" aria-hidden="true">&#8617;</div>
      <h2 class="confirm-modal__title" id="logoutModalTitle">Log out of ELRS?</h2>
      <p class="confirm-modal__message" id="logoutModalMsg">
        You'll need to sign in again to access your dashboard, leads, and reports.
      </p>
      <div class="confirm-modal__actions">
        <button type="button" class="confirm-modal__btn confirm-modal__btn--cancel" id="logoutCancelBtn">Cancel</button>
        <button type="button" class="confirm-modal__btn confirm-modal__btn--danger" id="logoutConfirmBtn">Log out</button>
      </div>
    </div>`;
  document.body.appendChild(backdrop);
  document.body.style.overflow = 'hidden';

  const cancelBtn = backdrop.querySelector('#logoutCancelBtn');
  const confirmBtn = backdrop.querySelector('#logoutConfirmBtn');
  const previouslyFocused = document.activeElement;

  function close() {
    backdrop.classList.remove('is-open');
    document.body.style.overflow = '';
    document.removeEventListener('keydown', onKeydown);
    setTimeout(() => {
      if (backdrop.parentNode) backdrop.remove();
      if (previouslyFocused && typeof previouslyFocused.focus === 'function') previouslyFocused.focus();
    }, 180);
  }

  function onKeydown(e) {
    if (e.key === 'Escape') { close(); return; }
    if (e.key === 'Tab') {
      const focusable = [cancelBtn, confirmBtn];
      const idx = focusable.indexOf(document.activeElement);
      e.preventDefault();
      const next = e.shiftKey
        ? (idx <= 0 ? focusable.length - 1 : idx - 1)
        : (idx === focusable.length - 1 ? 0 : idx + 1);
      focusable[next].focus();
    }
  }

  backdrop.addEventListener('click', (e) => { if (e.target === backdrop) close(); });
  cancelBtn.addEventListener('click', close);
  confirmBtn.addEventListener('click', async () => {
    confirmBtn.disabled = true;
    cancelBtn.disabled = true;
    confirmBtn.textContent = 'Logging out…';
    try { await API.post('/api/auth/logout'); } catch (_) {}
    goto('/pages/auth/login.html');
  });
  document.addEventListener('keydown', onKeydown);

  requestAnimationFrame(() => {
    backdrop.classList.add('is-open');
    confirmBtn.focus();
  });
}

/* -------- Sidebar UX (adapted from teammate app.js) -------- */
function initShellUX() {
  const appShell = document.querySelector('.app-shell');
  const sidebar = document.getElementById('sidebar');
  const sidebarToggle = document.querySelector('[data-sidebar-toggle]');
  if (!appShell || !sidebar || !sidebarToggle) return;

  const desktopQuery = window.matchMedia('(min-width: 1025px)');
  const mobileQuery  = window.matchMedia('(max-width: 768px)');
  const isMobile = () => mobileQuery.matches;

  function setMobileNav(open) {
    appShell.classList.toggle('is-mobile-nav-open', open);
    sidebarToggle.setAttribute('aria-expanded', String(open));
    sidebarToggle.setAttribute('aria-label', open ? 'Close navigation' : 'Open navigation');
    document.body.style.overflow = open ? 'hidden' : '';
  }
  function toggleDesktopSidebar() {
    const next = appShell.getAttribute('data-sidebar-state') === 'collapsed' ? 'expanded' : 'collapsed';
    appShell.setAttribute('data-sidebar-state', next);
    sidebarToggle.setAttribute('aria-expanded', next === 'expanded' ? 'true' : 'false');
  }
  function handleToggle() {
    if (isMobile()) setMobileNav(!appShell.classList.contains('is-mobile-nav-open'));
    else toggleDesktopSidebar();
  }
  function syncViewport() {
    if (isMobile()) { setMobileNav(false); return; }
    document.body.style.overflow = '';
    appShell.classList.remove('is-mobile-nav-open');
    sidebarToggle.setAttribute('aria-expanded', desktopQuery.matches ? 'true' : 'false');
    sidebarToggle.setAttribute('aria-label', 'Toggle navigation');
  }

  sidebarToggle.addEventListener('click', handleToggle);
  document.querySelectorAll('[data-sidebar-close]').forEach(el =>
    el.addEventListener('click', () => setMobileNav(false)));
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && appShell.classList.contains('is-mobile-nav-open')) {
      setMobileNav(false); sidebarToggle.focus();
    }
  });
  sidebar.addEventListener('click', (e) => {
    const link = e.target.closest('a');
    if (link && isMobile()) setMobileNav(false);
  });
  const add = mobileQuery.addEventListener ? 'addEventListener' : 'addListener';
  mobileQuery[add]('change', syncViewport);
  desktopQuery[add]('change', syncViewport);
  syncViewport();
}

/* -------- Mount app shell (sidebar + topbar) -------- */
async function mountShell(activeKey) {
  const me = await requireAuth();

  const nav = [
    { key: 'dashboard', label: 'Dashboard',     href: '/pages/dashboard/dashboard.html',           icon: '📊' },
    { key: 'leads',     label: 'Leads',         href: '/pages/leads/lead-management.html',         icon: '📥' },
    { key: 'customers', label: 'Customers',     href: '/pages/customers/customer-management.html', icon: '🧑' },
    { key: 'appointments', label: 'Appointments', href: '/pages/appointments/appointment.html',    icon: '📅' },
    { key: 'services',  label: 'Services',      href: '/pages/services/services.html',             icon: '🛠️' },
    { key: 'staff',     label: 'Staff',         href: '/pages/staff/staff-list.html',              icon: '👥', roles: ['ADMIN','BUSINESS_OWNER'] },
    { key: 'reports',   label: 'Reports',       href: '/pages/reports/reports.html',               icon: '📈' },
    { key: 'notif',     label: 'Notifications', href: '/pages/notifications/notifications.html',   icon: '🔔' },
    { key: 'activity',  label: 'Activity Logs', href: '/pages/activity/activity-logs.html',        icon: '📜', roles: ['ADMIN','BUSINESS_OWNER'] },
    { key: 'profile',   label: 'Profile',       href: '/pages/profile/profile.html',               icon: '👤' },
    { key: 'settings',  label: 'Settings',      href: '/pages/settings/settings.html',             icon: '⚙️' }
  ];

  const items = nav
    .filter(n => !n.roles || n.roles.includes(me.role))
    .map(n => `
      <li>
        <a href="${CTX}${n.href}" class="nav-link ${n.key===activeKey?'nav-link--active':''}">
          <span class="nav-link__icon" aria-hidden="true">${n.icon}</span>
          <span class="nav-link__label">${esc(n.label)}</span>
        </a>
      </li>`).join('');

  const initial = esc((me.fullName || '?').trim().charAt(0).toUpperCase());

  const shell = document.getElementById('shell');
  shell.className = 'app-shell';
  shell.setAttribute('data-sidebar-state', 'expanded');
  shell.innerHTML = `
    <a class="skip-link" href="#content">Skip to content</a>
    <aside class="sidebar" id="sidebar" aria-label="Primary">
      <div class="sidebar__brand">
        <div class="brand">
          <span class="brand__mark"><img src="${CTX}/images/logo.png" alt="ELRS logo" style="width:100%;height:100%;object-fit:contain;border-radius:6px"/></span>
          <div class="brand__text">
            <span class="brand__name">ELRS</span>
            <span class="brand__subtitle">Emergency Leads</span>
          </div>
        </div>
      </div>
      <nav class="sidebar__nav">
        <ul class="nav-list">${items}</ul>
      </nav>
      <div class="sidebar__footer">
        <ul class="nav-list">
          <li>
            <a href="#" class="nav-link nav-link--logout" id="logoutLink">
              <span class="nav-link__icon" aria-hidden="true">↩</span>
              <span class="nav-link__label">Log out</span>
            </a>
          </li>
        </ul>
      </div>
    </aside>
    <div class="sidebar-overlay" data-sidebar-close></div>
    <main class="app-main">
      <header class="topbar">
        <div class="topbar__inner">
          <button class="icon-button topbar__menu" type="button"
                  data-sidebar-toggle aria-expanded="true" aria-label="Toggle navigation">☰</button>
          <label class="search">
            <span class="search__icon" aria-hidden="true">🔍</span>
            <input type="search" placeholder="Search leads, staff, reports…" aria-label="Search"/>
          </label>
          <div class="topbar__actions">
            <button class="icon-button notification-button" id="notifBell" type="button" aria-label="Notifications">
              🔔<span class="notification-button__dot" id="notifDot" style="display:none"></span>
            </button>
            <button class="profile-menu" id="profileMenu" type="button" title="Log out">
              <span class="profile-menu__avatar" aria-hidden="true"
                    style="display:inline-flex;align-items:center;justify-content:center;color:#fff;font-weight:600">${initial}</span>
              <span class="profile-menu__name">${esc(me.fullName || 'User')}</span>
              <span class="profile-menu__arrow" aria-hidden="true">▾</span>
            </button>
          </div>
        </div>
      </header>
      <section class="content">
        <div class="content__inner" id="content"></div>
      </section>
    </main>`;

  document.getElementById('profileMenu').onclick = () => openLogoutModal();
  document.getElementById('logoutLink').onclick = (e) => {
    e.preventDefault(); openLogoutModal();
  };
  document.getElementById('notifBell').onclick = () => goto('/pages/notifications/notifications.html');

  initShellUX();
  refreshUnread();
  setInterval(refreshUnread, 30000);
  return me;
}

async function refreshUnread() {
  try {
    const { count } = await API.get('/api/notifications/unread-count');
    const dot = document.getElementById('notifDot');
    if (dot) dot.style.display = count > 0 ? 'block' : 'none';
  } catch (_) {}
}