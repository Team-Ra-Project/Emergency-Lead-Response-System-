/*
 * ELRS — Settings API client + page wiring (settings.html).
 */
(function () {
  'use strict';
  var R = window.ELRS.request;
  var BASE = window.ELRS.BASE + '/api/settings';

  var SettingsAPI = {
    get:  function ()   { return R('GET', BASE); },
    save: function (m)  { return R('PUT', BASE, m); }
  };
  window.ELRS.SettingsAPI = SettingsAPI;

  function el(id) { return document.getElementById(id); }
  function setVal(id, v) { var e = el(id); if (e && v != null) e.value = v; }
  function setChk(id, v) { var e = el(id); if (e) e.checked = (v === 'true' || v === true); }
  function boolStr(id) { var e = el(id); return e && e.checked ? 'true' : 'false'; }

  function fill(s) {
    setVal('org-name', s.business_name);
    setVal('org-address', s.business_address);
    setVal('timezone', s.timezone);
    setVal('language', s.language);
    setChk('email-notifications', s.notify_email);
    setChk('sms-notifications', s.notify_sms);
    setChk('desktop-notifications', s.notify_desktop);
    setChk('strong-password', s.security_strong_password);
    setChk('two-factor', s.security_two_factor);
    setVal('session-timeout', s.session_timeout);
    if (s.theme) {
      var radios = document.querySelectorAll('input[name="theme"]');
      var map = { light: 0, dark: 1, system: 2 };
      var idx = map[String(s.theme).toLowerCase()];
      if (idx != null && radios[idx]) radios[idx].checked = true;
    }
  }

  function collect() {
    var theme = 'light';
    var radios = document.querySelectorAll('input[name="theme"]');
    for (var i = 0; i < radios.length; i++) {
      if (radios[i].checked) { theme = ['light', 'dark', 'system'][i] || 'light'; break; }
    }
    return {
      business_name: (el('org-name') || {}).value || '',
      business_address: (el('org-address') || {}).value || '',
      timezone: (el('timezone') || {}).value || '',
      language: (el('language') || {}).value || '',
      notify_email: boolStr('email-notifications'),
      notify_sms: boolStr('sms-notifications'),
      notify_desktop: boolStr('desktop-notifications'),
      security_strong_password: boolStr('strong-password'),
      security_two_factor: boolStr('two-factor'),
      session_timeout: (el('session-timeout') || {}).value || '',
      theme: theme
    };
  }

  function findSaveButton() {
    var btns = document.querySelectorAll('button');
    for (var i = 0; i < btns.length; i++) {
      if (btns[i].textContent.trim().toLowerCase().indexOf('save settings') !== -1) return btns[i];
    }
    return null;
  }

  function wire() {
    if (!el('org-name')) return; // not the settings page yet
    SettingsAPI.get().then(fill).catch(function (e) { window.ELRS.toast('load settings: ' + e.message, true); });
    var save = findSaveButton();
    if (save && !save.dataset.wired) {
      save.dataset.wired = '1';
      save.addEventListener('click', function () {
        SettingsAPI.save(collect())
          .then(function () { alert('Settings saved'); })
          .catch(function (e) { alert(e.message); });
      });
    }
  }

  window.ELRS.ready(wire);
  document.addEventListener('elrs:page-ready', wire);
})();
