(function () {
  'use strict';

  var leads = [
    {
      id: 'ELRS-1001',
      name: 'Amit Sharma',
      phone: '+91 98765 43210',
      email: 'amit.sharma@example.com',
      service: 'Emergency Plumbing',
      assignedTo: 'Aarav Mehta',
      status: 'New',
      createdDate: '08 Jul 2026'
    },
    {
      id: 'ELRS-1002',
      name: 'Priya Kapoor',
      phone: '+91 99887 77665',
      email: 'priya.kapoor@example.com',
      service: 'Electrical Repair',
      assignedTo: 'Nisha Rao',
      status: 'Contacted',
      createdDate: '08 Jul 2026'
    },
    {
      id: 'ELRS-1003',
      name: 'Rahul Verma',
      phone: '+91 91234 56780',
      email: 'rahul.verma@example.com',
      service: 'HVAC Support',
      assignedTo: 'Rohan Shah',
      status: 'Qualified',
      createdDate: '07 Jul 2026'
    },
    {
      id: 'ELRS-1004',
      name: 'Neha Iyer',
      phone: '+91 90123 45678',
      email: 'neha.iyer@example.com',
      service: 'Roof Leak Repair',
      assignedTo: 'Priya Nair',
      status: 'Quote Sent',
      createdDate: '07 Jul 2026'
    },
    {
      id: 'ELRS-1005',
      name: 'Karan Malhotra',
      phone: '+91 97654 32109',
      email: 'karan.malhotra@example.com',
      service: 'Water Damage',
      assignedTo: 'Aarav Mehta',
      status: 'Booked',
      createdDate: '06 Jul 2026'
    },
    {
      id: 'ELRS-1006',
      name: 'Sneha Reddy',
      phone: '+91 93456 78120',
      email: 'sneha.reddy@example.com',
      service: 'Emergency Plumbing',
      assignedTo: 'Nisha Rao',
      status: 'Completed',
      createdDate: '05 Jul 2026'
    },
    {
      id: 'ELRS-1007',
      name: 'Vikram Singh',
      phone: '+91 94567 89012',
      email: 'vikram.singh@example.com',
      service: 'Electrical Repair',
      assignedTo: 'Rohan Shah',
      status: 'Lost',
      createdDate: '04 Jul 2026'
    },
    {
      id: 'ELRS-1008',
      name: 'Meera Joshi',
      phone: '+91 92345 67891',
      email: 'meera.joshi@example.com',
      service: 'HVAC Support',
      assignedTo: 'Priya Nair',
      status: 'Pending',
      createdDate: '03 Jul 2026'
    }
  ];

  var tableBody = document.querySelector('[data-lead-table-body]');
  var searchInput = document.getElementById('lead-search');
  var statusFilter = document.getElementById('status-filter');
  var resultCount = document.querySelector('[data-result-count]');
  var emptyState = document.querySelector('[data-empty-state]');
  var modalBackdrop = document.querySelector('[data-modal-backdrop]');
  var openModalButton = document.querySelector('[data-open-modal]');
  var closeModalButtons = document.querySelectorAll('[data-close-modal]');
  var leadForm = document.querySelector('[data-lead-form]');
  var lastFocusedElement = null;

  if (!tableBody || !searchInput || !statusFilter || !modalBackdrop || !leadForm) {
    return;
  }

  function getStatusClass(status) {
    return 'status-badge--' + status.toLowerCase().replace(/\s+/g, '-');
  }

  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // Lead table rendering
  function renderLeads() {
    var query = searchInput.value.trim().toLowerCase();
    var selectedStatus = statusFilter.value;

    var filteredLeads = leads.filter(function (lead) {
      var matchesSearch = !query ||
        lead.name.toLowerCase().includes(query) ||
        lead.phone.toLowerCase().includes(query) ||
        lead.email.toLowerCase().includes(query);
      var matchesStatus = selectedStatus === 'All' || lead.status === selectedStatus;

      return matchesSearch && matchesStatus;
    });

    tableBody.innerHTML = filteredLeads.map(function (lead) {
      return [
        '<tr>',
        '<td><span class="lead-id">' + escapeHtml(lead.id) + '</span></td>',
        '<td><span class="lead-customer">' + escapeHtml(lead.name) + '</span></td>',
        '<td>' + escapeHtml(lead.phone) + '</td>',
        '<td><span class="lead-muted">' + escapeHtml(lead.email) + '</span></td>',
        '<td>' + escapeHtml(lead.service) + '</td>',
        '<td>' + escapeHtml(lead.assignedTo) + '</td>',
        '<td><span class="status-badge ' + getStatusClass(lead.status) + '">' + escapeHtml(lead.status) + '</span></td>',
        '<td>' + escapeHtml(lead.createdDate) + '</td>',
        '<td>',
        '<div class="table-actions">',
        '<button class="action-button action-button--view" type="button" aria-label="View ' + escapeHtml(lead.name) + ' lead">',
        '<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path d="M12 5c5.5 0 9 5.1 9 7s-3.5 7-9 7-9-5.1-9-7 3.5-7 9-7Zm0 2c-4 0-6.6 3.8-7 5 .4 1.2 3 5 7 5s6.6-3.8 7-5c-.4-1.2-3-5-7-5Zm0 2.5A2.5 2.5 0 1 1 12 14a2.5 2.5 0 0 1 0-5Z"/></svg>',
        '</button>',
        '<button class="action-button action-button--edit" type="button" aria-label="Edit ' + escapeHtml(lead.name) + ' lead">',
        '<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path d="M4 17.2V20h2.8L17.1 9.7l-2.8-2.8L4 17.2ZM19.3 7.5a1 1 0 0 0 0-1.4l-1.4-1.4a1 1 0 0 0-1.4 0l-1.1 1.1 2.8 2.8 1.1-1.1Z"/></svg>',
        '</button>',
        '<button class="action-button action-button--delete" type="button" aria-label="Delete ' + escapeHtml(lead.name) + ' lead">',
        '<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path d="M7 21h10l1-14H6l1 14ZM9 4h6l1 2H8l1-2Zm-4 2h14v2H5V6Z"/></svg>',
        '</button>',
        '</div>',
        '</td>',
        '</tr>'
      ].join('');
    }).join('');

    resultCount.textContent = String(filteredLeads.length);
    emptyState.hidden = filteredLeads.length > 0;
  }

  function getFocusableModalElements() {
    return modalBackdrop.querySelectorAll('button, input, select, textarea, [href], [tabindex]:not([tabindex="-1"])');
  }

  // Modal controls
  function openModal() {
    lastFocusedElement = document.activeElement;
    modalBackdrop.hidden = false;
    document.body.style.overflow = 'hidden';
    leadForm.querySelector('input, select, textarea, button').focus();
  }

  function closeModal() {
    modalBackdrop.hidden = true;
    document.body.style.overflow = '';
    leadForm.reset();
    clearFormErrors();

    if (lastFocusedElement) {
      lastFocusedElement.focus();
    }
  }

  function trapModalFocus(event) {
    if (modalBackdrop.hidden || event.key !== 'Tab') {
      return;
    }

    var focusableElements = Array.prototype.slice.call(getFocusableModalElements());
    var firstElement = focusableElements[0];
    var lastElement = focusableElements[focusableElements.length - 1];

    if (event.shiftKey && document.activeElement === firstElement) {
      event.preventDefault();
      lastElement.focus();
    } else if (!event.shiftKey && document.activeElement === lastElement) {
      event.preventDefault();
      firstElement.focus();
    }
  }

  // Form validation
  function setFieldError(field, message) {
    var wrapper = field.closest('.form-field');
    var error = leadForm.querySelector('[data-error-for="' + field.name + '"]');

    wrapper.classList.toggle('is-invalid', Boolean(message));
    field.setAttribute('aria-invalid', message ? 'true' : 'false');

    if (error) {
      error.textContent = message;
    }
  }

  function clearFormErrors() {
    leadForm.querySelectorAll('.form-field').forEach(function (field) {
      field.classList.remove('is-invalid');
    });

    leadForm.querySelectorAll('[aria-invalid]').forEach(function (field) {
      field.removeAttribute('aria-invalid');
    });

    leadForm.querySelectorAll('.form-error').forEach(function (error) {
      error.textContent = '';
    });
  }

  function isValidEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  }

  function isValidPhone(value) {
    return /^[+\d][\d\s()-]{7,}$/.test(value);
  }

  function validateForm() {
    var isValid = true;
    var fields = leadForm.querySelectorAll('input[required], select[required], textarea[required]');

    clearFormErrors();

    fields.forEach(function (field) {
      var value = field.value.trim();

      if (!value) {
        setFieldError(field, 'This field is required.');
        isValid = false;
        return;
      }

      if (field.name === 'email' && !isValidEmail(value)) {
        setFieldError(field, 'Enter a valid email address.');
        isValid = false;
      }

      if (field.name === 'phone' && !isValidPhone(value)) {
        setFieldError(field, 'Enter a valid phone number.');
        isValid = false;
      }
    });

    return isValid;
  }

  function addLeadFromForm() {
    var formData = new FormData(leadForm);
    var nextId = 'ELRS-' + String(1001 + leads.length).padStart(4, '0');
    var createdDate = new Intl.DateTimeFormat('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    }).format(new Date());

    leads.unshift({
      id: nextId,
      name: formData.get('customerName').trim(),
      phone: formData.get('phone').trim(),
      email: formData.get('email').trim(),
      service: formData.get('service'),
      assignedTo: formData.get('assignedTo'),
      status: formData.get('status'),
      createdDate: createdDate
    });

    searchInput.value = '';
    statusFilter.value = 'All';
    renderLeads();
  }

  searchInput.addEventListener('input', renderLeads);
  statusFilter.addEventListener('change', renderLeads);
  openModalButton.addEventListener('click', openModal);

  closeModalButtons.forEach(function (button) {
    button.addEventListener('click', closeModal);
  });

  modalBackdrop.addEventListener('click', function (event) {
    if (event.target === modalBackdrop) {
      closeModal();
    }
  });

  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape' && !modalBackdrop.hidden) {
      closeModal();
    }

    trapModalFocus(event);
  });

  leadForm.addEventListener('submit', function (event) {
    event.preventDefault();

    if (!validateForm()) {
      var firstInvalidField = leadForm.querySelector('.is-invalid input, .is-invalid select, .is-invalid textarea');

      if (firstInvalidField) {
        firstInvalidField.focus();
      }

      return;
    }

    addLeadFromForm();
    closeModal();
  });

  leadForm.addEventListener('input', function (event) {
    if (event.target.matches('input, textarea')) {
      setFieldError(event.target, '');
    }
  });

  leadForm.addEventListener('change', function (event) {
    if (event.target.matches('select')) {
      setFieldError(event.target, '');
    }
  });

  renderLeads();
})();
