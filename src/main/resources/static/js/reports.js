/* reports.html - statistics and CSV export */

async function startPage() {
  await initLayout('reports', 'Reports');

  try {
    const stats = await api('/api/dashboard/stats');

    document.getElementById('r-total').textContent = stats.total;
    document.getElementById('r-rate').textContent = stats.resolutionRate + '%';
    document.getElementById('r-unassigned').textContent = stats.unassigned;
    document.getElementById('r-closed').textContent = stats.closed;

    renderBars('r-status', stats.byStatus, STATUS_LABELS, STATUS_COLORS);
    renderBars('r-priority', stats.byPriority, PRIORITY_LABELS, PRIORITY_COLORS);
    renderBars('r-severity', stats.bySeverity, SEVERITY_LABELS, SEVERITY_COLORS);

    if (Object.keys(stats.byDeveloper).length === 0) {
      document.getElementById('r-developer').innerHTML = '<div class="text-muted">No developers yet.</div>';
    } else {
      renderBars('r-developer', stats.byDeveloper, null, null);
    }
  } catch (error) {
    toast(error.message, 'error');
  }
}

/* ---------- Export CSV: download all tickets as a spreadsheet file ---------- */
function csvCell(value) {
  const text = value === null || value === undefined ? '' : String(value);
  return '"' + text.replace(/"/g, '""') + '"'; // wrap in quotes, double any quote inside
}

document.getElementById('export-btn').addEventListener('click', async function () {
  try {
    const tickets = await api('/api/tickets');
    const rows = [['Ticket ID', 'Title', 'Description', 'Priority', 'Severity', 'Status', 'Reporter', 'Assignee', 'Created', 'Updated']];

    tickets.forEach(function (t) {
      rows.push([
        t.ticketCode, t.title, t.description,
        PRIORITY_LABELS[t.priority], SEVERITY_LABELS[t.severity], STATUS_LABELS[t.status],
        t.reporter.fullName, t.assignee ? t.assignee.fullName : 'Unassigned',
        t.createdAt, t.updatedAt
      ]);
    });

    const csv = rows.map(function (row) { return row.map(csvCell).join(','); }).join('\r\n');
    const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'bug-report-' + new Date().toISOString().slice(0, 10) + '.csv';
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(link.href);
    toast('Exported ' + tickets.length + ' tickets');
  } catch (error) {
    toast(error.message, 'error');
  }
});

startPage();
