const state = { page: 'dashboard', books: [], students: [], loans: [], returns: [], fines: [], payments: [], report: null, returnTab: 'active' };
const pages = {
  dashboard: ['Dashboard', 'LIBRARY OVERVIEW'], books: ['Books', 'COLLECTION'], students: ['Students', 'MEMBERS'],
  issues: ['Issue books', 'CIRCULATION'], returns: ['Returns & fines', 'CIRCULATION'], reports: ['Reports', 'INSIGHTS'], settings: ['Settings', 'POLICY']
};
const content = document.querySelector('#pageContent');
const toastElement = document.querySelector('#toast');
let toastTimer;

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[character]);
}

async function api(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  const response = await fetch(path, { ...options, headers, credentials: 'same-origin' });
  const type = response.headers.get('content-type') || '';
  const body = type.includes('application/json') ? await response.json() : await response.text();
  if (!response.ok) {
    if (response.status === 401 && !path.startsWith('/api/auth/')) showLogin(false);
    throw new Error(body?.error || body?.message || `Request failed (${response.status})`);
  }
  return body;
}

function notify(message, error = false) {
  toastElement.textContent = message;
  toastElement.classList.toggle('error', error);
  toastElement.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toastElement.classList.remove('show'), 3200);
}

function showLogin(setupRequired) {
  document.querySelector('#appView').hidden = true;
  document.querySelector('#loginView').hidden = false;
  document.querySelector('#confirmField').hidden = !setupRequired;
  document.querySelector('#authTitle').textContent = setupRequired ? 'Create administrator' : 'Administrator sign in';
  document.querySelector('#authEyebrow').textContent = setupRequired ? 'FIRST-TIME SETUP' : 'WELCOME BACK';
  document.querySelector('#authDescription').textContent = setupRequired
    ? 'Create the first administrator account to secure this library.'
    : 'Sign in to continue to your library workspace.';
  document.querySelector('#authSubmit').textContent = setupRequired ? 'Create account' : 'Sign in';
  document.querySelector('#authError').textContent = '';
  document.querySelector('#authForm').dataset.setup = String(setupRequired);
}

