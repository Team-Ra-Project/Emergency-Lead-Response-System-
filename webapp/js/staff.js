(async function () {
  await mountShell('staff');

  document.getElementById('content').innerHTML = `
    <div class="page-header">
      <div>
        <h1>Staff Management</h1>
        <div class="text-secondary">Manage your team, availability, and assignments</div>
      </div>
      <a class="btn btn-primary" href="add-staff.html">+ Add Staff</a>
    </div>

    <div class="table-wrap">
      <div class="table-toolbar">
        <input id="search" placeholder="Search by name, email…" />
        <div class="filters">
          <select id="filterAvail">
            <option value="">All availability</option>
            <option value="AVAILABLE">Available</option>
            <option value="BUSY">Busy</option>
            <option value="OFF">Off</option>
          </select>
          <button class="btn btn-secondary" id="exportBtn">Export CSV</button>
        </div>
      </div>
      <table class="data" id="staffTable">
        <thead>
          <tr>
            <th>ID</th><th>Name</th><th>Email</th><th>Phone</th>
            <th>Designation</th><th>Specialization</th>
            <th>Availability</th><th style="text-align:right">Actions</th>
          </tr>
        </thead>
        <tbody><tr><td colspan="8" class="empty-state">Loading…</td></tr></tbody>
      </table>
    </div>`;

  let all = [];
  async function load() {
    try { all = await API.get('/api/staff'); render(); }
    catch (e) { toast(e.message, 'error'); }
  }

  function render() {
    const q = (document.getElementById('search').value || '').toLowerCase();
    const av = document.getElementById('filterAvail').value;
    const rows = all.filter(s =>
      (!q || (s.fullName + s.email + (s.phone||'')).toLowerCase().includes(q)) &&
      (!av || s.availability === av)
    );
    const tbody = document.querySelector('#staffTable tbody');
    if (rows.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No staff found</td></tr>'; return;
    }
    tbody.innerHTML = rows.map(s => `
      <tr>
        <td>#${esc(s.staffId)}</td>
        <td>${esc(s.fullName)}</td>
        <td>${esc(s.email)}</td>
        <td>${esc(s.phone || '-')}</td>
        <td>${esc(s.designation || '-')}</td>
        <td>${esc(s.specialization || '-')}</td>
        <td>${badge(s.availability)}</td>
        <td>
          <div class="row-actions">
            <button class="btn btn-icon" title="Edit"   data-edit="${s.staffId}">✏️</button>
            <button class="btn btn-icon" title="Delete" data-del="${s.staffId}">🗑️</button>
          </div>
        </td>
      </tr>`).join('');

    tbody.querySelectorAll('[data-del]').forEach(b => b.onclick = async () => {
      if (!confirm('Delete this staff member?')) return;
      try { await API.del('/api/staff/' + b.dataset.del); toast('Deleted','success'); load(); }
      catch (e) { toast(e.message,'error'); }
    });
    tbody.querySelectorAll('[data-edit]').forEach(b => b.onclick = () => editStaff(+b.dataset.edit));
  }

  function badge(av) {
    const cls = av === 'AVAILABLE' ? 'badge-completed'
             : av === 'BUSY'      ? 'badge-contacted'
             : 'badge-pending';
    return `<span class="badge ${cls}">${esc(av || 'N/A')}</span>`;
  }

  async function editStaff(id) {
    const s = all.find(x => x.staffId === id);
    if (!s) return;
    const designation   = prompt('Designation:', s.designation || '');
    if (designation === null) return;
    const specialization = prompt('Specialization:', s.specialization || '');
    if (specialization === null) return;
    const availability   = prompt('Availability (AVAILABLE / BUSY / OFF):', s.availability || 'AVAILABLE');
    if (availability === null) return;
    try {
      await API.put('/api/staff/' + id, { designation, specialization, availability });
      toast('Updated','success'); load();
    } catch (e) { toast(e.message,'error'); }
  }

  document.getElementById('search').addEventListener('input', render);
  document.getElementById('filterAvail').addEventListener('change', render);
  document.getElementById('exportBtn').onclick = () => {
    const hdr = ['ID','Name','Email','Phone','Designation','Specialization','Availability'];
    const rows = all.map(s => [s.staffId,s.fullName,s.email,s.phone,s.designation,s.specialization,s.availability]);
    const csv = [hdr, ...rows].map(r => r.map(v => `"${(v??'').toString().replace(/"/g,'""')}"`).join(',')).join('\n');
    const url = URL.createObjectURL(new Blob([csv],{type:'text/csv'}));
    const a = document.createElement('a'); a.href = url; a.download = 'staff.csv'; a.click();
    URL.revokeObjectURL(url);
  };

  load();
})();