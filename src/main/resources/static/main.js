// ✅ BUFFER per file selezionati nel MERGE
let mergeFileBuffer = [];
const csvFilesMergeInput = document.getElementById("csvFilesMerge");
const mergeFileList = document.getElementById("mergeFileList");

if (csvFilesMergeInput && mergeFileList) {
  csvFilesMergeInput.addEventListener("change", function () {
    const newFiles = Array.from(csvFilesMergeInput.files);

    // Aggiungi evitando duplicati per nome file
    newFiles.forEach(file => {
      if (!mergeFileBuffer.some(f => f.name === file.name)) {
        mergeFileBuffer.push(file);
      }
    });

    // Aggiorna interfaccia
    if (mergeFileBuffer.length > 0) {
      mergeFileList.innerHTML = "<strong>File selezionati:</strong><ul>" +
        mergeFileBuffer.map(f => `<li>${f.name}</li>`).join("") +
        "</ul>";
    } else {
      mergeFileList.innerHTML = "";
    }

    // Resetta input per poter aggiungere di nuovo lo stesso file se serve
    csvFilesMergeInput.value = "";
  });
}

// ✅ Invia file per analisi CSV (più file)
const analyzeForm = document.getElementById("analyzeForm");
const analyzeResult = document.getElementById("analyzeResult");

if (analyzeForm && analyzeResult) {
  analyzeForm.addEventListener("submit", function (e) {
    e.preventDefault();
    const files = document.getElementById("csvFileAnalyze").files;
    analyzeResult.innerHTML = "";

    for (let file of files) {
      const formData = new FormData();
      formData.append("file", file);

      fetch("http://localhost:8080/api/csv/validate", {
        method: "POST",
        body: formData
      })
        .then(res => res.json())
        .then(data => {
          analyzeResult.innerHTML += `<pre><strong>${file.name}</strong>\n${JSON.stringify(data, null, 2)}</pre>`;
        })
        .catch(err => {
          analyzeResult.innerHTML += `<div class="alert alert-danger">Errore con ${file.name}: ${err.message}</div>`;
        });
    }
  });
}

// ✅ Carica alias disponibili nella select
const dbAliasSelect = document.getElementById("dbAlias");

function loadDbAliasOptions() {
  if (!dbAliasSelect) return;
  dbAliasSelect.innerHTML = '<option value="">-- seleziona un alias --</option>';

  fetch("http://localhost:8080/api/admin/dbconfig/list")
    .then(res => res.json())
    .then(data => {
      data.forEach(alias => {
        const opt = document.createElement("option");
        opt.value = alias;
        opt.textContent = alias;
        dbAliasSelect.appendChild(opt);
      });
    })
    .catch(err => console.error("Errore nel caricamento alias DB:", err));
}

// Carica al primo avvio
loadDbAliasOptions();

// ✅ Invia richiesta per validazione da DB (ora usa solo alias e nome tabella)
const dbForm = document.getElementById("dbForm");
const dbResult = document.getElementById("dbResult");

if (dbForm && dbResult) {
  dbForm.addEventListener("submit", function (e) {
    e.preventDefault();
    const payload = {
      dbAlias: document.getElementById("dbAlias").value,
      tableName: document.getElementById("dbTable").value
    };

    fetch("http://localhost:8080/api/db/validate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    })
      .then(res => res.json())
      .then(data => {
        dbResult.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
      })
      .catch(err => {
        dbResult.innerHTML = `<div class="alert alert-danger">Errore: ${err.message}</div>`;
      });
  });
}

// ✅ Carica tabelle in base all'alias selezionato
const dbTableSelect = document.getElementById("dbTable");

if (dbAliasSelect && dbTableSelect) {
  dbAliasSelect.addEventListener("change", function () {
    const alias = dbAliasSelect.value;
    dbTableSelect.innerHTML = '<option value="">-- seleziona una tabella --</option>';

    if (!alias) return;

    fetch(`http://localhost:8080/api/db/tables/${alias}`)
      .then(res => res.json())
      .then(tables => {
        tables.forEach(table => {
          const opt = document.createElement("option");
          opt.value = table;
          opt.textContent = table;
          dbTableSelect.appendChild(opt);
        });
      })
      .catch(err => {
        console.error("Errore nel caricamento delle tabelle:", err);
      });
  });
}

// ✅ Invia file multipli per MERGE
const mergeForm = document.getElementById("mergeForm");
const mergeResult = document.getElementById("mergeResult");

if (mergeForm && mergeResult) {
  mergeForm.addEventListener("submit", function (e) {
    e.preventDefault();
    const joinKey = document.getElementById("mergeKey").value;

    if (mergeFileBuffer.length < 2) {
      mergeResult.innerHTML = `<div class="alert alert-warning">Seleziona almeno due file CSV.</div>`;
      return;
    }

    const formData = new FormData();
    mergeFileBuffer.forEach(f => formData.append("csvFiles", f));
    formData.append("joinKey", joinKey);

    fetch("http://localhost:8080/api/csv/merge", {
      method: "POST",
      body: formData
    })
      .then(res => res.text())
      .then(text => {
        mergeResult.innerHTML = `<div class="alert alert-success">${text}</div>`;
        mergeFileBuffer = [];
        mergeFileList.innerHTML = "";
      })
      .catch(err => {
        mergeResult.innerHTML = `<div class="alert alert-danger">Errore: ${err.message}</div>`;
      });
  });
}

// ✅ Form registrazione DB (admin)
const adminRegisterForm = document.getElementById("adminRegisterForm");
const adminRegisterResult = document.getElementById("adminRegisterResult");

if (adminRegisterForm && adminRegisterResult) {
  adminRegisterForm.addEventListener("submit", function (e) {
    e.preventDefault();

    const payload = {
      alias: document.getElementById("alias").value,
      url: document.getElementById("url").value,
      username: document.getElementById("user").value,
      password: document.getElementById("pass").value
    };

    fetch("http://localhost:8080/api/admin/dbconfig/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    })
      .then(res => res.text())
      .then(msg => {
        adminRegisterResult.innerHTML = `<div class="alert alert-success">${msg}</div>`;
        adminRegisterForm.reset();
        loadDbAliasOptions(); // 🔄 aggiorna select
      })
      .catch(err => {
        adminRegisterResult.innerHTML = `<div class="alert alert-danger">Errore: ${err.message}</div>`;
      });
  });
}