function showWorkspace(username) {
  document.querySelector('#loginView').hidden = true;
  document.querySelector('#appView').hidden = false;
  document.querySelector('#signedInName').textContent = username;
  document.querySelector('#todayLabel').textContent = new Intl.DateTimeFormat(undefined, { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' }).format(new Date());
  navigate('dashboard');
}

function table(headers, rows, emptyText = 'No records found.') {
  if (!rows.length) return `<div class="empty">${escapeHtml(emptyText)}</div>`;
  return `<div class="table-wrap"><table><thead><tr>${headers.map(header => `<th>${escapeHtml(header)}</th>`).join('')}</tr></thead><tbody>${rows.join('')}</tbody></table></div>`;
}

function statusPill(status, overdue = false) {
  const value = String(status || '');
  const warning = overdue || ['PENDING', 'PARTIAL', 'OVERDUE'].includes(value.toUpperCase());
  return `<span class="pill ${warning ? 'warn' : value.toUpperCase() === 'PAID' ? '' : 'neutral'}">${escapeHtml(value)}</span>`;
}

function panel(title, subtitle, body, actions = '') {
  return `<section class="panel"><div class="panel-head"><div><h3>${escapeHtml(title)}</h3><p>${escapeHtml(subtitle)}</p></div>${actions ? `<div class="toolbar">${actions}</div>` : ''}</div>${body}</section>`;
}

function setHeader(page) {
  document.querySelector('#pageTitle').textContent = pages[page][0];
  document.querySelector('#pageKicker').textContent = pages[page][1];
  document.querySelectorAll('.nav-item').forEach(button => button.classList.toggle('active', button.dataset.page === page));
}

async function navigate(page) {
  state.page = page;
  setHeader(page);
  content.innerHTML = '<div class="loading">Loading library data…</div>';
  try {
    if (page === 'dashboard') await renderDashboard();
    if (page === 'books') await renderBooks();
    if (page === 'students') await renderStudents();
    if (page === 'issues') await renderIssues();
    if (page === 'returns') await renderReturns();
    if (page === 'reports') await renderReports();
    if (page === 'settings') await renderSettings();
  } catch (error) {
    content.innerHTML = `<section class="panel"><div class="empty">${escapeHtml(error.message)}</div></section>`;
  }
}

async function renderDashboard() {
  const [stats, loans] = await Promise.all([api('/api/dashboard'), api('/api/loans')]);
  const metrics = [
    ['Catalog titles', stats.totalBooks], ['Available copies', stats.availableCopies], ['Currently issued', stats.issuedBooks],
    ['Registered students', stats.students], ['Overdue loans', stats.overdueBooks, true],
    ['Fines collected', new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(stats.finesCollected)]
  ];
  const cards = metrics.map(([label, value, warn]) => `<article class="metric"><div class="metric-label">${escapeHtml(label)}</div><div class="metric-value ${warn ? 'warn' : ''}">${escapeHtml(value)}</div></article>`).join('');
  const rows = loans.slice(0, 8).map(item => {
    const late = item.dueOn < new Date().toISOString().slice(0, 10);
    return `<tr><td>${escapeHtml(item.issueId)}</td><td>${escapeHtml(item.studentName)}</td><td>${escapeHtml(item.bookTitle)}</td><td>${escapeHtml(item.dueOn)}</td><td>${statusPill(late ? 'OVERDUE' : 'ACTIVE', late)}</td></tr>`;
  });
  content.innerHTML = `<div class="page-intro"><div><h2>At a glance</h2><p>Live totals from your library database.</p></div><button class="button quiet" data-action="refresh">Refresh</button></div><div class="metric-grid">${cards}</div>${panel('Due soon and overdue', 'Active loans ordered by due date', table(['Issue', 'Student', 'Book', 'Due date', 'Status'], rows, 'No active loans.'))}`;
}

async function renderBooks(query = '', sort = 'title') {
  state.books = await api(`/api/books?q=${encodeURIComponent(query)}&sort=${encodeURIComponent(sort)}`);
  const rows = state.books.map(book => `<tr><td>${escapeHtml(book.id)}</td><td>${escapeHtml(book.isbn)}</td><td>${escapeHtml(book.title)}</td><td>${escapeHtml(book.author)}</td><td>${escapeHtml(book.category)}</td><td>${escapeHtml(book.price)}</td><td>${escapeHtml(book.quantity)}</td><td>${escapeHtml(book.availableQuantity)}</td><td><button class="button quiet small" data-action="edit-book" data-id="${escapeHtml(book.id)}">Edit</button> <button class="button danger small" data-action="delete-book" data-id="${escapeHtml(book.id)}">Delete</button></td></tr>`);
  content.innerHTML = `<div class="page-intro"><div><h2>Collection</h2><p>Search and maintain books and copy counts.</p></div><button class="button primary" data-action="add-book">Add book</button></div>${panel('Book catalog', 'Search by title, author, ISBN or book ID', `<div class="table-wrap">${table(['ID', 'ISBN', 'Title', 'Author', 'Category', 'Price', 'Copies', 'Available', 'Actions'], rows)}</div>`, `<input id="bookSearch" placeholder="Search catalog" value="${escapeHtml(query)}"><select id="bookSort"><option value="title" ${sort === 'title' ? 'selected' : ''}>Title A–Z</option><option value="author" ${sort === 'author' ? 'selected' : ''}>Author</option><option value="available" ${sort === 'available' ? 'selected' : ''}>Availability</option><option value="newest" ${sort === 'newest' ? 'selected' : ''}>Newest</option></select><button class="button quiet" data-action="search-books">Search</button>`)}`;
}

async function renderStudents(query = '') {
  state.students = await api(`/api/students?q=${encodeURIComponent(query)}`);
  const rows = state.students.map(student => `<tr><td>${escapeHtml(student.id)}</td><td>${escapeHtml(student.name)}</td><td>${escapeHtml(student.email)}</td><td>${escapeHtml(student.phone)}</td><td>${escapeHtml(student.department)}</td><td>${escapeHtml(student.academicYear)}</td><td><button class="button quiet small" data-action="edit-student" data-id="${escapeHtml(student.id)}">Edit</button> <button class="button danger small" data-action="delete-student" data-id="${escapeHtml(student.id)}">Delete</button></td></tr>`);
  content.innerHTML = `<div class="page-intro"><div><h2>Student directory</h2><p>Manage registered borrowers and contact details.</p></div><button class="button primary" data-action="add-student">Add student</button></div>${panel('Registered students', 'Search by name, student ID or email', table(['Student ID', 'Name', 'Email', 'Phone', 'Department', 'Year', 'Actions'], rows), `<input id="studentSearch" placeholder="Search students" value="${escapeHtml(query)}"><button class="button quiet" data-action="search-students">Search</button>`)}`;
}

async function renderIssues(query = '', overdueOnly = false) {
  const [loans, students, availableBooks] = await Promise.all([
    api(`/api/loans?q=${encodeURIComponent(query)}&overdueOnly=${overdueOnly}`), api('/api/students'), api('/api/books?sort=title')
  ]);
  state.loans = loans;
  const options = students.map(student => `<option value="${escapeHtml(student.id)}">${escapeHtml(student.name)} · ${escapeHtml(student.id)}</option>`).join('');
  const books = availableBooks.filter(book => book.availableQuantity > 0).map(book => `<option value="${escapeHtml(book.id)}">${escapeHtml(book.title)} · ${escapeHtml(book.availableQuantity)} available</option>`).join('');
  const rows = loans.map(item => {
    const late = item.dueOn < new Date().toISOString().slice(0, 10);
    return `<tr><td>${escapeHtml(item.issueId)}</td><td>${escapeHtml(item.studentId)}</td><td>${escapeHtml(item.studentName)}</td><td>${escapeHtml(item.bookTitle)}</td><td>${escapeHtml(item.issuedOn)}</td><td>${escapeHtml(item.dueOn)}</td><td>${statusPill(late ? 'OVERDUE' : 'ACTIVE', late)}</td></tr>`;
  });
  const issueForm = `<form id="issueForm" class="toolbar issue-form"><select name="studentId" required><option value="">Choose student</option>${options}</select><select name="bookId" required><option value="">Choose available book</option>${books}</select><button class="button primary" type="submit">Issue book</button></form>`;
  content.innerHTML = `<div class="page-intro"><div><h2>Circulation desk</h2><p>Issue a copy and track its due date.</p></div></div>${panel('New loan', 'Due dates use the configured loan period', issueForm)}${panel('Active loans', 'Filter by student, book or issue ID', table(['Issue ID', 'Student ID', 'Student', 'Book', 'Issued', 'Due', 'Status'], rows, 'No active loans.'), `<input id="loanSearch" placeholder="Search active loans" value="${escapeHtml(query)}"><label class="inline-check"><input id="overdueOnly" type="checkbox" ${overdueOnly ? 'checked' : ''}>Overdue only</label><button class="button quiet" data-action="search-loans">Filter</button>`)}`;
  document.querySelector('#issueForm').addEventListener('submit', async event => {
    event.preventDefault(); const data = Object.fromEntries(new FormData(event.currentTarget));
    try { const result = await api('/api/loans', { method: 'POST', body: JSON.stringify({ studentId: data.studentId, bookId: Number(data.bookId) }) }); notify(`Loan created · issue ${result.issueId}`); renderIssues(); }
    catch (error) { notify(error.message, true); }
  });
}

async function renderReturns(query = '') {
  const [loans, returned, fines, payments] = await Promise.all([
    api(`/api/loans?q=${encodeURIComponent(query)}`), api(`/api/returns?q=${encodeURIComponent(query)}`), api(`/api/fines?q=${encodeURIComponent(query)}&status=All`), api('/api/payments')
  ]);
  state.loans = loans; state.returns = returned; state.fines = fines; state.payments = payments;
  const activeRows = loans.map(item => `<tr><td>${escapeHtml(item.issueId)}</td><td>${escapeHtml(item.studentName)}</td><td>${escapeHtml(item.bookTitle)}</td><td>${escapeHtml(item.dueOn)}</td><td><button class="button primary small" data-action="return-book" data-id="${escapeHtml(item.issueId)}">Return</button></td></tr>`);
  const returnRows = returned.map(item => `<tr><td>${escapeHtml(item.returnId)}</td><td>${escapeHtml(item.issueId)}</td><td>${escapeHtml(item.studentName)}</td><td>${escapeHtml(item.bookTitle)}</td><td>${escapeHtml(item.returnedOn)}</td><td>${escapeHtml(item.overdueDays)}</td><td>${escapeHtml(item.fineAmount)}</td></tr>`);
  const fineRows = fines.map(item => {
    const balance = Number(item.amount) - Number(item.paidAmount);
    return `<tr><td>${escapeHtml(item.id)}</td><td>${escapeHtml(item.issueId)}</td><td>${escapeHtml(item.studentName)}</td><td>${escapeHtml(item.bookTitle)}</td><td>${escapeHtml(item.amount)}</td><td>${escapeHtml(item.paidAmount)}</td><td>${balance.toFixed(2)}</td><td>${statusPill(item.status)}</td><td>${balance > 0 ? `<button class="button quiet small" data-action="pay-fine" data-id="${escapeHtml(item.id)}" data-balance="${balance.toFixed(2)}">Record payment</button>` : ''}</td></tr>`;
  });
  const paymentRows = payments.map(item => `<tr><td>${escapeHtml(item.paymentId)}</td><td>${escapeHtml(item.fineId)}</td><td>${escapeHtml(item.issueId)}</td><td>${escapeHtml(item.studentName)}</td><td>${escapeHtml(item.amount)}</td><td>${escapeHtml(item.paidAt)}</td></tr>`);
  const tabBody = state.returnTab === 'active' ? table(['Issue', 'Student', 'Book', 'Due', 'Action'], activeRows, 'No active loans.')
    : state.returnTab === 'history' ? table(['Return', 'Issue', 'Student', 'Book', 'Returned', 'Late days', 'Fine'], returnRows, 'No return history.')
    : `<div class="subsection"><h3>Fine balances</h3>${table(['Fine', 'Issue', 'Student', 'Book', 'Amount', 'Paid', 'Balance', 'Status', 'Action'], fineRows, 'No fines.')}</div><div class="subsection"><h3>Payment history</h3>${table(['Payment', 'Fine', 'Issue', 'Student', 'Amount', 'Paid at'], paymentRows, 'No payments recorded.')}</div>`;
  content.innerHTML = `<div class="page-intro"><div><h2>Returns &amp; fines</h2><p>Process returns, calculate late charges, and retain payment history.</p></div></div>${panel('Circulation records', 'Returns use the current daily fine setting', `<div class="tabs"><button class="tab-button ${state.returnTab === 'active' ? 'active' : ''}" data-action="return-tab" data-tab="active">Active loans</button><button class="tab-button ${state.returnTab === 'history' ? 'active' : ''}" data-action="return-tab" data-tab="history">Return history</button><button class="tab-button ${state.returnTab === 'fines' ? 'active' : ''}" data-action="return-tab" data-tab="fines">Fines &amp; payments</button></div>${tabBody}`, `<input id="returnSearch" placeholder="Search records" value="${escapeHtml(query)}"><button class="button quiet" data-action="search-returns">Search</button>`)}`;
}

async function renderReports(type = 'All books') {
  const report = await api(`/api/reports?type=${encodeURIComponent(type)}`);
  state.report = report; state.reportType = type;
  const rows = report.rows.map(row => `<tr>${row.map(cell => `<td>${escapeHtml(cell)}</td>`).join('')}</tr>`);
  const types = ['All books', 'Available books', 'Issued books', 'Returned books', 'Overdue books', 'Registered students', 'Fine collection'];
  const select = `<select id="reportType">${types.map(name => `<option ${type === name ? 'selected' : ''}>${escapeHtml(name)}</option>`).join('')}</select><button class="button quiet" data-action="export-report">Export CSV</button>`;
  content.innerHTML = `<div class="page-intro"><div><h2>Library reports</h2><p>Build a current report from the database and export it as CSV.</p></div></div>${panel(type, `${report.rows.length} record${report.rows.length === 1 ? '' : 's'}`, table(report.columns, rows, 'No rows in this report.'), select)}`;
}

async function renderSettings() {
  const settings = await api('/api/settings');
  content.innerHTML = `<div class="page-intro"><div><h2>Circulation policy</h2><p>These rules are used for new loans and future returns.</p></div></div><section class="panel"><div class="panel-head"><div><h3>Loan rules</h3><p>Daily fines and standard loan duration.</p></div></div><form id="settingsForm" class="settings-form"><label>Fine per overdue day<input type="number" name="finePerDay" min="0" step="0.01" required value="${escapeHtml(settings.finePerDay)}"></label><label>Loan period in days<input type="number" name="loanPeriodDays" min="1" max="365" step="1" required value="${escapeHtml(settings.loanPeriodDays)}"></label><button class="button primary" type="submit">Save settings</button></form></section>`;
  document.querySelector('#settingsForm').addEventListener('submit', async event => {
    event.preventDefault(); const form = Object.fromEntries(new FormData(event.currentTarget));
    try { await api('/api/settings', { method: 'PUT', body: JSON.stringify(form) }); notify('Circulation settings saved.'); }
    catch (error) { notify(error.message, true); }
  });
}

function showDialog(title, fields, submitLabel = 'Save changes') {
  return new Promise(resolve => {
    const dialog = document.querySelector('#formDialog'); const form = document.querySelector('#dialogForm');
    const fieldsRoot = document.querySelector('#dialogFields');
    document.querySelector('#dialogTitle').textContent = title;
    document.querySelector('#dialogSubmit').textContent = submitLabel;
    fieldsRoot.innerHTML = fields.map(field => {
      const wide = field.wide ? 'wide' : '';
      if (field.type === 'select') return `<label class="${wide}">${escapeHtml(field.label)}<select name="${escapeHtml(field.name)}" ${field.required ? 'required' : ''}>${field.options.map(option => `<option value="${escapeHtml(option.value)}" ${String(option.value) === String(field.value ?? '') ? 'selected' : ''}>${escapeHtml(option.label)}</option>`).join('')}</select></label>`;
      if (field.type === 'textarea') return `<label class="${wide}">${escapeHtml(field.label)}<textarea name="${escapeHtml(field.name)}" ${field.required ? 'required' : ''}>${escapeHtml(field.value)}</textarea></label>`;
      return `<label class="${wide}">${escapeHtml(field.label)}<input name="${escapeHtml(field.name)}" type="${escapeHtml(field.type || 'text')}" value="${escapeHtml(field.value)}" ${field.required ? 'required' : ''} ${field.min !== undefined ? `min="${field.min}"` : ''} ${field.step ? `step="${field.step}"` : ''} ${field.maxLength ? `maxlength="${field.maxLength}"` : ''} ${field.disabled ? 'disabled' : ''}></label>`;
    }).join('');
    let completed = false;
    const finish = value => { if (completed) return; completed = true; dialog.close(); resolve(value); };
    form.onsubmit = event => { event.preventDefault(); finish(Object.fromEntries(new FormData(form))); };
    document.querySelector('#dialogCancel').onclick = () => finish(null);
    document.querySelector('#dialogClose').onclick = () => finish(null);
    dialog.oncancel = event => { event.preventDefault(); finish(null); };
    dialog.showModal();
  });
}

async function addOrEditBook(book = null) {
  const fields = [
    { name: 'isbn', label: 'ISBN', value: book?.isbn, required: true }, { name: 'title', label: 'Title', value: book?.title, required: true },
    { name: 'author', label: 'Author', value: book?.author, required: true }, { name: 'publisher', label: 'Publisher', value: book?.publisher },
    { name: 'category', label: 'Category', value: book?.category }, { name: 'price', label: 'Price', type: 'number', min: '0', step: '0.01', value: book?.price ?? '0.00', required: true },
    { name: 'quantity', label: 'Total copies', type: 'number', min: '0', step: '1', value: book?.quantity ?? 1, required: true }
  ];
  const values = await showDialog(book ? 'Edit book' : 'Add book', fields);
  if (!values) return;
  const body = { ...values, price: Number(values.price), quantity: Number(values.quantity) };
  try { await api(book ? `/api/books/${book.id}` : '/api/books', { method: book ? 'PUT' : 'POST', body: JSON.stringify(body) }); notify(book ? 'Book updated.' : 'Book added.'); renderBooks(); }
  catch (error) { notify(error.message, true); }
}

async function addOrEditStudent(student = null) {
  const fields = [
    { name: 'id', label: 'Student ID', value: student?.id, required: true, disabled: Boolean(student) },
    { name: 'name', label: 'Full name', value: student?.name, required: true }, { name: 'email', label: 'Email', type: 'email', value: student?.email, required: true },
    { name: 'phone', label: 'Phone', value: student?.phone }, { name: 'department', label: 'Department', value: student?.department },
    { name: 'academicYear', label: 'Academic year', value: student?.academicYear }, { name: 'address', label: 'Address', value: student?.address, wide: true }
  ];
  const values = await showDialog(student ? 'Edit student' : 'Add student', fields);
  if (!values) return;
  if (student) values.id = student.id;
  try { await api(student ? `/api/students/${encodeURIComponent(student.id)}` : '/api/students', { method: student ? 'PUT' : 'POST', body: JSON.stringify(values) }); notify(student ? 'Student updated.' : 'Student added.'); renderStudents(); }
  catch (error) { notify(error.message, true); }
}

async function confirmAction(message) { return window.confirm(message); }

async function handleAction(button) {
  const action = button.dataset.action;
  try {
    if (action === 'refresh') return navigate(state.page);
    if (action === 'search-books') return renderBooks(document.querySelector('#bookSearch').value, document.querySelector('#bookSort').value);
    if (action === 'search-students') return renderStudents(document.querySelector('#studentSearch').value);
    if (action === 'search-loans') return renderIssues(document.querySelector('#loanSearch').value, document.querySelector('#overdueOnly').checked);
    if (action === 'search-returns') return renderReturns(document.querySelector('#returnSearch').value);
    if (action === 'add-book') return addOrEditBook();
    if (action === 'edit-book') return addOrEditBook(state.books.find(item => item.id === Number(button.dataset.id)));
    if (action === 'delete-book') {
      const book = state.books.find(item => item.id === Number(button.dataset.id));
      if (await confirmAction(`Delete “${book.title}”?`)) { await api(`/api/books/${book.id}`, { method: 'DELETE' }); notify('Book deleted.'); renderBooks(); }
    }
    if (action === 'add-student') return addOrEditStudent();
    if (action === 'edit-student') return addOrEditStudent(state.students.find(item => item.id === button.dataset.id));
    if (action === 'delete-student') {
      const student = state.students.find(item => item.id === button.dataset.id);
      if (await confirmAction(`Delete ${student.name}?`)) { await api(`/api/students/${encodeURIComponent(student.id)}`, { method: 'DELETE' }); notify('Student deleted.'); renderStudents(); }
    }
    if (action === 'return-book') {
      if (await confirmAction('Record this book as returned? Any overdue fine will be calculated using the current setting.')) {
        const result = await api('/api/returns', { method: 'POST', body: JSON.stringify({ issueId: Number(button.dataset.id) }) });
        notify(`Return saved · ${result.overdueDays} overdue day(s) · fine ${result.fineAmount}`); renderReturns();
      }
    }
    if (action === 'return-tab') { state.returnTab = button.dataset.tab; return renderReturns(document.querySelector('#returnSearch')?.value || ''); }
    if (action === 'pay-fine') {
      const amount = await showDialog('Record fine payment', [{ name: 'amount', label: `Outstanding balance: ${button.dataset.balance}`, type: 'number', min: '0.01', step: '0.01', value: button.dataset.balance, required: true }], 'Record payment');
      if (amount) { await api(`/api/fines/${button.dataset.id}/payments`, { method: 'POST', body: JSON.stringify(amount) }); notify('Payment recorded.'); renderReturns('', 'fines'); }
    }
    if (action === 'export-report') exportReport();
  } catch (error) { notify(error.message, true); }
}

function exportReport() {
  if (!state.report) return;
  const quote = value => `"${String(value ?? '').replaceAll('"', '""')}"`;
  const rows = [state.report.columns, ...state.report.rows].map(row => row.map(quote).join(',')).join('\r\n');
  const link = document.createElement('a'); link.href = URL.createObjectURL(new Blob([rows], { type: 'text/csv;charset=utf-8' }));
  link.download = `${state.reportType.toLowerCase().replaceAll(' ', '-')}.csv`; link.click(); URL.revokeObjectURL(link.href);
}

document.querySelector('#mainNav').addEventListener('click', event => {
  const button = event.target.closest('[data-page]'); if (button) navigate(button.dataset.page);
});
content.addEventListener('click', event => {
  const button = event.target.closest('[data-action]'); if (button) handleAction(button);
});
document.querySelector('#logoutButton').addEventListener('click', async () => {
  try { await api('/api/auth/logout', { method: 'POST', body: '{}' }); showLogin(false); }
  catch (error) { notify(error.message, true); }
});
document.querySelector('#authForm').addEventListener('submit', async event => {
  event.preventDefault(); const form = new FormData(event.currentTarget); const setup = event.currentTarget.dataset.setup === 'true';
  const body = { username: form.get('username'), password: form.get('password') };
  if (setup) body.confirmation = form.get('confirmation');
  try {
    const result = await api(setup ? '/api/auth/setup' : '/api/auth/login', { method: 'POST', body: JSON.stringify(body) });
    event.currentTarget.reset(); showWorkspace(result.username);
  } catch (error) { document.querySelector('#authError').textContent = error.message; }
});
document.querySelector('#reportType')?.addEventListener('change', event => renderReports(event.target.value));
content.addEventListener('change', event => { if (event.target.id === 'reportType') renderReports(event.target.value); });

(async function initialize() {
  try { const status = await api('/api/auth/status'); showLogin(status.setupRequired); }
  catch (error) { showLogin(false); document.querySelector('#authError').textContent = error.message; }
})();
