(async function () {
  await mountShell('dashboard');

  function fmtMoney(v) {
    const n = Number(v || 0);
    return '₹' + n.toLocaleString('en-IN', { maximumFractionDigits: 0 });
  }

  document.getElementById('content').innerHTML = `
    <div class="page-header">
      <div>
        <h1>Dashboard</h1>
        <div class="text-secondary">Overview of your emergency lead performance</div>
      </div>
    </div>

    <div class="stats-grid" id="statsGrid"></div>

    <div class="charts-grid">
      <div class="chart-card">
        <h3>Daily Leads (last 14 days)</h3>
        <canvas id="dailyChart" height="120"></canvas>
      </div>
      <div class="chart-card">
        <h3>Conversion</h3>
        <canvas id="convChart" height="120"></canvas>
      </div>
    </div>

    <div class="chart-card" style="margin-top:16px;">
      <h3>Monthly Revenue (last 6 months)</h3>
      <canvas id="revenueChart" height="80"></canvas>
    </div>`;

  try {
    const overview = await API.get('/api/reports');
    const conv     = await API.get('/api/reports/conversion');
    const cards = [
      { icon:'📥', label:'Total Leads',    value: overview.totalLeads },
      { icon:'📅', label:"Today's Leads",  value: overview.todayLeads },
      { icon:'📞', label:'Bookings',       value: overview.bookings },
      { icon:'⏱️', label:'Pending Jobs',   value: overview.pendingJobs },
      { icon:'✅', label:'Completed Jobs', value: overview.completedJobs },
      { icon:'❌', label:'Lost Leads',     value: overview.lostLeads },
      { icon:'💰', label:'Revenue (Month)', value: fmtMoney(overview.monthRevenue) },
      { icon:'🏦', label:'Revenue (Total)', value: fmtMoney(overview.totalRevenue) }
    ];
    document.getElementById('statsGrid').innerHTML = cards.map(c => `
      <div class="stat-card">
        <div class="stat-icon">${c.icon}</div>
        <div class="stat-body">
          <div class="label">${esc(c.label)}</div>
          <div class="value">${esc(c.value)}</div>
        </div>
      </div>`).join('');

    const daily = await API.get('/api/reports/daily?days=14');
    new Chart(document.getElementById('dailyChart'), {
      type: 'line',
      data: {
        labels: daily.map(d => d.date),
        datasets: [{
          label: 'Leads',
          data: daily.map(d => d.count),
          borderColor: '#6C5CE7',
          backgroundColor: 'rgba(108,92,231,.15)',
          tension: .3, fill: true
        }]
      },
      options: { responsive: true, plugins: { legend: { display: false } } }
    });

    new Chart(document.getElementById('convChart'), {
      type: 'doughnut',
      data: {
        labels: ['Converted', 'Remaining'],
        datasets: [{
          data: [conv.conversionRate, Math.max(0, 100 - conv.conversionRate)],
          backgroundColor: ['#6C5CE7', '#E3E5EC'],
          borderWidth: 0
        }]
      },
      options: { cutout: '70%', plugins: { legend: { position: 'bottom' } } }
    });

    const monthly = await API.get('/api/reports/monthly?months=6');
    new Chart(document.getElementById('revenueChart'), {
      type: 'bar',
      data: {
        labels: monthly.map(m => m.month),
        datasets: [{
          label: 'Revenue',
          data: monthly.map(m => m.revenue),
          backgroundColor: '#9B8CFF',
          borderRadius: 6
        }]
      },
      options: { responsive: true, plugins: { legend: { display: false } } }
    });
  } catch (e) { toast(e.message, 'error'); }
})();