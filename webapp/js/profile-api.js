/*
 * ELRS — Profile API client + page wiring (profile.html).
 * Loads the logged-in user's profile and wires the action buttons.
 */
(function () {
  'use strict';
  var R = window.ELRS.request;
  var BASE = window.ELRS.BASE + '/api/profile';

  var ProfileAPI = {
    get:            function ()      { return R('GET', BASE); },
    update:         function (p)     { return R('PUT', BASE, p); },
    updatePhoto:    function (url)   { return R('PUT', BASE + '/photo', { avatarUrl: url }); },
    changePassword: function (c, n)  { return R('PUT', BASE + '/password', { currentPassword: c, newPassword: n }); }
  };
  window.ELRS.ProfileAPI = ProfileAPI;

  function setInfo(labelText, value) {
    var items = document.querySelectorAll('.info-item');
    for (var i = 0; i < items.length; i++) {
      var lbl = items[i].querySelector('.info-label');
      var val = items[i].querySelector('.info-value');
      if (lbl && val && lbl.textContent.trim().toLowerCase() === labelText.toLowerCase()) {
        val.textContent = value == null || value === '' ? '—' : value;
        return;
      }
    }
  }

  function render(u) {
    var nameEl = document.querySelector('.profile-name');
    if (nameEl) nameEl.textContent = u.fullName || '';
    var roleEl = document.querySelector('.profile-role');
    if (roleEl) roleEl.textContent = u.designation || u.roleName || '';
    var avatar = document.querySelector('.profile-avatar');
    if (avatar && u.avatarUrl) {
      avatar.innerHTML = '<img src="' + u.avatarUrl + '" alt="Profile photo" ' +
        'style="width:100%;height:100%;border-radius:50%;object-fit:cover" />';
    }
    setInfo('Employee ID', u.employeeCode);
    setInfo('Email', u.email);
    setInfo('Phone Number', u.phone);
    setInfo('Department', u.department);
    setInfo('Role', u.roleName || u.designation);
    setInfo('Date Joined', u.dateJoined);
    setInfo('Account Status', u.status);
    setInfo('Full Name', u.fullName);
  }

  function load() {
    ProfileAPI.get().then(render).catch(function (e) { window.ELRS.toast('load profile: ' + e.message, true); });
  }

  function findButton(text) {
    var btns = document.querySelectorAll('button');
    for (var i = 0; i < btns.length; i++) {
      if (btns[i].textContent.trim().toLowerCase().indexOf(text.toLowerCase()) !== -1) return btns[i];
    }
    return null;
  }

  function wire() {
    if (!document.querySelector('.profile-name')) return; // not the profile page

    var photoBtn = findButton('Edit Photo');
    if (photoBtn) photoBtn.addEventListener('click', function () {
      var url = prompt('Enter image URL for your profile photo:');
      if (!url) return;
      ProfileAPI.updatePhoto(url.trim()).then(load).catch(function (e) { alert(e.message); });
    });

    var editBtn = findButton('Edit Profile');
    if (editBtn) editBtn.addEventListener('click', function () {
      ProfileAPI.get().then(function (u) {
        var name = prompt('Full name:', u.fullName || ''); if (name === null) return;
        var phone = prompt('Phone number:', u.phone || ''); if (phone === null) return;
        var dept = prompt('Department:', u.department || ''); if (dept === null) return;
        var desig = prompt('Designation:', u.designation || ''); if (desig === null) return;
        ProfileAPI.update({ fullName: name.trim(), phone: phone.trim(), department: dept.trim(), designation: desig.trim() })
          .then(load).catch(function (e) { alert(e.message); });
      });
    });

    var pwBtn = findButton('Change Password');
    if (pwBtn) pwBtn.addEventListener('click', function () {
      var cur = prompt('Current password:'); if (!cur) return;
      var next = prompt('New password (min 8 characters):'); if (!next) return;
      var confirmPw = prompt('Confirm new password:'); if (confirmPw === null) return;
      if (next !== confirmPw) { alert('Passwords do not match'); return; }
      ProfileAPI.changePassword(cur, next)
        .then(function () { alert('Password changed successfully'); })
        .catch(function (e) { alert(e.message); });
    });

    load();
  }

  window.ELRS.ready(wire);
  // profile.html renders content asynchronously via mountShell; re-wire then.
  document.addEventListener('elrs:page-ready', wire);
})();
