/*
 * ELRS — Services API client + page wiring.
 * Include AFTER services.js (list) or on add-service.html.
 */
(function () {
  'use strict';
  var R = window.ELRS.request;
  var BASE = window.ELRS.BASE + '/api/services';

  var ServiceAPI = {
    list:   function (p) {
      var qs = [];
      if (p) {
        if (p.q)        qs.push('q='        + encodeURIComponent(p.q));
        if (p.category) qs.push('category=' + encodeURIComponent(p.category));
        if (p.status)   qs.push('status='   + encodeURIComponent(p.status));
      }
      return R('GET', BASE + (qs.length ? ('?' + qs.join('&')) : ''));
    },
    get:    function (id)          { return R('GET',    BASE + '/' + id); },
    create: function (payload)     { return R('POST',   BASE, payload); },
    update: function (id, payload) { return R('PUT',    BASE + '/' + id, payload); },
    remove: function (id)          { return R('DELETE', BASE + '/' + id); }
  };
  window.ELRS.ServiceAPI = ServiceAPI;

  function fmtDate(iso) {
    if (!iso) return '';
    var d = new Date(iso); if (isNaN(d.getTime())) return iso;
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
  function fmtPrice(p) {
    if (p == null || p === '') return '₹0';
    var n = Number(p); if (isNaN(n)) return '₹' + p;
    return '₹' + n.toLocaleString('en-IN');
  }

  window.ELRS.mapServiceRow = function (row) {
    return {
      id: 'SRV-' + (100 + row.serviceId),
      serviceId: row.serviceId,
      name: row.name,
      category: row.category || '—',
      price: fmtPrice(row.price),
      duration: row.duration || '—',
      status: row.status || 'Active',
      updated: fmtDate(row.updatedAt || row.createdAt),
      availability: row.status === 'Active' ? 'Available' : (row.status || ''),
      description: row.description || ''
    };
  };

  // ---- ADD / EDIT page (add-service.html) ---------------------------------
  function initAddPage() {
    var form = document.getElementById('serviceForm');
    if (!form || document.querySelector('[data-service-table]')) return false;

    var editId = window.ELRS.qs('id');
    if (editId) {
      var h = document.querySelector('.service-title'); if (h) h.textContent = 'Edit Service';
      ServiceAPI.get(editId).then(function (s) {
        setVal('serviceName', s.name); setVal('category', s.category);
        setVal('description', s.description); setVal('price', s.price);
        setVal('duration', s.duration); setVal('status', s.status);
      }).catch(function (e) { window.ELRS.toast(e.message, true); });
    }

    form.addEventListener('submit', function (ev) {
      ev.preventDefault();
      var fd = new FormData(form);
      var payload = {
        name: fd.get('serviceName'),
        category: fd.get('category'),
        description: (fd.get('description') || '').trim(),
        price: fd.get('price'),
        duration: fd.get('duration'),
        status: fd.get('status') || 'Active'
      };
      var op = editId ? ServiceAPI.update(editId, payload) : ServiceAPI.create(payload);
      op.then(function () { window.location.href = 'services.html'; })
        .catch(function (e) { alert('Could not save service: ' + e.message); });
    });
    return true;

    function setVal(id, v) { var el = document.getElementById(id); if (el != null && v != null) el.value = v; }
  }

  // ---- LIST page (services.html) ------------------------------------------
  function initListPage() {
    var table = document.querySelector('[data-service-table]');
    if (!table) return false;

    function load() {
      ServiceAPI.list().then(function (rows) {
        var mapped = rows.map(window.ELRS.mapServiceRow);
        if (typeof window.ELRS.setServices === 'function') window.ELRS.setServices(mapped);
      }).catch(function (e) { window.ELRS.toast('load services: ' + e.message, true); });
    }

    table.addEventListener('click', function (ev) {
      var editBtn = ev.target.closest('[data-edit-service]');
      var delBtn = ev.target.closest('[data-delete-service]');
      var row = ev.target.closest('tr[data-service-id]');
      if (!row) return;
      var uiId = row.getAttribute('data-service-id');          // e.g. SRV-101
      var numId = uiId.replace(/^SRV-/, '');
      numId = String(Number(numId) - 100);
      if (editBtn) {
        ev.stopPropagation();
        window.location.href = '../services/add-service.html?id=' + numId;
      } else if (delBtn) {
        ev.stopPropagation();
        if (!confirm('Delete this service?')) return;
        ServiceAPI.remove(numId).then(load).catch(function (e) { alert(e.message); });
      }
    }, true);

    load();
    return true;
  }

  window.ELRS.ready(function () {
    if (!initAddPage()) initListPage();
  });
})();
