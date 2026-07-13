/*
 * ELRS — shared REST helper for all module API clients.
 * Exposes window.ELRS.request(method, url, body) -> Promise(resolvedData).
 */
(function () {
  'use strict';
  window.ELRS = window.ELRS || {};
  if (window.ELRS.request) return;

  // Context root, derived the same way as app.js (works at /elrs or /).
  window.ELRS.BASE = (function () {
    var parts = location.pathname.split('/');
    return parts.length > 1 && parts[1] ? '/' + parts[1] : '';
  })();

  window.ELRS.request = function (method, url, body) {
    var opts = {
      method: method,
      headers: { 'Accept': 'application/json' },
      credentials: 'same-origin'
    };
    if (body !== undefined) {
      opts.headers['Content-Type'] = 'application/json';
      opts.body = JSON.stringify(body);
    }
    return fetch(url, opts).then(function (r) {
      return r.json().then(function (j) {
        if (!r.ok || (j && j.success === false)) {
          throw new Error((j && j.message) || ('HTTP ' + r.status));
        }
        return j.data;
      });
    });
  };

  // Small helpers
  window.ELRS.qs = function (name) {
    return new URLSearchParams(window.location.search).get(name);
  };
  // Runs fn now if the DOM is already parsed (scripts are often injected
  // dynamically AFTER DOMContentLoaded on shell pages), else on DOMContentLoaded.
  window.ELRS.ready = function (fn) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', fn);
    } else {
      fn();
    }
  };
  window.ELRS.toast = function (msg, isError) {
    try { console[isError ? 'error' : 'log']('[ELRS] ' + msg); } catch (e) {}
  };
})();
