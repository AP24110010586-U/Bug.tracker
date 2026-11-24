/* ticket.html - details of one ticket, status, assignment, comments */

const ticketId = getQueryParam('id');
let ticket = null; // the ticket currently shown

const statusSelect = document.getElementById('status-select');
const assignSelect = document.getElementById('assign-select');

async function startPage() {
  await initLayout('tickets', 'Ticket details');

  if (!ticketId) {
    return showNotFound('No ticket was selected.');
  }

  try {
    ticket = await api('/api/tickets/' + ticketId);
  } catch (error) {
    return showNotFound(error.message);
  }

  showTicket();
  await loadComments();

  document.getElementById('ticket-view').style.visibility = 'visible';
}

function showNotFound(message) {
  document.getElementById('not-found-msg').textContent = message;
  document.getElementById('not-found').classList.remove('d-none');
}

/* ---------- show the ticket on the page ---------- */
function showTicket() {
  document.title = ticket.ticketCode + ' - BugTracker';
  document.getElementById('d-code').textContent = ticket.ticketCode;
  document.getElementById('d-status').innerHTML = statusPill(ticket.status);
  document.getElementById('d-priority').innerHTML = priorityPill(ticket.priority);
  document.getElementById('d-severity').innerHTML = severityPill(ticket.severity);
  document.getElementById('d-title').textContent = ticket.title;
  document.getElementById('d-description').textContent = ticket.description;

  document.getElementById('m-reporter').innerHTML =
    '<span class="d-inline-flex align-items-center gap-2">' + avatar(ticket.reporter, 'sm') + esc(ticket.reporter.fullName) + '</span>';
  document.getElementById('m-assignee').innerHTML = ticket.assignee
    ? '<span class="d-inline-flex align-items-center gap-2">' + avatar(ticket.assignee, 'sm') + esc(ticket.assignee.fullName) + '</span>'
    : '<span class="text-muted">Unassigned</span>';
  document.getElementById('m-created').textContent = formatDateTime(ticket.createdAt);
  document.getElementById('m-updated').textContent = formatDateTime(ticket.updatedAt);

  showStatusControls();
  showAssignControls();
  showEditDeleteButtons();
}

function showStatusControls() {
  // every status is listed, but the ones this user may not choose are greyed out
  statusSelect.innerHTML = Object.keys(STATUS_LABELS).map(function (key) {
    const allowed = ticket.allowedStatuses.indexOf(key) !== -1;
    return '<option value="' + key + '"' + (key === ticket.status ? ' selected' : '') +
      (allowed ? '' : ' disabled') + '>' + STATUS_LABELS[key] + '</option>';
  }).join('');

  const canChange = ticket.allowedStatuses.length > 0;
  statusSelect.disabled = !canChange;
  document.getElementById('status-btn').disabled = !canChange;
  document.getElementById('status-hint').textContent = canChange ? '' :
    (currentUser.role === 'DEVELOPER'
      ? 'Only the developer assigned to this ticket can change its status.'
      : 'You cannot change the status of this ticket.');
}

async function showAssignControls() {
  const block = document.getElementById('assign-block');
  if (!ticket.canAssign) {
    block.classList.add('d-none'); // only admins see this
    return;
  }
  block.classList.remove('d-none');

  try {
    const developers = await api('/api/users?role=DEVELOPER');
    assignSelect.innerHTML = '<option value="">Unassigned</option>' + developers.map(function (dev) {
      const selected = ticket.assignee && ticket.assignee.id === dev.id ? ' selected' : '';
      return '<option value="' + dev.id + '"' + selected + '>' + esc(dev.fullName) + '</option>';
    }).join('');
  } catch (error) {
    toast(error.message, 'error');
  }
}

function showEditDeleteButtons() {
  document.getElementById('edit-btn').classList.toggle('d-none', !ticket.canEdit);
  document.getElementById('delete-btn').classList.toggle('d-none', !ticket.canDelete);
  document.getElementById('action-buttons').classList.toggle('d-none', !ticket.canEdit && !ticket.canDelete);
}

/* ---------- buttons ---------- */
document.getElementById('status-btn').addEventListener('click', async function (event) {
  const button = event.currentTarget;
  const newStatus = statusSelect.value;
  if (newStatus === ticket.status) {
    toast('The ticket already has this status.', 'error');
    return;
  }
  button.disabled = true;
  try {
    ticket = await api('/api/tickets/' + ticket.id + '/status', 'PUT', { status: newStatus });
    showTicket();
    toast('Status changed to ' + STATUS_LABELS[ticket.status]);
  } catch (error) {
    toast(error.message, 'error');
    button.disabled = false;
  }
});

document.getElementById('assign-btn').addEventListener('click', async function (event) {
  const button = event.currentTarget;
  const developerId = assignSelect.value === '' ? null : Number(assignSelect.value);
  button.disabled = true;
  try {
    ticket = await api('/api/tickets/' + ticket.id + '/assign', 'PUT', { assigneeId: developerId });
    showTicket();
    toast(ticket.assignee ? 'Assigned to ' + ticket.assignee.fullName : 'Ticket is now unassigned');
  } catch (error) {
    toast(error.message, 'error');
  }
  button.disabled = false;
});

document.getElementById('edit-btn').addEventListener('click', function () {
  openEditModal(ticket, function (updated) {
    ticket = updated;
    showTicket();
  });
});

document.getElementById('delete-btn').addEventListener('click', function () {
  confirmDelete(ticket, function () {
    setFlash('Ticket ' + ticket.ticketCode + ' was deleted');
    window.location.href = 'tickets.html';
  });
});

/* ---------- comments ---------- */
async function loadComments() {
  try {
    const comments = await api('/api/tickets/' + ticket.id + '/comments');
    document.getElementById('comment-count').textContent = '(' + comments.length + ')';

    const list = document.getElementById('comment-list');
    if (comments.length === 0) {
      list.innerHTML = '<div class="text-muted py-2">No comments yet. Start the conversation below.</div>';
      return;
    }
    list.innerHTML = comments.map(function (c) {
      return '<div class="comment">' + avatar(c.author) +
        '<div class="flex-grow-1"><div><span class="who">' + esc(c.author.fullName) + '</span>' +
        '<span class="pill role-pill ms-2">' + esc(c.author.roleLabel) + '</span>' +
        '<span class="when">' + esc(timeAgo(c.createdAt)) + '</span></div>' +
        '<div class="text">' + esc(c.content) + '</div></div></div>';
    }).join('');
  } catch (error) {
    toast(error.message, 'error');
  }
}

document.getElementById('comment-form').addEventListener('submit', async function (event) {
  event.preventDefault();
  const textBox = document.getElementById('comment-text');
  const button = document.getElementById('comment-btn');
  const content = textBox.value.trim();
  if (!content) {
    toast('Please write a comment first.', 'error');
    return;
  }

  button.disabled = true;
  try {
    await api('/api/tickets/' + ticket.id + '/comments', 'POST', { content: content });
    textBox.value = '';
    await loadComments();
    toast('Comment posted');
  } catch (error) {
    toast(error.message, 'error');
  }
  button.disabled = false;
});

startPage();
