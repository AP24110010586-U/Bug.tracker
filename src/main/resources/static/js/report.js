/* report.html - the "Report a Bug" form */

const bugForm = document.getElementById('bug-form');
const titleInput = document.getElementById('title');
const descriptionInput = document.getElementById('description');
const prioritySelect = document.getElementById('priority');
const severitySelect = document.getElementById('severity');
const formError = document.getElementById('form-error');
const submitButton = document.getElementById('submit-btn');

async function startPage() {
  await initLayout('report', 'Report a Bug');

  prioritySelect.innerHTML = optionsHtml(PRIORITY_LABELS, 'MEDIUM');
  severitySelect.innerHTML = optionsHtml(SEVERITY_LABELS, 'MODERATE');

  titleInput.addEventListener('input', function () {
    document.getElementById('title-count').textContent = titleInput.value.length;
  });

  bugForm.addEventListener('submit', submitBug);
}

async function submitBug(event) {
  event.preventDefault();
  formError.classList.add('d-none');

  const payload = {
    title: titleInput.value.trim(),
    description: descriptionInput.value.trim(),
    priority: prioritySelect.value,
    severity: severitySelect.value
  };

  // simple checks in the browser (the server checks again)
  if (!payload.title) { return showError('Please enter a title.'); }
  if (!payload.description) { return showError('Please describe the bug.'); }

  submitButton.disabled = true;
  try {
    const ticket = await api('/api/tickets', 'POST', payload);
    setFlash('Bug reported! Your ticket number is ' + ticket.ticketCode);
    window.location.href = 'ticket.html?id=' + ticket.id;
  } catch (error) {
    showError(error.message);
    submitButton.disabled = false;
  }
}

function showError(message) {
  formError.textContent = message;
  formError.classList.remove('d-none');
}

startPage();
