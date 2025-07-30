// 🔐 Blocco accesso se non loggato (escluse login e register-admin)
const currentPath = window.location.pathname;
if (!['/login.html', '/register-admin.html'].some(p => currentPath.endsWith(p))) {
	if (!localStorage.getItem('token')) {
		window.location.href = 'login.html';
	}
}

// ✅ Fetch con JWT
function fetchWithAuth(url, options = {}) {
	const token = localStorage.getItem('token');
	options.headers = options.headers || {};
	options.headers['Authorization'] = 'Bearer ' + token;
	return fetch(url, options);
}

// ✅ Logout
const logoutBtn = document.getElementById("logoutBtn");
if (logoutBtn) {
	logoutBtn.addEventListener("click", () => {
		fetchWithAuth("http://localhost:8080/api/auth/logout", { method: "POST" })
			.finally(() => {
				localStorage.removeItem("token");
				localStorage.removeItem("role");
				window.location.href = "login.html";
			});
	});
}

// ✅ Adatta UI in base al ruolo
function decodeTokenAndAdaptUI() {
	const token = localStorage.getItem("token");
	if (!token) return;
	try {
		const payload = JSON.parse(atob(token.split('.')[1]));
		const role = payload.role;
		localStorage.setItem("role", role);
		document.getElementById("admin-tab")?.style && (document.getElementById("admin-tab").style.display = role === "ADMIN" ? "block" : "none");
		document.getElementById("users-tab")?.style && (document.getElementById("users-tab").style.display = role === "ADMIN" ? "block" : "none");
		document.getElementById("profile-tab")?.style && (document.getElementById("profile-tab").style.display = role === "ADMIN" ? "block" : "none");
	} catch (e) {
		console.error("Token non valido:", e);
	}
}
decodeTokenAndAdaptUI();

// ✅ Mostra link "Registra Admin" se DB vuoto
const registerAdminSection = document.getElementById("registerAdminSection");
if (registerAdminSection) {
	fetch("http://localhost:8080/api/auth/is-empty")
		.then(res => res.json())
		.then(isEmpty => {
			if (isEmpty) {
				registerAdminSection.style.display = "block";
			}
		})
		.catch(err => console.error("Errore is-empty:", err));
}

// ✅ Blocco accesso a register-admin.html se DB non vuoto
if (window.location.pathname.endsWith("register-admin.html")) {
	fetch("http://localhost:8080/api/auth/is-empty")
		.then(res => res.json())
		.then(isEmpty => {
			if (!isEmpty) {
				alert("Registrazione admin non disponibile.");
				window.location.href = "login.html";
			}
		});
}

// ✅ Merge buffer file
let mergeFileBuffer = [];
const csvFilesMergeInput = document.getElementById("csvFilesMerge");
const mergeFileList = document.getElementById("mergeFileList");

if (csvFilesMergeInput && mergeFileList) {
	csvFilesMergeInput.addEventListener("change", () => {
		const newFiles = Array.from(csvFilesMergeInput.files);
		newFiles.forEach(f => {
			if (!mergeFileBuffer.some(existing => existing.name === f.name)) {
				mergeFileBuffer.push(f);
			}
		});
		mergeFileList.innerHTML = mergeFileBuffer.length > 0
			? `<strong>File selezionati:</strong><ul>${mergeFileBuffer.map(f => `<li>${f.name}</li>`).join('')}</ul>`
			: "";
		csvFilesMergeInput.value = "";
	});
}

// ✅ Analisi CSV
const analyzeForm = document.getElementById("analyzeForm");
if (analyzeForm) {
	analyzeForm.addEventListener("submit", e => {
		e.preventDefault();
		const files = document.getElementById("csvFileAnalyze").files;
		const analyzeResult = document.getElementById("analyzeResult");
		analyzeResult.innerHTML = "";

		Array.from(files).forEach(file => {
			const formData = new FormData();
			formData.append("file", file);
			fetchWithAuth("http://localhost:8080/api/csv/validate", {
				method: "POST",
				body: formData
			})
				.then(res => res.json())
				.then(data => {
					analyzeResult.innerHTML += `<pre><strong>${file.name}</strong>\n${JSON.stringify(data, null, 2)}</pre>`;
				});
		});
	});
}

