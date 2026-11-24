/* =====================================================================
   app.js - code shared by every page
   - api()          : talks to the Java backend with fetch()
   - initLayout()   : builds the sidebar + top bar and checks that someone is logged in
   - helpers        : badges, dates, avatars, toasts, edit / delete dialogs
   ===================================================================== */

/* ---------- labels shown to the user (the backend sends the CAPITAL names) ---------- */
const STATUS_LABELS = { OPEN: 'Open', IN_PROGRESS: 'In Progress', RESOLVED: 'Resolved', CLOSED: 'Closed' };
const PRIORITY_LABELS = { LOW: 'Low', MEDIUM: 'Medium', HIGH: 'High', CRITICAL: 'Critical' };
const SEVERITY_LABELS = { MINOR: 'Minor', MODERATE: 'Moderate', MAJOR: 'Major', BLOCKER: 'Blocker' };

const STATUS_COLORS = { OPEN: '#2f80ed', IN_PROGRESS: '#f59e0b', RESOLVED: '#12a971', CLOSED: '#8b92b0' };
const PRIORITY_COLORS = { LOW: '#8b92b0', MEDIUM: '#2f80ed', HIGH: '#f08a24', CRITICAL: '#e5484d' };
const SEVERITY_COLORS = { MINOR: '#8b92b0', MODERATE: '#8266ff', MAJOR: '#f08a24', BLOCKER: '#e5484d' };

let currentUser = null; // filled in by initLayout()

/* ---------- talking to the backend ---------- */
async function api(url, method = 'GET', body = null) {
  const options = { method: method, headers: {}, credentials: 'same-origin' };
  if (body !== null) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }

  let response;
  try {
    response = await fetch(url, options);
  } catch (error) {
    throw new Error('Cannot reach the server. Is the Spring Boot application running?');
  }

  let data = null;
  try {
    data = await response.json();
  } catch (error) {
    // the response had no JSON body - that is fine
  }

  // Not logged in (or session expired) -> go to the login page
  if (response.status === 401 && !window.location.pathname.endsWith('login.html')) {
    window.location.href = 'login.html';
    return new Promise(function () { /* never finishes: the browser is leaving this page */ });
  }

  if (!response.ok) {
    throw new Error((data && data.message) || 'Request failed (' + response.status + ')');
  }
  return data;
}

