(function () {
  'use strict';

  var appShell = document.querySelector('.app-shell');
  var sidebar = document.getElementById('sidebar');
  var sidebarToggle = document.querySelector('[data-sidebar-toggle]');
  var sidebarCloseItems = document.querySelectorAll('[data-sidebar-close]');
  var desktopQuery = window.matchMedia('(min-width: 1025px)');
  var mobileQuery = window.matchMedia('(max-width: 768px)');

  if (!appShell || !sidebar || !sidebarToggle) {
    return;
  }

  function isMobile() {
    return mobileQuery.matches;
  }

  function setMobileNav(open) {
    appShell.classList.toggle('is-mobile-nav-open', open);
    sidebarToggle.setAttribute('aria-expanded', String(open));
    sidebarToggle.setAttribute('aria-label', open ? 'Close navigation' : 'Open navigation');
    document.body.style.overflow = open ? 'hidden' : '';
  }

  function toggleDesktopSidebar() {
    // The same navigation button collapses the desktop shell and opens the mobile drawer.
    var nextState = appShell.getAttribute('data-sidebar-state') === 'collapsed' ? 'expanded' : 'collapsed';
    appShell.setAttribute('data-sidebar-state', nextState);
    sidebarToggle.setAttribute('aria-expanded', nextState === 'expanded' ? 'true' : 'false');
  }

  function handleSidebarToggle() {
    if (isMobile()) {
      setMobileNav(!appShell.classList.contains('is-mobile-nav-open'));
      return;
    }

    toggleDesktopSidebar();
  }

  function syncLayoutForViewport() {
    if (isMobile()) {
      setMobileNav(false);
      return;
    }

    document.body.style.overflow = '';
    appShell.classList.remove('is-mobile-nav-open');
    sidebarToggle.setAttribute('aria-expanded', desktopQuery.matches ? 'true' : 'false');
    sidebarToggle.setAttribute('aria-label', 'Toggle navigation');
  }

  sidebarToggle.addEventListener('click', handleSidebarToggle);

  sidebarCloseItems.forEach(function (item) {
    item.addEventListener('click', function () {
      setMobileNav(false);
    });
  });

  document.addEventListener('keydown', function (event) {
    // Escape gives keyboard users a quick way out of the mobile navigation drawer.
    if (event.key === 'Escape' && appShell.classList.contains('is-mobile-nav-open')) {
      setMobileNav(false);
      sidebarToggle.focus();
    }
  });

  sidebar.addEventListener('click', function (event) {
    var link = event.target.closest('a');

    if (link && isMobile()) {
      setMobileNav(false);
    }
  });

  if (typeof mobileQuery.addEventListener === 'function') {
    mobileQuery.addEventListener('change', syncLayoutForViewport);
    desktopQuery.addEventListener('change', syncLayoutForViewport);
  } else {
    mobileQuery.addListener(syncLayoutForViewport);
    desktopQuery.addListener(syncLayoutForViewport);
  }

  syncLayoutForViewport();
})();
