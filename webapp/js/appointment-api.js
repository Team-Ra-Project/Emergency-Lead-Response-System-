/*
 * ELRS — Appointment API client + page wiring.
 * Include AFTER appointment.js (list) or on add-appointment.html.
 */
(function () {
  'use strict';
  var R = window.ELRS.request;
  var BASE = window.ELRS.BASE + '/api/appointments';

  var AppointmentAPI = {
    list:   function (p) {
      var qs = [];
      if (p) {
        if (p.q)      qs.push('q='      + encodeURIComponent(p.q));
        if (p.status) qs.push('status=' + encodeURIComponent(p.status));
        if (p.date)   qs.push('date='   + encodeURIComponent(p.date));
      }
      return R('GET', BASE + (qs.length ? ('?' + qs.join('&')) : ''));
    },
    get:    function (id)          { return R('GET',    BASE + '/' + id); },
    create: function (payload)     { return R('POST',   BASE, payload); },
    update: function (id, payload) { return R('PUT',    BASE + '/' + id, payload); },
    remove: function (id)          { return R('DELETE', BASE + '/' + id); }
  };
  window.ELRS.AppointmentAPI = AppointmentAPI;

  function to12h(t) {
    if (!t) return '';
    var parts = t.split(':'); var h = Number(parts[0]); var m = parts[1] || '00';
    var ap = h >= 12 ? 'PM' : 'AM'; var hh = h % 12; if (hh === 0) hh = 12;
    return (hh < 10 ? '0' + hh : hh) + ':' + m + ' ' + ap;
  }

  window.ELRS.mapAppointmentRow = function (row) {
    return {
      id: 'APT-' + (1000 + row.appointmentId),
      appointmentId: row.appointmentId,
      customer: row.customerName || '—',
      staff: row.staffName || 'Unassigned',
      service: row.serviceName || '—',
      date: row.appointmentDate || '',
      time: to12h(row.appointmentTime),
      duration: row.duration || '—',
      status: row.status || 'Booked',
      location: row.location || '—',
      notes: row.notes || ''
    };
  };

  // ---- ADD / EDIT page (add-appointment.html) -----------------------------
  function initAddPage() {
    var form = document.getElementById('appointmentForm');
    if (!form || document.querySelector('[data-appointment-list]')) return false;

    var editId = window.ELRS.qs('id');
    if (editId) {
      var h = document.querySelector('.appointment-title'); if (h) h.textContent = 'Edit Appointment';
      AppointmentAPI.get(editId).then(function (a) {
        setVal('customerName', a.customerName); setVal('assignedStaff', a.staffName);
        setVal('serviceName', a.serviceName); setVal('appointmentDate', a.appointmentDate);
        setVal('appointmentTime', a.appointmentTime); setVal('duration', a.duration);
        setVal('appointmentNote', a.notes);
      }).catch(function (e) { window.ELRS.toast(e.message, true); });
    }

    form.addEventListener('submit', function (ev) {
      ev.preventDefault();
      var fd = new FormData(form);
      var payload = {
        customerName: (fd.get('customerName') || '').trim(),
        assignedStaff: fd.get('assignedStaff'),
        serviceName: fd.get('serviceName'),
        appointmentDate: fd.get('appointmentDate'),
        appointmentTime: fd.get('appointmentTime'),
        duration: fd.get('duration'),
        appointmentNote: (fd.get('appointmentNote') || '').trim()
      };
      var op = editId ? AppointmentAPI.update(editId, payload) : AppointmentAPI.create(payload);
      op.then(function () { window.location.href = 'appointment.html'; })
        .catch(function (e) { alert('Could not save appointment: ' + e.message); });
    });
    return true;

    function setVal(id, v) { var el = document.getElementById(id); if (el != null && v != null) el.value = v; }
  }

  // ---- LIST page (appointment.html) ---------------------------------------
  function initListPage() {
    var list = document.querySelector('[data-appointment-list]');
    if (!list) return false;

    function load() {
      AppointmentAPI.list().then(function (rows) {
        var mapped = rows.map(window.ELRS.mapAppointmentRow);
        if (typeof window.ELRS.setAppointments === 'function') window.ELRS.setAppointments(mapped);
      }).catch(function (e) { window.ELRS.toast('load appointments: ' + e.message, true); });
    }

    // "Add" button navigates to the standalone add page.
    var addBtn = document.querySelector('[data-open-appointment-modal]');
    if (addBtn) {
      addBtn.addEventListener('click', function (ev) {
        ev.preventDefault(); ev.stopImmediatePropagation();
        window.location.href = '../appointments/add-appointment.html';
      }, true);
    }

    load();
    return true;
  }

  window.ELRS.ready(function () {
    if (!initAddPage()) initListPage();
  });
})();
