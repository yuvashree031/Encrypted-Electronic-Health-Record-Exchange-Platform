// Healthcare Data Exchange - Modern UI Application

let token = localStorage.getItem('token');
let userRole = localStorage.getItem('role');
let userEmail = localStorage.getItem('email');
let userName = localStorage.getItem('name');

// Initialize app
if (token) {
    showDashboard();
    loadDashboardData();
}

// Auth Functions
function switchTab(tab) {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const tabs = document.querySelectorAll('.tab-btn');
    
    tabs.forEach(t => t.classList.remove('active'));
    
    if (tab === 'login') {
        loginForm.classList.remove('hidden');
        registerForm.classList.add('hidden');
        tabs[0].classList.add('active');
    } else {
        registerForm.classList.remove('hidden');
        loginForm.classList.add('hidden');
        tabs[1].classList.add('active');
    }
}

async function login() {
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    
    if (!email || !password) {
        showMessage('authMessage', 'Please fill in all fields', 'error');
        return;
    }

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const result = await response.json();
        
        if (response.ok) {
            localStorage.setItem('token', result.token);
            localStorage.setItem('email', result.email);
            localStorage.setItem('role', result.role);
            localStorage.setItem('name', result.email.split('@')[0]);
            showMessage('authMessage', 'Login successful! Redirecting...', 'success');
            setTimeout(() => location.reload(), 1000);
        } else {
            showMessage('authMessage', result.message || 'Login failed', 'error');
        }
    } catch (error) {
        showMessage('authMessage', 'Connection error. Please try again.', 'error');
    }
}

async function register() {
    const name = document.getElementById('regName').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const role = document.getElementById('regRole').value;
    
    if (!name || !email || !password || !role) {
        showMessage('authMessage', 'Please fill in all fields', 'error');
        return;
    }

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, role })
        });

        const result = await response.json();
        
        if (response.ok) {
            localStorage.setItem('token', result.token);
            localStorage.setItem('email', result.email);
            localStorage.setItem('role', result.role);
            localStorage.setItem('name', name);
            showMessage('authMessage', 'Account created! Redirecting...', 'success');
            setTimeout(() => location.reload(), 1000);
        } else {
            showMessage('authMessage', result.message || 'Registration failed', 'error');
        }
    } catch (error) {
        showMessage('authMessage', 'Connection error. Please try again.', 'error');
    }
}

function logout() {
    localStorage.clear();
    location.reload();
}

// Dashboard Functions
function showDashboard() {
    document.getElementById('authSection').classList.add('hidden');
    document.getElementById('dashboardSection').classList.remove('hidden');
    
    const displayName = userName || userEmail.split('@')[0];
    const initials = displayName.substring(0, 2).toUpperCase();
    
    document.getElementById('sidebarUserName').textContent = displayName;
    document.getElementById('sidebarUserRole').textContent = userRole;
    document.getElementById('topBarUserName').textContent = displayName;
    document.getElementById('topBarUserRole').textContent = userRole;
    document.getElementById('welcomeUserName').textContent = displayName;
    document.getElementById('userAvatar').textContent = initials;
    
    if (userRole === 'DOCTOR') {
        document.getElementById('patientsNav').style.display = 'flex';
        document.getElementById('doctorRecordsView').classList.remove('hidden');
    } else {
        document.getElementById('patientRecordsView').classList.remove('hidden');
    }
}

function showSection(section) {
    const sections = ['overview', 'records', 'patients', 'audit'];
    const navItems = document.querySelectorAll('.nav-item');
    
    sections.forEach(s => {
        document.getElementById(s + 'Section').classList.add('hidden');
    });
    
    navItems.forEach(item => item.classList.remove('active'));
    
    document.getElementById(section + 'Section').classList.remove('hidden');
    event.target.closest('.nav-item').classList.add('active');
    
    const titles = {
        overview: 'Dashboard Overview',
        records: 'Medical Records',
        patients: 'Patient Directory',
        audit: 'System Audit Logs'
    };
    
    document.getElementById('pageTitle').textContent = titles[section];
    
    if (section === 'records' && userRole === 'PATIENT') {
        loadRecords();
    } else if (section === 'patients' && userRole === 'DOCTOR') {
        loadAllPatients();
    } else if (section === 'audit') {
        loadAuditLogs();
    }
}

