/* dashboard.html */

async function loadDashboard() {
  const user = await initLayout('dashboard', 'Dashboard');
  document.getElementById('welcome').textContent = 'Welcome back, ' + user.fullName.split(' ')[0] + ' 👋';

  try {
    const stats = await api('/api/dashboard/stats');
    showNumbers(stats);
    showRecentTickets(stats.recentTickets);
    showDonut(stats);
    renderBars('priority-bars', stats.byPriority, PRIORITY_LABELS, PRIORITY_COLORS);
    showDevelopers(stats.byDeveloper);
  } catch (error) {
    toast(error.message, 'error');
  }
}

function showNumbers(stats) {
  document.getElementById('stat-total').textContent = stats.total;
  document.getElementById('stat-open').textContent = stats.open;
  document.getElementById('stat-progress').textContent = stats.inProgress;
  document.getElementById('stat-resolved').textContent = stats.resolved;
}

function showRecentTickets(tickets) {
  const body = document.getElementById('recent-body');
  if (tickets.length === 0) {
    body.innerHTML = '<tr><td colspan="5"><div class="empty-state"><i class="bi bi-inbox"></i>No tickets yet. Report the first bug!</div></td></tr>';
    return;
  }

  body.innerHTML = tickets.map(function (t) {
    const assignee = t.assignee
      ? '<span class="d-inline-flex align-items-center gap-2">' + avatar(t.assignee, 'sm') + esc(t.assignee.fullName) + '</span>'
      : '<span class="text-muted">Unassigned</span>';
    return '<tr>' +
      '<td><a class="ticket-code" href="ticket.html?id=' + t.id + '">' + esc(t.ticketCode) + '</a></td>' +
      '<td class="title-cell"><a class="title-link" href="ticket.html?id=' + t.id + '">' + esc(t.title) + '</a>' +
      '<span class="sub">' + esc(timeAgo(t.createdAt)) + ' by ' + esc(t.reporter.fullName) + '</span></td>' +
      '<td>' + priorityPill(t.priority) + '</td>' +
      '<td>' + statusPill(t.status) + '</td>' +
      '<td class="nowrap">' + assignee + '</td></tr>';
  }).join('');
}

function showDonut(stats) {
  // build a CSS conic-gradient: one coloured slice per status
  let gradient = [];
  let start = 0;
  Object.keys(stats.byStatus).forEach(function (status) {
    const share = stats.total === 0 ? 0 : stats.byStatus[status] / stats.total * 100;
    gradient.push(STATUS_COLORS[status] + ' ' + start + '% ' + (start + share) + '%');
    start += share;
  });

  const donut = document.getElementById('donut');
  donut.style.background = stats.total === 0 ? '#eceef6' : 'conic-gradient(' + gradient.join(', ') + ')';
  document.getElementById('donut-total').textContent = stats.total;

  document.getElementById('donut-legend').innerHTML = Object.keys(stats.byStatus).map(function (status) {
    return '<div class="legend-row"><span class="legend-dot" style="background:' + STATUS_COLORS[status] + '"></span>' +
      STATUS_LABELS[status] + '<span class="count">' + stats.byStatus[status] + '</span></div>';
  }).join('');
}

function showDevelopers(byDeveloper) {
  if (Object.keys(byDeveloper).length === 0) {
    document.getElementById('developer-bars').innerHTML = '<div class="text-muted">No developers yet.</div>';
    return;
  }
  renderBars('developer-bars', byDeveloper, null, null);
}

loadDashboard();
