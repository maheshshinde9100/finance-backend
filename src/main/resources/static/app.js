// API Configuration
const API_BASE_URL = '/api'; // relative base path is safer for both local and deployed environments

// State Management
let authToken = localStorage.getItem('authToken');
let currentUser = JSON.parse(localStorage.getItem('currentUser'));
let allRecords = [];
let dashboardData = null;

function handleUnauthorized() {
    authToken = null;
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    showToast('Session expired. Please login again.', 'error');
    showPage('auth');
    showContentPage('dashboard');
}

async function apiFetch(path, options = {}) {
    options.headers = options.headers || {};

    if (authToken) {
        options.headers['Authorization'] = `Bearer ${authToken}`;
    }

    if (!options.headers['Content-Type'] && !(options.body instanceof FormData)) {
        options.headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(`${API_BASE_URL}${path}`, options);

    if (response.status === 401) {
        handleUnauthorized();
        throw new Error('Unauthorized');
    }

    if (!response.ok) {
        const msg = await response.text();
        throw new Error(msg || `Request failed with status ${response.status}`);
    }

    return response;
}

// Initialize Application
document.addEventListener('DOMContentLoaded', () => {
    if (authToken && currentUser) {
        showPage('app');
        document.getElementById('navUsername').textContent = currentUser.username || 'User';
        showContentPage('dashboard');
        loadDashboard();
    } else {
        showPage('auth');
    }

    setupEventListeners();
    setDefaultDate();
});

// Event Listeners Setup
function setupEventListeners() {
    // Auth tabs
    document.querySelectorAll('.auth-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            const tabName = tab.getAttribute('data-tab');
            switchAuthTab(tabName);
        });
    });
    
    // Navigation links
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', () => {
            const page = link.getAttribute('data-page');
            showContentPage(page);
        });
    });
}

// Page Navigation
function showPage(pageName) {
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });
    document.getElementById(`${pageName}Page`).classList.add('active');
}

function showContentPage(pageName) {
    if (!authToken || !currentUser) {
        showToast('Please login first', 'error');
        showPage('auth');
        return;
    }

    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    document.querySelectorAll('.content-page').forEach(page => {
        page.classList.remove('active');
    });

    document.querySelector(`[data-page="${pageName}"]`).classList.add('active');
    document.getElementById(`${pageName}Page`).classList.add('active');

    // Load data for specific pages
    if (pageName === 'dashboard') {
        loadDashboard();
    } else if (pageName === 'records') {
        loadRecords();
    } else if (pageName === 'analytics') {
        loadAnalytics();
    }
}

// Authentication Functions
function switchAuthTab(tabName) {
    document.querySelectorAll('.auth-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.auth-form').forEach(form => {
        form.classList.remove('active');
    });
    
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');
    document.getElementById(`${tabName}Form`).classList.add('active');
}