async function loadDashboardData() {
    try {
        const [recordsRes, auditRes] = await Promise.all([
            fetch('/api/records/my-records', {
                headers: { 'Authorization': 'Bearer ' + token }
            }),
            fetch('/api/audit/logs', {
                headers: { 'Authorization': 'Bearer ' + token }
            })
        ]);
        
        if (recordsRes.ok) {
            const records = await recordsRes.json();
            document.getElementById('statRecords').textContent = records.length;
        }
        
        if (auditRes.ok) {
            const audits = await auditRes.json();
            document.getElementById('statAudits').textContent = audits.length;
        }
        
        if (userRole === 'DOCTOR') {
            const patientsRes = await fetch('/api/users/patients', {
                headers: { 'Authorization': 'Bearer ' + token }
            });
            if (patientsRes.ok) {
                const patients = await patientsRes.json();
                document.getElementById('statUsers').textContent = patients.length;
            }
        }
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

// Patient Functions
async function loadPatients() {
    try {
        const response = await fetch('/api/users/patients', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        const patients = await response.json();
        
        if (response.ok) {
            const select = document.getElementById('patientSelect');
            select.innerHTML = '<option value="">Choose a patient</option>';
            
            patients.forEach(patient => {
                const option = document.createElement('option');
                option.value = patient.id;
                option.textContent = `${patient.name} - ${patient.email}`;
                option.dataset.name = patient.name;
                option.dataset.email = patient.email;
                select.appendChild(option);
            });
            
            select.onchange = function() {
                const selected = this.options[this.selectedIndex];
                if (selected.value) {
                    const info = document.getElementById('patientInfo');
                    info.innerHTML = `
                        <strong>Selected Patient:</strong><br>
                        Name: ${selected.dataset.name}<br>
                        Email: ${selected.dataset.email}<br>
                        ID: ${selected.value}
                    `;
                    info.classList.remove('hidden');
                } else {
                    document.getElementById('patientInfo').classList.add('hidden');
                }
            };
            
            showMessage('doctorMessage', `Loaded ${patients.length} patient(s)`, 'success');
        }
    } catch (error) {
        showMessage('doctorMessage', 'Error loading patients', 'error');
    }
}

async function loadAllPatients() {
    try {
        const response = await fetch('/api/users/patients', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        const patients = await response.json();
        
        if (response.ok) {
            const grid = document.getElementById('patientsGrid');
            
            if (patients.length === 0) {
                grid.innerHTML = '<div class="card"><p>No patients registered yet.</p></div>';
                return;
            }
            
            grid.innerHTML = '<div class="patient-grid">' + patients.map(patient => `
                <div class="patient-card">
                    <div class="user-avatar" style="margin-bottom: 12px;">${patient.name.substring(0, 2).toUpperCase()}</div>
                    <h3 style="margin-bottom: 8px; font-size: 18px;">${patient.name}</h3>
                    <p style="color: var(--text-secondary); font-size: 14px; margin-bottom: 4px;">${patient.email}</p>
                    <p style="color: var(--text-secondary); font-size: 12px;">Patient ID: ${patient.id}</p>
                </div>
            `).join('') + '</div>';
        }
    } catch (error) {
        console.error('Error loading patients:', error);
    }
}

// Medical Records Functions
async function createRecord() {
    const patientId = document.getElementById('patientSelect').value;
    const recordData = document.getElementById('recordData').value;
    const pdfFile = document.getElementById('pdfFile').files[0];

    if (!patientId) {
        showMessage('doctorMessage', 'Please select a patient', 'error');
        return;
    }

    if (!recordData.trim()) {
        showMessage('doctorMessage', 'Please enter medical record details', 'error');
        return;
    }

    try {
        const formData = new FormData();
        formData.append('patientId', patientId);
        formData.append('recordData', recordData);
        
        if (pdfFile) {
            // Validate file type
            if (pdfFile.type !== 'application/pdf') {
                showMessage('doctorMessage', 'Please upload a PDF file only', 'error');
                return;
            }
            
            // Validate file size (10MB limit)
            if (pdfFile.size > 10 * 1024 * 1024) {
                showMessage('doctorMessage', 'PDF file size must be less than 10MB', 'error');
                return;
            }
            
            formData.append('pdfFile', pdfFile);
        }

        const response = await fetch('/api/records/create', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token
                // Don't set Content-Type header when using FormData
            },
            body: formData
        });

        const result = await response.json();
        
        if (response.ok) {
            showMessage('doctorMessage', 'Medical record created successfully!', 'success');
            document.getElementById('patientSelect').value = '';
            document.getElementById('recordData').value = '';
            document.getElementById('pdfFile').value = '';
            document.getElementById('patientInfo').classList.add('hidden');
            loadDashboardData();
        } else {
            showMessage('doctorMessage', result.message || 'Failed to create record', 'error');
        }
    } catch (error) {
        showMessage('doctorMessage', 'Connection error. Please try again.', 'error');
    }
}

async function loadRecords() {
    try {
        const response = await fetch('/api/records/my-records', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        const records = await response.json();
        
        if (response.ok) {
            const list = document.getElementById('recordsList');
            
            if (records.length === 0) {
                list.innerHTML = '<div class="card"><p>No medical records found.</p></div>';
                return;
            }
            
            list.innerHTML = records.map(record => `
                <div class="record-card">
                    <div class="record-header">
                        <span class="record-id">Record #${record.id}</span>
                        <span class="record-date">${new Date(record.createdAt).toLocaleString()}</span>
                    </div>
                    <div class="record-content">${record.recordData}</div>
                    <div class="record-meta">
                        <span>Doctor ID: ${record.doctorId}</span>
                        <span>Patient ID: ${record.patientId}</span>
                    </div>
                    ${record.pdfFilePath ? `
                        <div class="record-actions">
                            <button onclick="viewPdf(${record.id})" class="btn btn-secondary btn-sm">
                                📄 View PDF
                            </button>
                        </div>
                    ` : ''}
                </div>
            `).join('');
        }
    } catch (error) {
        showMessage('patientMessage', 'Error loading records', 'error');
    }
}

async function viewPdf(recordId) {
    try {
        const response = await fetch(`/api/records/download-pdf/${recordId}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const newWindow = window.open(url, '_blank');
            
            // Clean up the object URL when the window is closed
            if (newWindow) {
                newWindow.onload = () => {
                    setTimeout(() => window.URL.revokeObjectURL(url), 100);
                };
            }
        } else {
            alert('Failed to load PDF. Please try again.');
        }
    } catch (error) {
        alert('Error loading PDF: ' + error.message);
    }
}

// Audit Logs Functions
async function loadAuditLogs() {
    try {
        const response = await fetch('/api/audit/logs', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        const logs = await response.json();
        
        if (response.ok) {
            const list = document.getElementById('auditLogsList');
            
            if (logs.length === 0) {
                list.innerHTML = '<div class="card"><p>No audit logs found.</p></div>';
                return;
            }
            
            list.innerHTML = `
                <div class="audit-table">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Action</th>
                                <th>User</th>
                                <th>Timestamp</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${logs.map(log => `
                                <tr>
                                    <td>#${log.id}</td>
                                    <td><span class="audit-action">${log.action}</span></td>
                                    <td>${log.userEmail}</td>
                                    <td>${new Date(log.timestamp).toLocaleString()}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            `;
        }
    } catch (error) {
        console.error('Error loading audit logs:', error);
    }
}

// Utility Functions
function showMessage(elementId, message, type) {
    const element = document.getElementById(elementId);
    element.innerHTML = `<div class="message ${type}">${message}</div>`;
    setTimeout(() => element.innerHTML = '', 5000);
}
