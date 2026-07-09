(function () {
  'use strict';

  var services = [
    { id: 'SRV-101', name: 'Emergency Plumbing', category: 'Plumbing', price: '₹2,500', duration: '1 hour', status: 'Active', updated: '09 Jul 2026', availability: '24/7 Emergency', description: 'Rapid response for burst pipes, leaks, clogs and urgent water line issues.' },
    { id: 'SRV-102', name: 'Electrical Repair', category: 'Electrical', price: '₹3,200', duration: '90 minutes', status: 'Active', updated: '08 Jul 2026', availability: 'Same Day', description: 'Troubleshooting and repair for power outages, short circuits and unsafe wiring.' },
    { id: 'SRV-103', name: 'HVAC Support', category: 'HVAC', price: '₹4,800', duration: '2 hours', status: 'Active', updated: '07 Jul 2026', availability: 'Business Hours', description: 'Heating and cooling system diagnosis, maintenance and emergency repairs.' },
    { id: 'SRV-104', name: 'Water Damage Control', category: 'Restoration', price: '₹7,500', duration: '3 hours', status: 'Active', updated: '06 Jul 2026', availability: '24/7 Emergency', description: 'Initial mitigation for water intrusion, extraction and damage assessment.' },
    { id: 'SRV-105', name: 'Roof Leak Inspection', category: 'Inspection', price: '₹2,000', duration: '45 minutes', status: 'Inactive', updated: '03 Jul 2026', availability: 'Temporarily Paused', description: 'Site inspection for roof leaks, drainage issues and visible structural damage.' },
    { id: 'SRV-106', name: 'Fire Safety Check', category: 'Inspection', price: '₹3,600', duration: '1 hour', status: 'Active', updated: '01 Jul 2026', availability: 'Weekdays', description: 'Safety review of alarms, extinguishers, exits and emergency readiness.' }
  ];

  var selectedServiceId = services[0].id;
  var els = {};

  document.addEventListener('DOMContentLoaded', function () {
    els.search = document.getElementById('serviceSearch');
    els.category = document.getElementById('categoryFilter');
    els.status = document.getElementById('serviceStatusFilter');
    els.table = document.querySelector('[data-service-table]');
    els.count = document.querySelector('[data-service-count]');
    els.details = document.querySelector('[data-service-details]');
    els.serviceModal = document.querySelector('[data-service-modal]');
    els.deleteModal = document.querySelector('[data-delete-service-modal]');

    bindEvents();
    renderServices();
    renderDetails(services[0]);
  });

  function bindEvents() {
    [els.search, els.category, els.status].forEach(function (control) {
      control.addEventListener('input', renderServices);
      control.addEventListener('change', renderServices);
    });

    document.querySelector('[data-open-add-service]').addEventListener('click', function () {
      document.getElementById('serviceModalTitle').textContent = 'Add Service';
      els.serviceModal.hidden = false;
    });

    document.querySelectorAll('[data-close-service-modal]').forEach(function (button) {
      button.addEventListener('click', function () { els.serviceModal.hidden = true; });
    });

    document.querySelectorAll('[data-close-delete-modal]').forEach(function (button) {
      button.addEventListener('click', function () { els.deleteModal.hidden = true; });
    });

    [els.serviceModal, els.deleteModal].forEach(function (modal) {
      modal.addEventListener('click', function (event) {
        if (event.target === modal) {
          modal.hidden = true;
        }
      });
    });
  }

  function renderServices() {
    var filtered = getFilteredServices();
    els.count.textContent = String(filtered.length);
    els.table.innerHTML = filtered.map(createRow).join('') || '<tr><td colspan="8">No services found.</td></tr>';

    els.table.querySelectorAll('tr[data-service-id]').forEach(function (row) {
      row.addEventListener('click', function () {
        selectedServiceId = row.dataset.serviceId;
        renderDetails(findService(selectedServiceId));
        renderServices();
      });
    });

    els.table.querySelectorAll('[data-edit-service]').forEach(function (button) {
      button.addEventListener('click', function (event) {
        event.stopPropagation();
        document.getElementById('serviceModalTitle').textContent = 'Edit Service';
        els.serviceModal.hidden = false;
      });
    });

    els.table.querySelectorAll('[data-delete-service]').forEach(function (button) {
      button.addEventListener('click', function (event) {
        event.stopPropagation();
        els.deleteModal.hidden = false;
      });
    });
  }

  function getFilteredServices() {
    var term = els.search.value.trim().toLowerCase();
    var category = els.category.value;
    var status = els.status.value;

    return services.filter(function (item) {
      var matchesText = !term || [item.id, item.name, item.category, item.description].join(' ').toLowerCase().includes(term);
      var matchesCategory = category === 'All' || item.category === category;
      var matchesStatus = status === 'All' || item.status === status;
      return matchesText && matchesCategory && matchesStatus;
    });
  }

  function createRow(item) {
    return '<tr data-service-id="' + item.id + '" class="' + (item.id === selectedServiceId ? 'is-selected' : '') + '">' +
      '<td class="service-id">' + item.id + '</td><td><strong>' + item.name + '</strong></td><td>' + item.category + '</td><td>' + item.price + '</td><td>' + item.duration + '</td><td><span class="status-badge status-' + item.status.toLowerCase() + '">' + item.status + '</span></td><td>' + item.updated + '</td>' +
      '<td><div class="table-actions"><button class="action-button action-button--view" type="button" title="View"><i class="fa-solid fa-eye"></i></button><button class="action-button action-button--edit" type="button" title="Edit" data-edit-service><i class="fa-solid fa-pen"></i></button><button class="action-button action-button--delete" type="button" title="Delete" data-delete-service><i class="fa-solid fa-trash"></i></button></div></td>' +
      '</tr>';
  }

  function renderDetails(item) {
    els.details.innerHTML = '<h2>' + item.name + '</h2><p>' + item.description + '</p><dl class="detail-list">' +
      detail('Category', item.category) + detail('Price', item.price) + detail('Estimated Duration', item.duration) + detail('Availability', item.availability) + detail('Status', '<span class="status-badge status-' + item.status.toLowerCase() + '">' + item.status + '</span>') +
      '</dl>';
  }

  function detail(label, value) {
    return '<div><dt>' + label + '</dt><dd>' + value + '</dd></div>';
  }

  function findService(id) {
    return services.find(function (item) { return item.id === id; }) || services[0];
  }
}());
