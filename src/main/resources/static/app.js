const API_BASE_URL = 'http://localhost:8081/api';
let authToken = localStorage.getItem('authToken');
let currentUser = JSON.parse(localStorage.getItem('currentUser'));

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    if (authToken && currentUser) {
        showAppSection();
        loadDashboard();
    } else {
        showAuthSection();
    }
    
    // Set today's date as default
    document.getElementById('recordDate').valueAsDate = new Date();
});

// Authentication Functions
function switchAuthTab(tab) {
    document.querySelectorAll('.auth-section .tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.auth-section .tab-content').forEach(c => c.classList.remove('active'));
    
    if (tab === 'login') {
        document.querySelectorAll('.auth-section .tab')[0].classList.add('active');
        document.getElementById('loginTab').classList.add('active');
    } else {
        document.querySelectorAll('.auth-section .tab')[1].classList.add('active');
        document.getElementById('signupTab').classList.add('active');
    }
}

async function handleLogin(event) {
    event.preventDefault();
    
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        
        if (response.ok) {
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
            
            showMessage('Login successful!', 'success');
            showAppSection();
            loadDashboard();
        } else {
            const error = await response.json();
            showMessage(error.error || 'Login failed', 'error');
        }
    } catch (error) {
        showMessage('Network error. Please try again.', 'error');
    }
}

async function handleSignup(event) {
    event.preventDefault();
    
    const username = document.getElementById('signupUsername').value;
    const email = document.getElementById('signupEmail').value;
    const password = document.getElementById('signupPassword').value;
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/signup`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, email, password })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showMessage(data.message, 'success');
            switchAuthTab('login');
            document.getElementById('signupForm').reset();
        } else {
            showMessage(data.message || 'Signup failed', 'error');
        }
    } catch (error) {
        showMessage('Network error. Please try again.', 'error');
    }
}

function handleLogout() {
    authToken = null;
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    showAuthSection();
    showMessage('Logged out successfully', 'success');
}

// App Navigation
function switchAppTab(tab) {
    document.querySelectorAll('.app-section .tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.app-section .tab-content').forEach(c => c.classList.remove('active'));
    
    const tabs = ['dashboard', 'records', 'add'];
    const index = tabs.indexOf(tab);
    
    document.querySelectorAll('.app-section .tab')[index].classList.add('active');
    document.getElementById(`${tab}Tab`).classList.add('active');
    
    if (tab === 'dashboard') {
        loadDashboard();
    } else if (tab === 'records') {
        loadRecords();
    }
}

function showAuthSection() {
    document.getElementById('authSection').classList.add('active');
    document.getElementById('appSection').classList.remove('active');
}

function showAppSection() {
    document.getElementById('authSection').classList.remove('active');
    document.getElementById('appSection').classList.add('active');
    document.getElementById('currentUser').textContent = currentUser.username;
}

// Dashboard Functions
async function loadDashboard() {
    try {
        const response = await fetch(`${API_BASE_URL}/records/dashboard`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            displayDashboard(data);
        } else {
            showMessage('Failed to load dashboard', 'error');
        }
    } catch (error) {
        showMessage('Network error. Please try again.', 'error');
    }
}

function displayDashboard(data) {
    const statsHtml = `
        <div class="stat-card">
            <h3>Total Income</h3>
            <div class="value">$${data.totalIncome.toFixed(2)}</div>
        </div>
        <div class="stat-card">
            <h3>Total Expense</h3>
            <div class="value">$${data.totalExpense.toFixed(2)}</div>
        </div>
        <div class="stat-card">
            <h3>Balance</h3>
            <div class="value">$${data.balance.toFixed(2)}</div>
        </div>
        <div class="stat-card">
            <h3>Total Records</h3>
            <div class="value">${data.totalRecords}</div>
        </div>
    `;
    
    document.getElementById('dashboardStats').innerHTML = statsHtml;
}

// Records Functions
async function loadRecords() {
    try {
        const response = await fetch(`${API_BASE_URL}/records`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok) {
            const records = await response.json();
            displayRecords(records);
        } else {
            showMessage('Failed to load records', 'error');
        }
    } catch (error) {
        showMessage('Network error. Please try again.', 'error');
    }
}

function displayRecords(records) {
    if (records.length === 0) {
        document.getElementById('recordsList').innerHTML = '<p>No records found.</p>';
        return;
    }
    
    const recordsHtml = records.map(record => `
        <div class="record-card ${record.type.toLowerCase()}">
            <div class="record-header">
                <div>
                    <strong>${record.category}</strong>
                    <span style="margin-left: 10px; color: #666;">${record.date}</span>
                </div>
                <div class="record-amount ${record.type.toLowerCase()}">
                    ${record.type === 'INCOME' ? '+' : '-'}$${record.amount.toFixed(2)}
                </div>
            </div>
            <p>${record.description || 'No description'}</p>
            <button class="btn btn-danger" onclick="deleteRecord('${record.id}')" style="margin-top: 10px;">Delete</button>
        </div>
    `).join('');
    
    document.getElementById('recordsList').innerHTML = recordsHtml;
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
        const response = await fetch(`${API_BASE_URL}/records`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify(recordData)
        });
        
        if (response.ok) {
            showMessage('Record added successfully!', 'success');
            document.getElementById('recordForm').reset();
            document.getElementById('recordDate').valueAsDate = new Date();
            loadDashboard();
        } else {
            const error = await response.json();
            showMessage('Failed to add record', 'error');
        }
    } catch (error) {
        showMessage('Network error. Please try again.', 'error');
    }
}

async function deleteRecord(id) {
    if (!confirm('Are you sure you want to delete this record?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/records/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok) {
            showMessage('Record deleted successfully!', 'success');
            loadRecords();
            loadDashboard();
        } else {
            showMessage('Failed to delete record', 'error');
        }
    } catch (error) {
        showMessage('Network error. Please try again.', 'error');
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
        const response = await fetch(`${API_BASE_URL}/records/filter?${params.toString()}`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok) {
            const records = await response.json();
            displayRecords(records);
        } else {
            showMessage('Failed to filter records', 'error');
        }
    } catch (error) {
        showMessage('Network error. Please try again.', 'error');
    }
}

function clearFilters() {
    document.getElementById('filterType').value = '';
    document.getElementById('filterCategory').value = '';
    document.getElementById('filterStartDate').value = '';
    document.getElementById('filterEndDate').value = '';
    loadRecords();
}

// Utility Functions
function showMessage(message, type) {
    const messageDiv = document.getElementById('message');
    messageDiv.innerHTML = `<div class="message ${type}">${message}</div>`;
    
    setTimeout(() => {
        messageDiv.innerHTML = '';
    }, 5000);
}
