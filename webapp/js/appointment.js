(function () {
  'use strict';

  var appointments = [
    { id: 'APT-1001', customer: 'Ananya Rao', staff: 'Aarav Mehta', service: 'Emergency Plumbing', date: '2026-07-09', time: '09:30 AM', duration: '1 hour', status: 'Booked', location: 'Andheri East, Mumbai', notes: 'Burst pipe inspection and emergency repair.' },
    { id: 'APT-1002', customer: 'Rahul Verma', staff: 'Nisha Rao', service: 'Electrical Repair', date: '2026-07-09', time: '11:15 AM', duration: '45 minutes', status: 'Pending', location: 'Bandra West, Mumbai', notes: 'Main panel tripping repeatedly.' },
    { id: 'APT-1003', customer: 'Meera Kapoor', staff: 'Priya Nair', service: 'HVAC Support', date: '2026-07-10', time: '02:00 PM', duration: '2 hours', status: 'Booked', location: 'Powai, Mumbai', notes: 'Cooling failure in office reception area.' },
    { id: 'APT-1004', customer: 'Dev Patel', staff: 'Rohan Shah', service: 'Water Damage', date: '2026-07-11', time: '10:00 AM', duration: '90 minutes', status: 'Rescheduled', location: 'Dadar, Mumbai', notes: 'Basement water seepage assessment.' },
    { id: 'APT-1005', customer: 'Sana Sheikh', staff: 'Aarav Mehta', service: 'Roof Leak Repair', date: '2026-07-13', time: '04:30 PM', duration: '1 hour', status: 'Completed', location: 'Thane West', notes: 'Temporary patch completed and quote shared.' },
    { id: 'APT-1006', customer: 'Karan Malhotra', staff: 'Priya Nair', service: 'Emergency Plumbing', date: '2026-07-15', time: '01:45 PM', duration: '30 minutes', status: 'Cancelled', location: 'Chembur, Mumbai', notes: 'Customer cancelled after issue was resolved.' }
  ];

  var visibleMonth = new Date(2026, 6, 1);
  var todayKey = '2026-07-09';
  var selectedDate = '';
  var els = {};

  document.addEventListener('DOMContentLoaded', function () {
    els.search = document.getElementById('appointmentSearch');
    els.status = document.getElementById('appointmentStatusFilter');
    els.date = document.getElementById('appointmentDateFilter');
    els.list = document.querySelector('[data-appointment-list]');
    els.count = document.querySelector('[data-appointment-count]');
    els.grid = document.querySelector('[data-calendar-grid]');
    els.month = document.querySelector('[data-calendar-month]');
    els.backdrop = document.querySelector('[data-appointment-backdrop]');

    bindEvents();
    renderAppointments();
    renderCalendar();
    renderSchedulePanel();
  });

  function bindEvents() {
    [els.search, els.status, els.date].forEach(function (control) {
      control.addEventListener('input', renderAppointments);
      control.addEventListener('change', renderAppointments);
    });

    document.querySelector('[data-prev-month]').addEventListener('click', function () {
      visibleMonth.setMonth(visibleMonth.getMonth() - 1);
      renderCalendar();
    });

    document.querySelector('[data-next-month]').addEventListener('click', function () {
      visibleMonth.setMonth(visibleMonth.getMonth() + 1);
      renderCalendar();
    });

    document.querySelector('[data-open-appointment-modal]').addEventListener('click', function () {
      els.backdrop.hidden = false;
    });

    document.querySelectorAll('[data-close-appointment-modal]').forEach(function (button) {
      button.addEventListener('click', function () {
        els.backdrop.hidden = true;
      });
    });

    els.backdrop.addEventListener('click', function (event) {
      if (event.target === els.backdrop) {
        els.backdrop.hidden = true;
      }
    });
  }

  function renderAppointments() {
    var filtered = getFilteredAppointments();
    els.count.textContent = String(filtered.length);
    els.list.innerHTML = filtered.map(createAppointmentCard).join('') || '<div class="appointment-card">No appointments found.</div>';
  }

  function getFilteredAppointments() {
    var term = els.search.value.trim().toLowerCase();
    var status = els.status.value;
    var date = els.date.value || selectedDate;

    return appointments.filter(function (item) {
      var matchesText = !term || [item.id, item.customer, item.staff, item.service, item.location].join(' ').toLowerCase().includes(term);
      var matchesStatus = status === 'All' || item.status === status;
      var matchesDate = !date || item.date === date;
      return matchesText && matchesStatus && matchesDate;
    });
  }

  function createAppointmentCard(item) {
    return '<article class="appointment-card">' +
      '<div class="appointment-card__top"><div><span class="appointment-id">' + item.id + '</span><h3>' + item.customer + '</h3></div><span class="status-badge status-' + item.status.toLowerCase() + '">' + item.status + '</span></div>' +
      '<div class="appointment-meta">' +
      meta('Assigned Staff', item.staff) + meta('Service Name', item.service) + meta('Appointment Date', formatDate(item.date)) + meta('Appointment Time', item.time) + meta('Duration', item.duration) + meta('Location', item.location) +
      '</div><p class="appointment-notes">' + item.notes + '</p>' +
      '<div class="card-actions"><button class="action-button action-button--view" type="button" title="View"><i class="fa-solid fa-eye"></i></button><button class="action-button action-button--edit" type="button" title="Edit"><i class="fa-solid fa-pen"></i></button><button class="action-button action-button--delete" type="button" title="Delete"><i class="fa-solid fa-trash"></i></button></div>' +
      '</article>';
  }

  function meta(label, value) {
    return '<div><p class="meta-label">' + label + '</p><p class="meta-value">' + value + '</p></div>';
  }

  function renderCalendar() {
    var weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    var year = visibleMonth.getFullYear();
    var month = visibleMonth.getMonth();
    var firstDay = new Date(year, month, 1).getDay();
    var daysInMonth = new Date(year, month + 1, 0).getDate();
    var html = weekdays.map(function (day) { return '<div class="calendar-weekday">' + day + '</div>'; }).join('');

    els.month.textContent = visibleMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

    for (var i = 0; i < firstDay; i += 1) {
      html += '<div class="calendar-day is-empty"></div>';
    }

    for (var dayNumber = 1; dayNumber <= daysInMonth; dayNumber += 1) {
      var key = year + '-' + String(month + 1).padStart(2, '0') + '-' + String(dayNumber).padStart(2, '0');
      var dayAppointments = appointments.filter(function (item) { return item.date === key; });
      html += '<button class="calendar-day' + (key === todayKey ? ' is-today' : '') + (key === selectedDate ? ' is-selected' : '') + '" type="button" data-date="' + key + '"><span class="day-number">' + dayNumber + '</span>' + dayAppointments.map(function () { return '<span class="appointment-dot"></span>'; }).join('') + '</button>';
    }

    els.grid.innerHTML = html;
    els.grid.querySelectorAll('[data-date]').forEach(function (button) {
      button.addEventListener('click', function () {
        selectedDate = button.dataset.date === selectedDate ? '' : button.dataset.date;
        els.date.value = selectedDate;
        renderCalendar();
        renderAppointments();
      });
    });
  }

  function renderSchedulePanel() {
    document.querySelector('[data-schedule-today]').innerHTML = scheduleItems('2026-07-09');
    document.querySelector('[data-schedule-tomorrow]').innerHTML = scheduleItems('2026-07-10');
    document.querySelector('[data-schedule-week]').innerHTML = appointments.slice(3, 6).map(scheduleMarkup).join('');
  }

  function scheduleItems(date) {
    return appointments.filter(function (item) { return item.date === date; }).map(scheduleMarkup).join('') || '<p class="text-muted mb-0">No appointments scheduled.</p>';
  }

  function scheduleMarkup(item) {
    return '<div class="schedule-item"><span class="schedule-time">' + item.time + '</span><div><p>' + item.customer + '</p><span>' + item.service + ' · ' + item.status + '</span></div></div>';
  }

  function formatDate(value) {
    return new Date(value + 'T00:00:00').toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}());