// ✅ CSV da DB
const dbForm = document.getElementById("dbForm");
if (dbForm) {
	dbForm.addEventListener("submit", e => {
		e.preventDefault();
		const payload = {
			dbAlias: document.getElementById("dbAlias").value,
			tableName: document.getElementById("dbTable").value
		};
		fetchWithAuth("http://localhost:8080/api/db/validate", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(payload)
		})
			.then(res => res.json())
			.then(data => {
				document.getElementById("dbResult").innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
			});
	});
}

// ✅ Caricamento alias e tabelle
function loadDbAliasOptions() {
	const dbAliasSelect = document.getElementById("dbAlias");
	if (!dbAliasSelect) return;
	dbAliasSelect.innerHTML = '<option value="">-- seleziona un alias --</option>';
	fetchWithAuth("http://localhost:8080/api/db/aliases")
		.then(res => res.json())
		.then(data => {
			data.forEach(alias => {
				const opt = document.createElement("option");
				opt.value = alias;
				opt.textContent = alias;
				dbAliasSelect.appendChild(opt);
			});
		});
}
loadDbAliasOptions();

const dbAliasSelect = document.getElementById("dbAlias");
const dbTableSelect = document.getElementById("dbTable");

if (dbAliasSelect && dbTableSelect) {
	dbAliasSelect.addEventListener("change", () => {
		const alias = dbAliasSelect.value;
		dbTableSelect.innerHTML = '<option value="">-- seleziona una tabella --</option>';
		if (!alias) return;
		fetchWithAuth(`http://localhost:8080/api/db/tables/${alias}`)
			.then(res => res.json())
			.then(tables => {
				tables.forEach(table => {
					const opt = document.createElement("option");
					opt.value = table;
					opt.textContent = table;
					dbTableSelect.appendChild(opt);
				});
			});
	});
}

// ✅ Merge CSV
const mergeForm = document.getElementById("mergeForm");
if (mergeForm) {
	mergeForm.addEventListener("submit", e => {
		e.preventDefault();
		const formData = new FormData();
		mergeFileBuffer.forEach(f => formData.append("csvFiles", f));
		formData.append("joinKey", document.getElementById("mergeKey").value);
		fetchWithAuth("http://localhost:8080/api/csv/merge", {
			method: "POST",
			body: formData
		})
			.then(res => res.text())
			.then(msg => {
				document.getElementById("mergeResult").innerHTML = `<div class="alert alert-success">${msg}</div>`;
				mergeFileBuffer = [];
				mergeFileList.innerHTML = "";
			});
	});
}

// ✅ Gestione Utenti
const userRegisterForm = document.getElementById("userRegisterForm");
if (userRegisterForm) {
	userRegisterForm.addEventListener("submit", e => {
		e.preventDefault();
		const payload = {
			username: document.getElementById("newUsername").value,
			password: document.getElementById("newPassword").value,
			role: document.getElementById("newRole").value
		};
		fetchWithAuth("http://localhost:8080/api/auth/register", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(payload)
		})
			.then(res => res.text())
			.then(msg => {
				document.getElementById("userRegisterResult").innerHTML = `<div class="alert alert-success">${msg}</div>`;
				userRegisterForm.reset();
				loadUsers();
			});
	});
}