async function handleLogin(event) {
    event.preventDefault();
    
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    
    try {
        const response = await apiFetch('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();
        authToken = data.token;
        currentUser = {
            id: data.id,
            username: data.username,
            email: data.email,
            roles: data.roles
        };

        localStorage.setItem('authToken', authToken);
        localStorage.setItem('currentUser', JSON.stringify(currentUser));

        showToast('Login successful!', 'success');
        showPage('app');
        document.getElementById('navUsername').textContent = currentUser.username;
        loadDashboard();
    } catch (error) {
        showToast(error.message || 'Network error. Please try again.', 'error');
    }
}

async function handleSignup(event) {
    event.preventDefault();
    
    const username = document.getElementById('signupUsername').value;
    const email = document.getElementById('signupEmail').value;
    const password = document.getElementById('signupPassword').value;
    
    try {
        const response = await apiFetch('/auth/signup', {
            method: 'POST',
            body: JSON.stringify({ username, email, password })
        });

        const data = await response.json();
        showToast(data.message, 'success');
        switchAuthTab('login');
        document.getElementById('signupForm').reset();
    } catch (error) {
        showToast(error.message || 'Network error. Please try again.', 'error');
    }
}

function handleLogout() {
    authToken = null;
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    showPage('auth');
    showToast('Logged out successfully', 'success');
}

// Dashboard Functions
async function loadDashboard() {
    try {
        const response = await apiFetch('/records/dashboard');
        dashboardData = await response.json();
        displayDashboard(dashboardData);
        loadRecentTransactions();
    } catch (error) {
        showToast(error.message || 'Failed to load dashboard', 'error');
    }
}

function displayDashboard(data) {
    document.getElementById('totalIncome').textContent = formatCurrency(data.totalIncome);
    document.getElementById('totalExpense').textContent = formatCurrency(data.totalExpense);
    document.getElementById('balance').textContent = formatCurrency(data.balance);
    document.getElementById('totalRecords').textContent = data.totalRecords;
}

async function loadRecentTransactions() {
    try {
        const response = await apiFetch('/records');
        const records = await response.json();
        const recent = records.slice(0, 5);
        displayRecentTransactions(recent);
    } catch (error) {
        console.error('Failed to load recent transactions:', error.message);
    }
}

function displayRecentTransactions(records) {
    const container = document.getElementById('recentTransactions');
    
    if (records.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📊</div><div class="empty-state-text">No transactions yet</div></div>';
        return;
    }
    
    const html = records.map(record => `
        <div class="record-item ${record.type.toLowerCase()}">
            <div class="record-header">
                <div class="record-info">
                    <h4>${record.category}</h4>
                    <div class="record-meta">${formatDate(record.date)}</div>
                </div>
                <div class="record-amount ${record.type.toLowerCase()}">
                    ${record.type === 'INCOME' ? '+' : '-'}${formatCurrency(record.amount)}
                </div>
            </div>
            ${record.description ? `<div class="record-description">${record.description}</div>` : ''}
        </div>
    `).join('');
    
    container.innerHTML = html;
}

// Records Functions
async function loadRecords() {
    try {
        const response = await apiFetch('/records');
        allRecords = await response.json();
        displayRecords(allRecords);
    } catch (error) {
        showToast(error.message || 'Failed to load records', 'error');
    }
}

function displayRecords(records) {
    const container = document.getElementById('recordsList');
    
    if (records.length === 0) {
        container.innerHTML = '<div class="section-card"><div class="empty-state"><div class="empty-state-icon">📝</div><div class="empty-state-text">No records found</div></div></div>';
        return;
    }
    
    const html = records.map(record => `
        <div class="record-item ${record.type.toLowerCase()}">
            <div class="record-header">
                <div class="record-info">
                    <h4>${record.category}</h4>
                    <div class="record-meta">${formatDate(record.date)} • ${record.type}</div>
                </div>
                <div class="record-amount ${record.type.toLowerCase()}">
                    ${record.type === 'INCOME' ? '+' : '-'}${formatCurrency(record.amount)}
                </div>
            </div>
            ${record.description ? `<div class="record-description">${record.description}</div>` : ''}
            <div class="record-actions">
                <button class="btn btn-sm btn-primary" onclick="openEditModal('${record.id}')">Edit</button>
                <button class="btn btn-sm btn-danger" onclick="deleteRecord('${record.id}')">Delete</button>
            </div>
        </div>
    `).join('');
    
    container.innerHTML = html;
}

async function handleAddRecord(event) {
    event.preventDefault();
    
    const recordData = {
        type: document.getElementById('recordType').value,
        category: document.getElementById('recordCategory').value,
        amount: parseFloat(document.getElementById('recordAmount').value),
        description: document.getElementById('recordDescription').value,
        date: document.getElementById('recordDate').value
    };
    
    try {
        await apiFetch('/records', {
            method: 'POST',
            body: JSON.stringify(recordData)
        });

        showToast('Record added successfully!', 'success');
        document.getElementById('addRecordForm').reset();
        setDefaultDate();
        loadDashboard();
        if (document.getElementById('recordsPage').classList.contains('active')) {
            loadRecords();
        }
    } catch (error) {
        showToast(error.message || 'Failed to add record', 'error');
    }
}

async function deleteRecord(id) {
    if (!confirm('Are you sure you want to delete this record?')) {
        return;
    }
    
    try {
        await apiFetch(`/records/${id}`, {
            method: 'DELETE'
        });

        showToast('Record deleted successfully!', 'success');
        loadRecords();
        loadDashboard();
    } catch (error) {
        showToast(error.message || 'Failed to delete record', 'error');
    }
}

// Edit Modal Functions
function openEditModal(id) {
    const record = allRecords.find(r => r.id === id);
    if (!record) return;
    
    document.getElementById('editRecordId').value = record.id;
    document.getElementById('editRecordType').value = record.type;
    document.getElementById('editRecordCategory').value = record.category;
    document.getElementById('editRecordAmount').value = record.amount;
    document.getElementById('editRecordDate').value = record.date;
    document.getElementById('editRecordDescription').value = record.description || '';
    
    document.getElementById('editModal').classList.add('active');
}

function closeEditModal() {
    document.getElementById('editModal').classList.remove('active');
    document.getElementById('editRecordForm').reset();
}

async function handleUpdateRecord(event) {
    event.preventDefault();
    
    const id = document.getElementById('editRecordId').value;
    const recordData = {
        type: document.getElementById('editRecordType').value,
        category: document.getElementById('editRecordCategory').value,
        amount: parseFloat(document.getElementById('editRecordAmount').value),
        description: document.getElementById('editRecordDescription').value,
        date: document.getElementById('editRecordDate').value
    };
    
    try {
        await apiFetch(`/records/${id}`, {
            method: 'PUT',
            body: JSON.stringify(recordData)
        });

        showToast('Record updated successfully!', 'success');
        closeEditModal();
        loadRecords();
        loadDashboard();
    } catch (error) {
        showToast(error.message || 'Failed to update record', 'error');
    }
}

// Filter Functions
async function applyFilters() {
    const type = document.getElementById('filterType').value;
    const category = document.getElementById('filterCategory').value;
    const startDate = document.getElementById('filterStartDate').value;
    const endDate = document.getElementById('filterEndDate').value;
    
    const params = new URLSearchParams();
    if (type) params.append('type', type);
    if (category) params.append('category', category);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    try {
        const response = await apiFetch(`/records/filter?${params.toString()}`);
        const records = await response.json();
        displayRecords(records);
    } catch (error) {
        showToast(error.message || 'Failed to filter records', 'error');
    }
}

function clearFilters() {
    document.getElementById('filterType').value = '';
    document.getElementById('filterCategory').value = '';
    document.getElementById('filterStartDate').value = '';
    document.getElementById('filterEndDate').value = '';
    loadRecords();
}

// Analytics Functions
async function loadAnalytics() {
    try {
        const response = await apiFetch('/records/dashboard');
        const data = await response.json();
        displayAnalytics(data);
    } catch (error) {
        showToast(error.message || 'Failed to load analytics', 'error');
    }
}

function displayAnalytics(data) {
    // Income by Category
    const incomeContainer = document.getElementById('incomeByCategory');
    if (Object.keys(data.incomeByCategory).length === 0) {
        incomeContainer.innerHTML = '<div class="empty-state-text">No income data</div>';
    } else {
        const incomeHtml = Object.entries(data.incomeByCategory)
            .map(([category, amount]) => `
                <div class="category-item">
                    <span class="category-name">${category}</span>
                    <span class="category-amount">${formatCurrency(amount)}</span>
                </div>
            `).join('');
        incomeContainer.innerHTML = incomeHtml;
    }
    
    // Expense by Category
    const expenseContainer = document.getElementById('expenseByCategory');
    if (Object.keys(data.expenseByCategory).length === 0) {
        expenseContainer.innerHTML = '<div class="empty-state-text">No expense data</div>';
    } else {
        const expenseHtml = Object.entries(data.expenseByCategory)
            .map(([category, amount]) => `
                <div class="category-item">
                    <span class="category-name">${category}</span>
                    <span class="category-amount">${formatCurrency(amount)}</span>
                </div>
            `).join('');
        expenseContainer.innerHTML = expenseHtml;
    }
    
    // Monthly Trend
    const trendContainer = document.getElementById('monthlyTrend');
    if (Object.keys(data.monthlyTrend).length === 0) {
        trendContainer.innerHTML = '<div class="empty-state-text">No trend data</div>';
    } else {
        const trendHtml = Object.entries(data.monthlyTrend)
            .map(([month, amount]) => `
                <div class="trend-item">
                    <span class="trend-month">${month}</span>
                    <span class="trend-amount">${formatCurrency(amount)}</span>
                </div>
            `).join('');
        trendContainer.innerHTML = trendHtml;
    }
}

// Utility Functions
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: 2
    }).format(amount);
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-IN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    }).format(date);
}

function setDefaultDate() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('recordDate').value = today;
}

function resetForm() {
    document.getElementById('addRecordForm').reset();
    setDefaultDate();
}

// Close modal when clicking outside
document.addEventListener('click', (event) => {
    const modal = document.getElementById('editModal');
    if (event.target === modal) {
        closeEditModal();
    }
});
