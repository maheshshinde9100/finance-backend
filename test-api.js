// test-api.js - cross-platform API test script for Zorvyn backend
// Usage:
//   API_BASE_URL=http://localhost:8081 node test-api.js
//   node test-api.js  (falls back to localhost)

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8081';
const USERNAME = process.env.TEST_USER || 'testuser';
const EMAIL = process.env.TEST_EMAIL || 'testuser@example.com';
const PASSWORD = process.env.TEST_PASS || 'test@123';

async function apiFetch(path, options = {}) {
  const url = `${API_BASE_URL}${path}`;
  if (options.body && typeof options.body !== 'string') {
    options.body = JSON.stringify(options.body);
  }
  options.headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };
  const response = await fetch(url, options);
  const text = await response.text();
  let body = text;
  try {
    body = JSON.parse(text);
  } catch (ignored) {}

  if (!response.ok) {
    throw new Error(`HTTP ${response.status} ${response.statusText}: ${text}`);
  }
  return { status: response.status, body };
}

async function main() {
  console.log('API base url:', API_BASE_URL);

  // Signup
  console.log('\n1) Signup user');
  const signupResult = await apiFetch('/api/auth/signup', {
    method: 'POST',
    body: { username: USERNAME, email: EMAIL, password: PASSWORD },
  });
  console.log('Signup response:', signupResult.body);

  // Login
  console.log('\n2) Login user');
  const loginResult = await apiFetch('/api/auth/login', {
    method: 'POST',
    body: { username: USERNAME, password: PASSWORD },
  });
  console.log('Login response:', loginResult.body);

  const token = loginResult.body?.token;
  if (!token) {
    throw new Error('Login did not return token');
  }

  const authHeaders = {
    Authorization: `Bearer ${token}`,
  };

  console.log('\n3) Get all records (should be empty or user-specific data)');
  const recordsList = await apiFetch('/api/records', {
    method: 'GET',
    headers: authHeaders,
  });
  console.log('Records:', recordsList.body);

  console.log('\n4) Create a record');
  const createResponse = await apiFetch('/api/records', {
    method: 'POST',
    headers: authHeaders,
    body: {
      type: 'INCOME',
      category: 'Salary',
      amount: 5000,
      description: 'Test salary income',
      date: new Date().toISOString().split('T')[0],
    },
  });
  console.log('Created record:', createResponse.body);

  const recordId = createResponse.body?.id || createResponse.body?._id;
  if (!recordId) {
    throw new Error('Created record did not return id');
  }

  console.log('\n5) Get dashboard summary');
  const dashboard = await apiFetch('/api/records/dashboard', {
    method: 'GET',
    headers: authHeaders,
  });
  console.log('Dashboard:', dashboard.body);

  console.log('\n6) Filter records by type');
  const filtered = await apiFetch('/api/records/filter?type=INCOME', {
    method: 'GET',
    headers: authHeaders,
  });
  console.log('Filtered INCOME:', filtered.body);

  console.log('\n7) Update record');
  const updateResponse = await apiFetch(`/api/records/${recordId}`, {
    method: 'PUT',
    headers: authHeaders,
    body: {
      type: 'INCOME',
      category: 'Updated Salary',
      amount: 6000,
      description: 'Updated test income',
      date: new Date().toISOString().split('T')[0],
    },
  });
  console.log('Updated record:', updateResponse.body);

  console.log('\n8) Get record by id');
  const readResponse = await apiFetch(`/api/records/${recordId}`, {
    method: 'GET',
    headers: authHeaders,
  });
  console.log('Record by id:', readResponse.body);

  console.log('\n9) Delete record');
  await apiFetch(`/api/records/${recordId}`, {
    method: 'DELETE',
    headers: authHeaders,
  });
  console.log('Deleted record', recordId);

  console.log('\n10) Confirm records list');
  const finalRecords = await apiFetch('/api/records', {
    method: 'GET',
    headers: authHeaders,
  });
  console.log('Final Records:', finalRecords.body);

  console.log('\nAll API tests completed successfully.');
}

main().catch((error) => {
  console.error('API test failed:', error.message || error);
  process.exit(1);
});