function loadUsers() {
	fetchWithAuth("http://localhost:8080/api/auth/all")
		.then(res => res.json())
		.then(users => {
			const tbody = document.querySelector("#userTable tbody");
			tbody.innerHTML = "";
			users.forEach(user => {
				const isProtected = user.protectedAdmin === true;
				const tr = document.createElement("tr");
				tr.innerHTML = `
          <td>${user.username}</td>
          ${isProtected ? `<td><span class='badge bg-secondary'>${user.role}</span></td><td><span class='text-muted'>Protetto</span></td>` : `
            <td>
              <select class='form-select form-select-sm' id='role-${user.username}'>
                <option value='ADMIN' ${user.role === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                <option value='EMPLOYEE' ${user.role === 'EMPLOYEE' ? 'selected' : ''}>EMPLOYEE</option>
              </select>
            </td>
            <td>
              <button class='btn btn-sm btn-success me-1' onclick='updateUserRole("${user.username}")'>Salva</button>
              <button class='btn btn-sm btn-danger' onclick='deleteUser("${user.username}")'>Elimina</button>
            </td>`}`;
				tbody.appendChild(tr);
			});
		});
}

function updateUserRole(username) {
	const selectedRole = document.getElementById(`role-${username}`).value;
	const payload = { username, role: selectedRole };
	fetchWithAuth("http://localhost:8080/api/auth/update-role", {
		method: "PUT",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(payload)
	})
		.then(res => {
			if (!res.ok) throw new Error("Errore aggiornamento ruolo");
			return res.text();
		})
		.then(() => {
			alert("Ruolo aggiornato con successo");
			loadUsers();
		});
}

function deleteUser(username) {
	if (!confirm(`Sei sicuro di voler eliminare ${username}?`)) return;
	fetchWithAuth(`http://localhost:8080/api/auth/${username}`, {
		method: "DELETE"
	})
		.then(res => res.text())
		.then(() => {
			alert("Utente eliminato");
			loadUsers();
		});
}

const usersTab = document.getElementById("users-tab");
if (usersTab) {
	usersTab.addEventListener("click", () => {
		loadUsers();
	});
}

// ✅ Modifica Profilo Admin Protetto
const profileForm = document.getElementById("profileUpdateForm");
if (profileForm) {
	profileForm.addEventListener("submit", function(e) {
		e.preventDefault();
		const payload = {
			newUsername: document.getElementById("newAdminUsername").value,
			newPassword: document.getElementById("newAdminPassword").value
		};

		fetchWithAuth("http://localhost:8080/api/admin-profile/update", {
			method: "PUT",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(payload)
		})
			.then(res => res.text())
			.then(msg => {
				document.getElementById("profileUpdateResult").innerHTML =
					`<div class='alert alert-success'>Credenziali aggiornate. Verrai disconnesso...</div>`;
				setTimeout(() => {
					localStorage.removeItem("token");
					localStorage.removeItem("role");
					window.location.href = "login.html";
				}, 1500);
			})
			.catch(err => {
				document.getElementById("profileUpdateResult").innerHTML =
					`<div class='alert alert-danger'>${err.message}</div>`;
			});
	});
}

// ✅ Login (per login.html)
const form = document.getElementById("loginForm");
const result = document.getElementById("loginResult");

if (form) {
	form.addEventListener("submit", function(e) {
		e.preventDefault();
		const payload = {
			username: document.getElementById("username").value,
			password: document.getElementById("password").value
		};

		fetch("http://localhost:8080/api/auth/login", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(payload)
		})
			.then(res => {
				if (!res.ok) throw new Error("Credenziali non valide");
				return res.json();
			})
			.then(data => {
				localStorage.setItem("token", data.token);
				window.location.href = "index.html";
			})
			.catch(err => {
				result.innerHTML = `<div class='alert alert-danger'>${err.message}</div>`;
			});
	});
}
const profileTab = document.getElementById("profile-tab");
if (profileTab) {
	fetchWithAuth("http://localhost:8080/api/admin-profile/is-protected-admin")
		.then(res => res.json())
		.then(isProtected => {
			profileTab.style.display = isProtected ? "block" : "none";
		})
		.catch(err => {
			console.error("Errore verifica protectedAdmin:", err);
			profileTab.style.display = "none";
		});
}
// ✅ Mostra nome utente e ruolo in alto
const userInfo = document.getElementById("userInfo");
const token = localStorage.getItem("token");

if (userInfo && token) {
	try {
		const payload = JSON.parse(atob(token.split('.')[1]));
		const username = payload.sub || payload.username || "Utente";
		const role = payload.role || "RUOLO";
		userInfo.textContent = `👤 ${username} (${role})`;
	} catch (e) {
		console.error("Errore nel decodificare il token per username e ruolo");
	}
}

