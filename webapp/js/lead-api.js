/*
 * ELRS — Lead Management API client
 * ---------------------------------
 * Thin wrapper around the /api/leads servlet.
 * Included AFTER lead-management.js on lead-management.html so it can
 * hydrate the existing table with server data without changing the UI.
 *
 *   <script src="../../js/lead-management.js"></script>
 *   <script src="../../js/lead-api.js"></script>
 */
(function () {
  'use strict';

  var BASE = '/elrs/api/leads';

  // ---- REST helpers ----------------------------------------------------
  function request(method, url, body) {
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
  }

  var LeadAPI = {
    list: function (params) {
      var qs = [];
      if (params) {
        if (params.q)        qs.push('q='        + encodeURIComponent(params.q));
        if (params.status)   qs.push('status='   + encodeURIComponent(params.status));
        if (params.priority) qs.push('priority=' + encodeURIComponent(params.priority));
      }
      return request('GET', BASE + (qs.length ? ('?' + qs.join('&')) : ''));
    },
    get:          function (id)              { return request('GET',    BASE + '/' + id); },
    create:       function (payload)         { return request('POST',   BASE, payload); },
    update:       function (id, payload)     { return request('PUT',    BASE + '/' + id, payload); },
    updateStatus: function (id, status)      { return request('PUT',    BASE + '/' + id + '/status', { status: status }); },
    assign:       function (id, staffId)     { return request('PUT',    BASE + '/' + id + '/assign', { staffId: staffId }); },
    remove:       function (id)              { return request('DELETE', BASE + '/' + id); }
  };

  // ---- Map server row -> UI row shape used by lead-management.js -------
  // UI (existing) expects: { id, name, phone, email, service, assignedTo, status, createdDate }
  function toUi(row) {
    return {
      id:          'ELRS-' + (1000 + row.leadId),
      leadId:      row.leadId,
      name:        row.customerName,
      phone:       row.customerPhone,
      email:       row.customerEmail,
      service:     row.serviceName || '—',
      assignedTo:  row.assignedStaffName || 'Unassigned',
      status:      toTitle(row.status),
      priority:    toTitle(row.priority),
      createdDate: formatDate(row.createdAt)
    };
  }

  function toTitle(s) {
    if (!s) return '';
    return s.toLowerCase().split('_').map(function (w) {
      return w.charAt(0).toUpperCase() + w.slice(1);
    }).join(' ');
  }

  function formatDate(iso) {
    if (!iso) return '';
    var d = new Date(iso);
    if (isNaN(d.getTime())) return iso;
    var day = String(d.getDate()).padStart(2, '0');
    var mon = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'][d.getMonth()];
    return day + ' ' + mon + ' ' + d.getFullYear();
  }

  // Expose for lead-management.js to consume
  window.ELRS = window.ELRS || {};
  window.ELRS.LeadAPI = LeadAPI;
  window.ELRS.mapLeadRow = toUi;

  // ---- Auto-hydrate the table if the page has already rendered ---------
  document.addEventListener('DOMContentLoaded', function () {
    LeadAPI.list().then(function (rows) {
      var mapped = rows.map(toUi);
      if (typeof window.setLeads === 'function') {
        // If lead-management.js exposes a setter, use it.
        window.setLeads(mapped);
      } else {
        // Fallback: emit a custom event any renderer can subscribe to.
        document.dispatchEvent(new CustomEvent('elrs:leads-loaded', { detail: mapped }));
      }
    }).catch(function (err) {
      console.error('[ELRS] Failed to load leads:', err);
    });
  });
})();
