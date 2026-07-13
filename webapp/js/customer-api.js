/*
 * ELRS — Customer Management API client + page wiring.
 * Include AFTER customer-management.js (list) or on add-customer.html.
 */
(function () {
  'use strict';
  var R = window.ELRS.request;
  var BASE = window.ELRS.BASE + '/api/customers';

  var CustomerAPI = {
    list:   function (p) {
      var qs = [];
      if (p) {
        if (p.q)      qs.push('q='      + encodeURIComponent(p.q));
        if (p.type)   qs.push('type='   + encodeURIComponent(p.type));
        if (p.status) qs.push('status=' + encodeURIComponent(p.status));
      }
      return R('GET', BASE + (qs.length ? ('?' + qs.join('&')) : ''));
    },
    get:    function (id)          { return R('GET',    BASE + '/' + id); },
    create: function (payload)     { return R('POST',   BASE, payload); },
    update: function (id, payload) { return R('PUT',    BASE + '/' + id, payload); },
    remove: function (id)          { return R('DELETE', BASE + '/' + id); }
  };
  window.ELRS.CustomerAPI = CustomerAPI;

  function fmtDate(iso) {
    if (!iso) return '';
    var d = new Date(iso); if (isNaN(d.getTime())) return iso;
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  // Map backend row -> UI shape expected by customer-management.js
  window.ELRS.mapCustomerRow = function (row) {
    var addr = [row.address, row.city, row.state, row.pincode].filter(Boolean).join(', ');
    return {
      id: 'CUS-' + (2000 + row.customerId),
      customerId: row.customerId,
      name: row.fullName,
      phone: row.phone,
      email: row.email,
      address: addr,
      linkedLeads: 'No linked leads',
      appointments: 'No appointments',
      type: row.customerType || 'Individual',
      status: row.status || 'Active',
      createdDate: fmtDate(row.createdAt),
      services: 'No recent services',
      notes: row.notes || ''
    };
  };

  // ---- ADD / EDIT page (add-customer.html) --------------------------------
  function initAddPage() {
    var form = document.getElementById('customerForm');
    if (!form || document.querySelector('[data-customer-table-body]')) return false;

    var editId = window.ELRS.qs('id');
    if (editId) {
      var h = document.querySelector('.customer-title');
      if (h) h.textContent = 'Edit Customer';
      CustomerAPI.get(editId).then(function (c) {
        setVal('customerName', c.fullName); setVal('phoneNumber', c.phone);
        setVal('email', c.email); setVal('city', c.city); setVal('address', c.address);
        setVal('state', c.state); setVal('pincode', c.pincode);
        setVal('customerType', c.customerType); setVal('notes', c.notes);
      }).catch(function (e) { window.ELRS.toast(e.message, true); });
    }

    form.addEventListener('submit', function (ev) {
      ev.preventDefault();
      var fd = new FormData(form);
      var payload = {
        fullName: (fd.get('customerName') || '').trim(),
        phone: (fd.get('phoneNumber') || '').trim(),
        email: (fd.get('email') || '').trim(),
        city: (fd.get('city') || '').trim(),
        address: (fd.get('address') || '').trim(),
        state: (fd.get('state') || '').trim(),
        pincode: (fd.get('pincode') || '').trim(),
        customerType: fd.get('customerType') || 'Individual',
        notes: (fd.get('notes') || '').trim()
      };
      var op = editId ? CustomerAPI.update(editId, payload) : CustomerAPI.create(payload);
      op.then(function () { window.location.href = 'customer-management.html'; })
        .catch(function (e) { alert('Could not save customer: ' + e.message); });
    });
    return true;

    function setVal(id, v) { var el = document.getElementById(id); if (el != null && v != null) el.value = v; }
  }

  // ---- LIST page (customer-management.html) --------------------------------
  function initListPage() {
    var body = document.querySelector('[data-customer-table-body]');
    if (!body) return false;

    function load() {
      CustomerAPI.list().then(function (rows) {
        var mapped = rows.map(window.ELRS.mapCustomerRow);
        if (typeof window.ELRS.setCustomers === 'function') window.ELRS.setCustomers(mapped);
      }).catch(function (e) { window.ELRS.toast('load customers: ' + e.message, true); });
    }

    // Delete via any button carrying data-delete-customer="<numericId>"
    body.addEventListener('click', function (ev) {
      var edit = ev.target.closest('[data-edit-customer]');
      if (edit) { ev.stopPropagation(); window.location.href = '../customers/add-customer.html?id=' + edit.getAttribute('data-edit-customer'); return; }
      var del = ev.target.closest('[data-delete-customer]');
      if (!del) return;
      ev.stopPropagation();
      var id = del.getAttribute('data-delete-customer');
      if (!confirm('Delete this customer?')) return;
      CustomerAPI.remove(id).then(load).catch(function (e) { alert(e.message); });
    });

    window.ELRS.reloadCustomers = load;
    load();
    return true;
  }

  window.ELRS.ready(function () {
    if (!initAddPage()) initListPage();
  });
})();
