(function () {
  'use strict';

  var customers = [
    {
      id: 'CUS-2001',
      name: 'Ananya Desai',
      phone: '+91 98765 12001',
      email: 'ananya.desai@example.com',
      address: '42 Park Street, Mumbai, Maharashtra 400001',
      linkedLeads: 'ELRS-1001, ELRS-1018',
      appointments: '2 completed, 1 upcoming',
      type: 'VIP',
      status: 'Active',
      createdDate: '08 Jul 2026',
      services: 'Emergency Plumbing, Water Damage',
      notes: 'Prefers morning appointments and quick phone updates.'
    },
    {
      id: 'CUS-2002',
      name: 'Rohit Menon',
      phone: '+91 97654 12002',
      email: 'rohit.menon@example.com',
      address: '18 Lake View Road, Pune, Maharashtra 411001',
      linkedLeads: 'ELRS-1002',
      appointments: '1 completed',
      type: 'New',
      status: 'Active',
      createdDate: '08 Jul 2026',
      services: 'Electrical Repair',
      notes: 'First-time customer with urgent repair request.'
    },
    {
      id: 'CUS-2003',
      name: 'Isha Trivedi',
      phone: '+91 96543 12003',
      email: 'isha.trivedi@example.com',
      address: '73 Green Avenue, Ahmedabad, Gujarat 380015',
      linkedLeads: 'ELRS-1003, ELRS-1022',
      appointments: '3 completed',
      type: 'Existing',
      status: 'Active',
      createdDate: '07 Jul 2026',
      services: 'HVAC Support, Roof Leak Repair',
      notes: 'Has recurring seasonal maintenance needs.'
    },
    {
      id: 'CUS-2004',
      name: 'Kabir Sethi',
      phone: '+91 95432 12004',
      email: 'kabir.sethi@example.com',
      address: '11 North Lane, Delhi, Delhi 110001',
      linkedLeads: 'ELRS-1004',
      appointments: 'No appointments',
      type: 'New',
      status: 'Inactive',
      createdDate: '06 Jul 2026',
      services: 'Roof Leak Repair',
      notes: 'Lead went inactive after quote follow-up.'
    },
    {
      id: 'CUS-2005',
      name: 'Megha Kulkarni',
      phone: '+91 94321 12005',
      email: 'megha.kulkarni@example.com',
      address: '8 Hill Road, Nashik, Maharashtra 422001',
      linkedLeads: 'ELRS-1005, ELRS-1031',
      appointments: '4 completed',
      type: 'VIP',
      status: 'Active',
      createdDate: '05 Jul 2026',
      services: 'Water Damage, Emergency Plumbing',
      notes: 'High-value customer with multiple properties.'
    },
    {
      id: 'CUS-2006',
      name: 'Dev Patel',
      phone: '+91 93210 12006',
      email: 'dev.patel@example.com',
      address: '29 Riverfront Road, Surat, Gujarat 395007',
      linkedLeads: 'ELRS-1006',
      appointments: '1 upcoming',
      type: 'Existing',
      status: 'Active',
      createdDate: '04 Jul 2026',
      services: 'Electrical Repair',
      notes: 'Requested technician arrival before noon.'
    },
    {
      id: 'CUS-2007',
      name: 'Sara Khan',
      phone: '+91 92109 12007',
      email: 'sara.khan@example.com',
      address: '55 Residency Road, Bengaluru, Karnataka 560025',
      linkedLeads: 'ELRS-1007',
      appointments: '2 cancelled',
      type: 'Existing',
      status: 'Inactive',
      createdDate: '03 Jul 2026',
      services: 'HVAC Support',
      notes: 'Reactivation campaign candidate.'
    },
    {
      id: 'CUS-2008',
      name: 'Nikhil Rao',
      phone: '+91 91098 12008',
      email: 'nikhil.rao@example.com',
      address: '102 Ocean Drive, Chennai, Tamil Nadu 600001',
      linkedLeads: 'ELRS-1008, ELRS-1037',
      appointments: '5 completed',
      type: 'VIP',
      status: 'Active',
      createdDate: '02 Jul 2026',
      services: 'Water Damage, HVAC Support',
      notes: 'VIP customer. Keep response time below 15 minutes.'
    }
  ];

  var tableBody = document.querySelector('[data-customer-table-body]');
  var searchInput = document.getElementById('customer-search');
  var customerFilter = document.getElementById('customer-filter');
  var resultCount = document.querySelector('[data-result-count]');
  var emptyState = document.querySelector('[data-empty-state]');
  var modalBackdrop = document.querySelector('[data-modal-backdrop]');
  var drawerBackdrop = document.querySelector('[data-drawer-backdrop]');
  var openModalButtons = document.querySelectorAll('[data-open-modal]');
  var closeModalButtons = document.querySelectorAll('[data-close-modal]');
  var closeDrawerButton = document.querySelector('[data-close-drawer]');
  var customerForm = document.querySelector('[data-customer-form]');
  var lastFocusedElement = null;

  if (!tableBody || !searchInput || !customerFilter || !modalBackdrop || !drawerBackdrop || !customerForm) {
    return;
  }

  function getBadgeClass(value) {
    return 'status-badge--' + value.toLowerCase().replace(/\s+/g, '-');
  }

  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function getInitials(name) {
    return name
      .split(' ')
      .map(function (word) {
        return word.charAt(0);
      })
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }

  // Customer table rendering
  function renderCustomers() {
    var query = searchInput.value.trim().toLowerCase();
    var selectedFilter = customerFilter.value;

    var filteredCustomers = customers.filter(function (customer) {
      var matchesSearch = !query ||
        customer.id.toLowerCase().includes(query) ||
        customer.name.toLowerCase().includes(query) ||
        customer.phone.toLowerCase().includes(query) ||
        customer.email.toLowerCase().includes(query);
      var matchesFilter = selectedFilter === 'All Customers' ||
        customer.type === selectedFilter ||
        customer.status === selectedFilter;

      return matchesSearch && matchesFilter;
    });

    tableBody.innerHTML = filteredCustomers.map(function (customer) {
      return [
        '<tr>',
        '<td><span class="customer-id">' + escapeHtml(customer.id) + '</span></td>',
        '<td><span class="customer-name">' + escapeHtml(customer.name) + '</span></td>',
        '<td>' + escapeHtml(customer.phone) + '</td>',
        '<td><span class="customer-muted">' + escapeHtml(customer.email) + '</span></td>',
        '<td><span class="customer-muted">' + escapeHtml(customer.address) + '</span></td>',
        '<td>' + escapeHtml(customer.linkedLeads) + '</td>',
        '<td>' + escapeHtml(customer.appointments) + '</td>',
        '<td><span class="status-badge ' + getBadgeClass(customer.type) + '">' + escapeHtml(customer.type) + '</span></td>',
        '<td><span class="status-badge ' + getBadgeClass(customer.status) + '">' + escapeHtml(customer.status) + '</span></td>',
        '<td>' + escapeHtml(customer.createdDate) + '</td>',
        '<td>',
        '<div class="table-actions">',
        '<button class="action-button action-button--view" type="button" data-view-customer="' + escapeHtml(customer.id) + '" aria-label="View ' + escapeHtml(customer.name) + ' customer profile">',
        '<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path d="M12 5c5.5 0 9 5.1 9 7s-3.5 7-9 7-9-5.1-9-7 3.5-7 9-7Zm0 2c-4 0-6.6 3.8-7 5 .4 1.2 3 5 7 5s6.6-3.8 7-5c-.4-1.2-3-5-7-5Zm0 2.5A2.5 2.5 0 1 1 12 14a2.5 2.5 0 0 1 0-5Z"/></svg>',
        '</button>',
        '<button class="action-button action-button--edit" type="button" aria-label="Edit ' + escapeHtml(customer.name) + ' customer">',
        '<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path d="M4 17.2V20h2.8L17.1 9.7l-2.8-2.8L4 17.2ZM19.3 7.5a1 1 0 0 0 0-1.4l-1.4-1.4a1 1 0 0 0-1.4 0l-1.1 1.1 2.8 2.8 1.1-1.1Z"/></svg>',
        '</button>',
        '<button class="action-button action-button--delete" type="button" aria-label="Delete ' + escapeHtml(customer.name) + ' customer">',
        '<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path d="M7 21h10l1-14H6l1 14ZM9 4h6l1 2H8l1-2Zm-4 2h14v2H5V6Z"/></svg>',
        '</button>',
        '</div>',
        '</td>',
        '</tr>'
      ].join('');
    }).join('');

    resultCount.textContent = String(filteredCustomers.length);
    emptyState.hidden = filteredCustomers.length > 0;
  }

  function updateText(selector, value) {
    var element = document.querySelector(selector);

    if (element) {
      element.textContent = value;
    }
  }

  // Drawer controls
  function openDrawer(customerId) {
    var customer = customers.find(function (item) {
      return item.id === customerId;
    });

    if (!customer) {
      return;
    }

    lastFocusedElement = document.activeElement;
    updateText('[data-drawer-initials]', getInitials(customer.name));
    updateText('[data-drawer-name]', customer.name);
    updateText('[data-drawer-email]', customer.email);
    updateText('[data-drawer-status]', customer.status);
    updateText('[data-drawer-id]', customer.id);
    updateText('[data-drawer-phone]', customer.phone);
    updateText('[data-drawer-type]', customer.type);
    updateText('[data-drawer-created]', customer.createdDate);
    updateText('[data-drawer-address]', customer.address);
    updateText('[data-drawer-leads]', customer.linkedLeads);
    updateText('[data-drawer-appointments]', customer.appointments);
    updateText('[data-drawer-services]', customer.services);
    updateText('[data-drawer-notes]', customer.notes);

    var drawerStatus = document.querySelector('[data-drawer-status]');
    drawerStatus.className = 'status-badge ' + getBadgeClass(customer.status);

    drawerBackdrop.hidden = false;
    document.body.style.overflow = 'hidden';
    closeDrawerButton.focus();
  }

  function closeDrawer() {
    drawerBackdrop.hidden = true;
    document.body.style.overflow = '';

    if (lastFocusedElement) {
      lastFocusedElement.focus();
    }
  }

  function getFocusableElements(container) {
    return container.querySelectorAll('button, input, select, textarea, [href], [tabindex]:not([tabindex="-1"])');
  }

  function trapFocus(event, container) {
    if (event.key !== 'Tab' || container.hidden) {
      return;
    }

    var focusableElements = Array.prototype.slice.call(getFocusableElements(container));
    var firstElement = focusableElements[0];
    var lastElement = focusableElements[focusableElements.length - 1];

    if (!firstElement || !lastElement) {
      return;
    }

    if (event.shiftKey && document.activeElement === firstElement) {
      event.preventDefault();
      lastElement.focus();
    } else if (!event.shiftKey && document.activeElement === lastElement) {
      event.preventDefault();
      firstElement.focus();
    }
  }

  // Modal controls
  function openModal() {
    lastFocusedElement = document.activeElement;
    modalBackdrop.hidden = false;
    document.body.style.overflow = 'hidden';
    customerForm.querySelector('input, select, textarea, button').focus();
  }

  function closeModal() {
    modalBackdrop.hidden = true;
    document.body.style.overflow = '';
    customerForm.reset();
    clearFormErrors();

    if (lastFocusedElement) {
      lastFocusedElement.focus();
    }
  }

  // Form validation
  function setFieldError(field, message) {
    var wrapper = field.closest('.form-field');
    var error = customerForm.querySelector('[data-error-for="' + field.name + '"]');

    wrapper.classList.toggle('is-invalid', Boolean(message));
    field.setAttribute('aria-invalid', message ? 'true' : 'false');

    if (error) {
      error.textContent = message;
    }
  }

  function clearFormErrors() {
    customerForm.querySelectorAll('.form-field').forEach(function (field) {
      field.classList.remove('is-invalid');
    });

    customerForm.querySelectorAll('[aria-invalid]').forEach(function (field) {
      field.removeAttribute('aria-invalid');
    });

    customerForm.querySelectorAll('.form-error').forEach(function (error) {
      error.textContent = '';
    });
  }

  function isValidEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  }

  function isValidPhone(value) {
    return /^[+\d][\d\s()-]{7,}$/.test(value);
  }

  function isValidPincode(value) {
    return /^\d{5,6}$/.test(value);
  }

  function validateForm() {
    var isValid = true;
    var fields = customerForm.querySelectorAll('input[required], select[required], textarea[required]');

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

      if (field.name === 'pincode' && !isValidPincode(value)) {
        setFieldError(field, 'Enter a valid pincode.');
        isValid = false;
      }
    });

    return isValid;
  }

  function addCustomerFromForm() {
    var formData = new FormData(customerForm);
    var nextId = 'CUS-' + String(2001 + customers.length).padStart(4, '0');
    var createdDate = new Intl.DateTimeFormat('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    }).format(new Date());
    var addressParts = [
      formData.get('address').trim(),
      formData.get('city').trim(),
      formData.get('state').trim(),
      formData.get('pincode').trim()
    ];

    customers.unshift({
      id: nextId,
      name: formData.get('customerName').trim(),
      phone: formData.get('phone').trim(),
      email: formData.get('email').trim(),
      address: addressParts.join(', '),
      linkedLeads: 'No linked leads',
      appointments: 'No appointments',
      type: formData.get('customerType'),
      status: 'Active',
      createdDate: createdDate,
      services: 'No recent services',
      notes: formData.get('notes').trim()
    });

    searchInput.value = '';
    customerFilter.value = 'All Customers';
    renderCustomers();
  }

  searchInput.addEventListener('input', renderCustomers);
  customerFilter.addEventListener('change', renderCustomers);

  openModalButtons.forEach(function (button) {
    button.addEventListener('click', openModal);
  });

  closeModalButtons.forEach(function (button) {
    button.addEventListener('click', closeModal);
  });

  closeDrawerButton.addEventListener('click', closeDrawer);

  tableBody.addEventListener('click', function (event) {
    var viewButton = event.target.closest('[data-view-customer]');

    if (viewButton) {
      openDrawer(viewButton.getAttribute('data-view-customer'));
    }
  });

  modalBackdrop.addEventListener('click', function (event) {
    if (event.target === modalBackdrop) {
      closeModal();
    }
  });

  drawerBackdrop.addEventListener('click', function (event) {
    if (event.target === drawerBackdrop) {
      closeDrawer();
    }
  });

  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
      if (!modalBackdrop.hidden) {
        closeModal();
      } else if (!drawerBackdrop.hidden) {
        closeDrawer();
      }
    }

    trapFocus(event, modalBackdrop);
    trapFocus(event, drawerBackdrop);
  });

  customerForm.addEventListener('submit', function (event) {
    event.preventDefault();

    if (!validateForm()) {
      var firstInvalidField = customerForm.querySelector('.is-invalid input, .is-invalid select, .is-invalid textarea');

      if (firstInvalidField) {
        firstInvalidField.focus();
      }

      return;
    }

    addCustomerFromForm();
    closeModal();
  });

  customerForm.addEventListener('input', function (event) {
    if (event.target.matches('input, textarea')) {
      setFieldError(event.target, '');
    }
  });

  customerForm.addEventListener('change', function (event) {
    if (event.target.matches('select')) {
      setFieldError(event.target, '');
    }
  });

  renderCustomers();
})();
