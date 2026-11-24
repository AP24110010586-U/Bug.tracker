/* tickets.html - table with search, filters, edit and delete */

let allLoadedTickets = []; // the tickets currently shown (used by the edit / delete buttons)
let searchTimer = null;

const searchInput = document.getElementById('f-search');
const statusFilter = document.getElementById('f-status');
const priorityFilter = document.getElementById('f-priority');
const severityFilter = document.getElementById('f-severity');
const assigneeFilter = document.getElementById('f-assignee');
const mineButton = document.getElementById('f-mine');

async function startPage() {
  const user = await initLayout('tickets', 'All Tickets');

  // fill the filter drop-downs
  statusFilter.insertAdjacentHTML('beforeend', optionsHtml(STATUS_LABELS, ''));
  priorityFilter.insertAdjacentHTML('beforeend', optionsHtml(PRIORITY_LABELS, ''));
  severityFilter.insertAdjacentHTML('beforeend', optionsHtml(SEVERITY_LABELS, ''));

  try {
    const developers = await api('/api/users?role=DEVELOPER');
    developers.forEach(function (dev) {
      assigneeFilter.insertAdjacentHTML('beforeend', '<option value="' + dev.id + '">' + esc(dev.fullName) + '</option>');
    });
  } catch (error) {
    toast(error.message, 'error');
  }

  // search text may come from the top-bar search box (?search=...)
  const initialSearch = getQueryParam('search');
  if (initialSearch) {
    searchInput.value = initialSearch;
  }

  // reload the table whenever a filter changes
  [statusFilter, priorityFilter, severityFilter, assigneeFilter].forEach(function (select) {
    select.addEventListener('change', loadTickets);
  });
  searchInput.addEventListener('input', function () {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(loadTickets, 300); // wait until the user stops typing
  });
  mineButton.addEventListener('click', function () {
    assigneeFilter.value = String(user.id);
    if (assigneeFilter.value !== String(user.id)) {
      toast('Only developers have tickets assigned to them.', 'error');
      assigneeFilter.value = '';
    }
    loadTickets();
  });
  document.getElementById('f-clear').addEventListener('click', function () {
    searchInput.value = '';
    statusFilter.value = '';
    priorityFilter.value = '';
    severityFilter.value = '';
    assigneeFilter.value = '';
    loadTickets();
  });

  loadTickets();
}

async function loadTickets() {
  // build the URL with only the filters that have a value
  const params = new URLSearchParams();
  if (searchInput.value.trim()) params.set('search', searchInput.value.trim());
  if (statusFilter.value) params.set('status', statusFilter.value);
  if (priorityFilter.value) params.set('priority', priorityFilter.value);
  if (severityFilter.value) params.set('severity', severityFilter.value);
  if (assigneeFilter.value !== '') params.set('assigneeId', assigneeFilter.value);

  try {
    allLoadedTickets = await api('/api/tickets' + (params.toString() ? '?' + params.toString() : ''));
    renderTable();
  } catch (error) {
    document.getElementById('tickets-body').innerHTML =
      '<tr><td colspan="8"><div class="empty-state"><i class="bi bi-wifi-off"></i>' + esc(error.message) + '</div></td></tr>';
  }
}

function renderTable() {
  const body = document.getElementById('tickets-body');
  document.getElementById('result-count').textContent = '(' + allLoadedTickets.length + ')';

  if (allLoadedTickets.length === 0) {
    body.innerHTML = '<tr><td colspan="8"><div class="empty-state"><i class="bi bi-search"></i>No tickets match your search.</div></td></tr>';
    return;
  }

  body.innerHTML = allLoadedTickets.map(function (t) {
    const assignee = t.assignee
      ? '<span class="d-inline-flex align-items-center gap-2">' + avatar(t.assignee, 'sm') + esc(t.assignee.fullName) + '</span>'
      : '<span class="text-muted">Unassigned</span>';

    // Edit / delete buttons are only shown when the server says this user may use them.
    let actions = '<a class="btn-icon" href="ticket.html?id=' + t.id + '" title="View details"><i class="bi bi-eye"></i></a> ';
    if (t.canEdit) {
      actions += '<button class="btn-icon" data-action="edit" data-id="' + t.id + '" title="Edit"><i class="bi bi-pencil"></i></button> ';
    }
    if (t.canDelete) {
      actions += '<button class="btn-icon danger" data-action="delete" data-id="' + t.id + '" title="Delete"><i class="bi bi-trash3"></i></button>';
    }

    return '<tr>' +
      '<td><a class="ticket-code" href="ticket.html?id=' + t.id + '">' + esc(t.ticketCode) + '</a></td>' +
      '<td class="title-cell"><a class="title-link" href="ticket.html?id=' + t.id + '">' + esc(t.title) + '</a>' +
      '<span class="sub">Reported by ' + esc(t.reporter.fullName) + '</span></td>' +
      '<td>' + priorityPill(t.priority) + '</td>' +
      '<td>' + severityPill(t.severity) + '</td>' +
      '<td>' + statusPill(t.status) + '</td>' +
      '<td class="nowrap">' + assignee + '</td>' +
      '<td class="text-muted nowrap">' + esc(formatDate(t.createdAt)) + '</td>' +
      '<td class="actions">' + actions + '</td></tr>';
  }).join('');
}

// One click listener for all edit / delete buttons in the table
document.getElementById('tickets-body').addEventListener('click', function (event) {
  const button = event.target.closest('button[data-action]');
  if (!button) return;

  const ticket = allLoadedTickets.find(function (t) { return t.id === Number(button.dataset.id); });
  if (!ticket) return;

  if (button.dataset.action === 'edit') {
    openEditModal(ticket, loadTickets);
  } else if (button.dataset.action === 'delete') {
    confirmDelete(ticket, loadTickets);
  }
});

startPage();