/* ---------- small helpers ---------- */
function esc(value) { // makes text safe to put inside HTML (prevents HTML injection)
  if (value === null || value === undefined) return '';
  return String(value)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

/* The server sends e.g. 2026-11-24T10:15:30.123456 (6 decimals). Some browsers only accept 3, so we cut it. */
function toDate(iso) {
  return new Date(String(iso).replace(/(\.\d{3})\d+/, '$1'));
}

function formatDate(iso) {
  if (!iso) return '-';
  return toDate(iso).toLocaleDateString(undefined, { day: '2-digit', month: 'short', year: 'numeric' });
}

function formatDateTime(iso) {
  if (!iso) return '-';
  return toDate(iso).toLocaleString(undefined, { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function timeAgo(iso) {
  if (!iso) return '';
  const seconds = Math.floor((Date.now() - toDate(iso).getTime()) / 1000);
  if (seconds < 60) return 'just now';
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return minutes + ' min ago';
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return hours + ' hr ago';
  const days = Math.floor(hours / 24);
  if (days < 30) return days + ' day' + (days === 1 ? '' : 's') + ' ago';
  return formatDate(iso);
}

const AVATAR_COLORS = ['#6c4cf1', '#2f80ed', '#12a971', '#e5484d', '#f08a24', '#0f9fb5', '#c2358f'];

function avatar(user, size = '') {
  if (!user) return '';
  const parts = user.fullName.trim().split(/\s+/);
  const initials = (parts[0][0] + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase();
  const color = AVATAR_COLORS[user.id % AVATAR_COLORS.length];
  return '<span class="avatar ' + size + '" style="background:' + color + '">' + esc(initials) + '</span>';
}

function statusPill(status) { return '<span class="pill pill-' + status + '">' + esc(STATUS_LABELS[status] || status) + '</span>'; }
function priorityPill(priority) { return '<span class="pill pill-' + priority + '">' + esc(PRIORITY_LABELS[priority] || priority) + '</span>'; }
function severityPill(severity) { return '<span class="pill pill-' + severity + '">' + esc(SEVERITY_LABELS[severity] || severity) + '</span>'; }

function optionsHtml(labels, selected) {
  return Object.keys(labels).map(function (key) {
    return '<option value="' + key + '"' + (key === selected ? ' selected' : '') + '>' + labels[key] + '</option>';
  }).join('');
}

function toast(message, type = 'success') {
  let area = document.getElementById('toast-area');
  if (!area) {
    area = document.createElement('div');
    area.id = 'toast-area';
    document.body.appendChild(area);
  }
  const el = document.createElement('div');
  el.className = 'app-toast ' + type;
  el.innerHTML = '<i class="bi ' + (type === 'error' ? 'bi-exclamation-circle-fill' : 'bi-check-circle-fill') + '"></i><span>' + esc(message) + '</span>';
  area.appendChild(el);
  setTimeout(function () {
    el.classList.add('hide');
    setTimeout(function () { el.remove(); }, 350);
  }, 3800);
}

/* "flash" = a message that is shown on the NEXT page (e.g. after creating a ticket we redirect) */
function setFlash(message) {
  try { sessionStorage.setItem('flash', message); } catch (e) { /* ignore */ }
}
function showFlash() {
  try {
    const message = sessionStorage.getItem('flash');
    if (message) {
      sessionStorage.removeItem('flash');
      toast(message);
    }
  } catch (e) { /* ignore */ }
}

function getQueryParam(name) {
  return new URLSearchParams(window.location.search).get(name);
}

/* ---------- layout: sidebar + top bar ---------- */
const NAV_ITEMS = [
  { key: 'dashboard', label: 'Dashboard', icon: 'bi-grid-1x2-fill', href: 'dashboard.html' },
  { key: 'tickets', label: 'All Tickets', icon: 'bi-ticket-detailed-fill', href: 'tickets.html' },
  { key: 'report', label: 'Report a Bug', icon: 'bi-bug-fill', href: 'report.html' },
  { key: 'reports', label: 'Reports', icon: 'bi-bar-chart-line-fill', href: 'reports.html' }
];

async function initLayout(activeKey, pageTitle) {
  const user = await api('/api/auth/me');
  currentUser = user;

  const links = NAV_ITEMS.map(function (item) {
    return '<a class="side-link' + (item.key === activeKey ? ' active' : '') + '" href="' + item.href + '">' +
      '<i class="bi ' + item.icon + '"></i><span>' + item.label + '</span></a>';
  }).join('');

  document.getElementById('sidebar').innerHTML =
    '<a class="brand" href="dashboard.html">' +
    '<span class="brand-logo"><i class="bi bi-bug-fill"></i></span>' +
    '<span class="brand-name">BugTracker<small>Issue &amp; ticket tracking</small></span></a>' +
    '<div class="nav-label">Workspace</div>' + links +
    '<div class="sidebar-footer">' +
    '<div class="user-card">' + avatar(user, 'lg') +
    '<div><div class="name">' + esc(user.fullName) + '</div><div class="role">' + esc(user.roleLabel) + '</div></div></div>' +
    '<button class="logout-btn" id="logout-btn"><i class="bi bi-box-arrow-right me-2"></i>Log out</button>' +
    '</div>';

  document.getElementById('topbar').innerHTML =
    '<button class="btn btn-icon menu-btn" id="menu-btn" aria-label="Open menu"><i class="bi bi-list fs-5"></i></button>' +
    '<h1 class="page-title">' + esc(pageTitle) + '</h1>' +
    '<form class="search-box input-icon" id="global-search"><i class="bi bi-search"></i>' +
    '<input class="form-control" id="global-search-input" type="search" placeholder="Search tickets..."></form>' +
    '<a href="report.html" class="btn btn-primary btn-sm d-none d-sm-inline-flex align-items-center"><i class="bi bi-plus-lg me-1"></i> New Bug</a>';

  // mobile menu
  const sidebar = document.getElementById('sidebar');
  const backdrop = document.getElementById('sidebar-backdrop');
  function toggleMenu(open) {
    sidebar.classList.toggle('open', open);
    backdrop.classList.toggle('show', open);
  }
  document.getElementById('menu-btn').addEventListener('click', function () { toggleMenu(!sidebar.classList.contains('open')); });
  backdrop.addEventListener('click', function () { toggleMenu(false); });

  // logout
  document.getElementById('logout-btn').addEventListener('click', async function () {
    try { await api('/api/auth/logout', 'POST'); } catch (e) { /* ignore */ }
    window.location.href = 'login.html';
  });

  // global search -> opens the tickets page with the search text
  document.getElementById('global-search').addEventListener('submit', function (event) {
    event.preventDefault();
    const text = document.getElementById('global-search-input').value.trim();
    window.location.href = 'tickets.html' + (text ? '?search=' + encodeURIComponent(text) : '');
  });

  showFlash();
  return user;
}

/* ---------- reusable dialogs: edit a ticket / confirm delete ---------- */
function createModal(innerHtml) {
  const wrapper = document.createElement('div');
  wrapper.innerHTML = '<div class="modal fade" tabindex="-1" aria-hidden="true">' + innerHtml + '</div>';
  const element = wrapper.firstElementChild;
  document.body.appendChild(element);
  const modal = new bootstrap.Modal(element);
  element.addEventListener('hidden.bs.modal', function () { modal.dispose(); element.remove(); });
  modal.show();
  return { element: element, modal: modal };
}

function openEditModal(ticket, onSaved) {
  const dialog = createModal(
    '<div class="modal-dialog modal-dialog-centered modal-lg"><form class="modal-content" novalidate>' +
    '<div class="modal-header"><h5 class="modal-title">Edit ' + esc(ticket.ticketCode) + '</h5>' +
    '<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>' +
    '<div class="modal-body">' +
    '<div class="mb-3"><label class="form-label">Title</label><input class="form-control" name="title" maxlength="200" required></div>' +
    '<div class="mb-3"><label class="form-label">Description</label><textarea class="form-control" name="description" rows="6" maxlength="4000" required></textarea></div>' +
    '<div class="row g-3"><div class="col-sm-6"><label class="form-label">Priority</label><select class="form-select" name="priority">' + optionsHtml(PRIORITY_LABELS, ticket.priority) + '</select></div>' +
    '<div class="col-sm-6"><label class="form-label">Severity</label><select class="form-select" name="severity">' + optionsHtml(SEVERITY_LABELS, ticket.severity) + '</select></div></div>' +
    '<div class="text-danger small mt-3 d-none" id="edit-error"></div>' +
    '</div><div class="modal-footer">' +
    '<button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>' +
    '<button type="submit" class="btn btn-primary" id="edit-save">Save changes</button></div></form></div>');

  const form = dialog.element.querySelector('form');
  form.elements['title'].value = ticket.title;
  form.elements['description'].value = ticket.description;

  form.addEventListener('submit', async function (event) {
    event.preventDefault();
    const errorBox = dialog.element.querySelector('#edit-error');
    const saveButton = dialog.element.querySelector('#edit-save');
    errorBox.classList.add('d-none');

    const payload = {
      title: form.elements['title'].value.trim(),
      description: form.elements['description'].value.trim(),
      priority: form.elements['priority'].value,
      severity: form.elements['severity'].value
    };
    if (!payload.title || !payload.description) {
      errorBox.textContent = 'Title and description are required.';
      errorBox.classList.remove('d-none');
      return;
    }

    saveButton.disabled = true;
    try {
      const updated = await api('/api/tickets/' + ticket.id, 'PUT', payload);
      dialog.modal.hide();
      toast('Ticket ' + updated.ticketCode + ' updated');
      if (onSaved) onSaved(updated);
    } catch (error) {
      errorBox.textContent = error.message;
      errorBox.classList.remove('d-none');
      saveButton.disabled = false;
    }
  });
}

function confirmDelete(ticket, onDeleted) {
  const dialog = createModal(
    '<div class="modal-dialog modal-dialog-centered"><div class="modal-content">' +
    '<div class="modal-header"><h5 class="modal-title">Delete ticket?</h5>' +
    '<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>' +
    '<div class="modal-body"><p class="mb-1">You are about to permanently delete:</p>' +
    '<p class="fw-bold mb-2">' + esc(ticket.ticketCode) + ' - ' + esc(ticket.title) + '</p>' +
    '<p class="text-muted small mb-0">All comments of this ticket will be deleted too. This cannot be undone.</p>' +
    '<div class="text-danger small mt-3 d-none" id="delete-error"></div></div>' +
    '<div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>' +
    '<button type="button" class="btn btn-danger" id="delete-confirm"><i class="bi bi-trash3 me-1"></i>Delete</button></div>' +
    '</div></div>');

  dialog.element.querySelector('#delete-confirm').addEventListener('click', async function (event) {
    const button = event.currentTarget;
    button.disabled = true;
    try {
      await api('/api/tickets/' + ticket.id, 'DELETE');
      dialog.modal.hide();
      toast('Ticket ' + ticket.ticketCode + ' deleted');
      if (onDeleted) onDeleted();
    } catch (error) {
      const errorBox = dialog.element.querySelector('#delete-error');
      errorBox.textContent = error.message;
      errorBox.classList.remove('d-none');
      button.disabled = false;
    }
  });
}

/* ---------- reusable chart pieces (pure HTML/CSS, no chart library needed) ---------- */
function renderBars(containerId, dataMap, labels, colors) {
  const container = document.getElementById(containerId);
  const keys = Object.keys(dataMap);
  const max = Math.max(1, ...keys.map(function (k) { return dataMap[k]; }));

  container.innerHTML = keys.map(function (key) {
    const label = labels ? (labels[key] || key) : key;
    const color = colors ? (colors[key] || '#6c4cf1') : '#6c4cf1';
    return '<div class="bar-row"><div class="bar-label"><span>' + esc(label) + '</span><span>' + dataMap[key] + '</span></div>' +
      '<div class="bar-track"><div class="bar-fill" data-width="' + Math.round(dataMap[key] / max * 100) + '" style="background:' + color + '"></div></div></div>';
  }).join('');

  // animate the bars after they are on the page
  requestAnimationFrame(function () {
    container.querySelectorAll('.bar-fill').forEach(function (bar) { bar.style.width = bar.dataset.width + '%'; });
  });
}
