(async function () {
  await mountShell('reports');

  document.getElementById('content').innerHTML = `
    <div class="page-header">
      <div>
        <h1>Reports Dashboard</h1>
        <div class="text-secondary">Track leads, conversion, revenue, staff performance & sources</div>
      </div>
      <div class="report-filters">
        <select id="range">
          <option value="7">Last 7 days</option>
          <option value="14" selected>Last 14 days</option>
          <option value="30">Last 30 days</option>
        </select>
        <select id="months">
          <option value="3">Last 3 months</option>
          <option value="6" selected>Last 6 months</option>
          <option value="12">Last 12 months</option>
        </select>
      </div>
    </div>

    <div class="stats-grid" id="statsGrid"></div>

    <div class="charts-grid">
      <div class="chart-card">
        <h3>Daily Leads</h3>
        <canvas id="dailyChart" height="120"></canvas>
      </div>
      <div class="chart-card">
        <h3>Lead Sources</h3>
        <canvas id="sourceChart" height="120"></canvas>
      </div>
    </div>

    <div class="chart-card" style="margin-top:16px;">
      <h3>Monthly Revenue</h3>
      <canvas id="revChart" height="80"></canvas>
    </div>

    <div class="table-wrap" style="margin-top:16px;">
      <div class="table-toolbar"><h3 style="margin:0">Staff Performance</h3></div>
      <table class="data" id="perfTable">
        <thead>
          <tr><th>Staff</th><th>Assigned</th><th>Completed</th><th>Performance</th></tr>
        </thead>
        <tbody><tr><td colspan="4" class="empty-state">Loading…</td></tr></tbody>
      </table>
    </div>`;

  let dailyChart, sourceChart, revChart;

  async function loadAll() {
    try {
      const overview = await API.get('/api/reports');
      const conv     = await API.get('/api/reports/conversion');
      document.getElementById('statsGrid').innerHTML = [
        ['📥','Total Leads',      overview.totalLeads],
        ['✅','Completed',        overview.completedJobs],
        ['⏱️','Pending',          overview.pendingJobs],
        ['📈','Conversion Rate', conv.conversionRate + '%']
      ].map(([i,l,v]) => `
        <div class="stat-card">
          <div class="stat-icon">${i}</div>
          <div class="stat-body"><div class="label">${l}</div><div class="value">${esc(v)}</div></div>
        </div>`).join('');

      const days = document.getElementById('range').value;
      const months = document.getElementById('months').value;
      const daily   = await API.get('/api/reports/daily?days=' + days);
      const monthly = await API.get('/api/reports/monthly?months=' + months);
      const sources = await API.get('/api/reports/sources');
      const perf    = await API.get('/api/staff/performance');

      if (dailyChart) dailyChart.destroy();
      dailyChart = new Chart(document.getElementById('dailyChart'), {
        type: 'bar',
        data: {
          labels: daily.map(d => d.date),
          datasets: [{ label: 'Leads', data: daily.map(d => d.count),
            backgroundColor: '#6C5CE7', borderRadius: 4 }]
        },
        options: { plugins: { legend: { display: false } } }
      });

      if (sourceChart) sourceChart.destroy();
      sourceChart = new Chart(document.getElementById('sourceChart'), {
        type: 'pie',
        data: {
          labels: sources.map(s => s.source),
          datasets: [{
            data: sources.map(s => s.count),
            backgroundColor: ['#6C5CE7','#9B8CFF','#3B82F6','#14B8A6','#F59E0B','#EF4444']
          }]
        },
        options: { plugins: { legend: { position: 'bottom' } } }
      });

      if (revChart) revChart.destroy();
      revChart = new Chart(document.getElementById('revChart'), {
        type: 'line',
        data: {
          labels: monthly.map(m => m.month),
          datasets: [{
            label: 'Revenue', data: monthly.map(m => m.revenue),
            borderColor: '#22C55E', backgroundColor: 'rgba(34,197,94,.15)',
            tension: .3, fill: true
          }]
        },
        options: { plugins: { legend: { display: false } } }
      });

      const tbody = document.querySelector('#perfTable tbody');
      if (!perf.length) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-state">No staff data</td></tr>';
      } else {
        const max = Math.max(1, ...perf.map(p => p.completed));
        tbody.innerHTML = perf.map(p => `
          <tr>
            <td>${esc(p.name)}</td>
            <td>${esc(p.assigned)}</td>
            <td>${esc(p.completed)}</td>
            <td><div class="perf-bar"><span style="width:${(p.completed/max*100).toFixed(0)}%"></span></div></td>
          </tr>`).join('');
      }
    } catch (e) { toast(e.message, 'error'); }
  }

  document.getElementById('range').onchange  = loadAll;
  document.getElementById('months').onchange = loadAll;
  loadAll();
})();