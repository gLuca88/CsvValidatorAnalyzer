Questo progetto nasce come esercizio pratico dopo aver seguito un corso su Spring Boot (Udemy).
Il mio obiettivo principale è stato quello di consolidare le competenze backend, mettendo in pratica autenticazione, 
sicurezza con JWT, gestione utenti e integrazione con database.
🔹 Per quanto riguarda il frontend, ho solo conoscenze di base delle tecniche di sviluppo web (HTML, CSS, JavaScript). 
Per questa parte mi sono fatto aiutare dal mio amico ChatGPT, che mi ha supportato nella creazione delle pagine e 
nella gestione della comunicazione con le API backend.
Il risultato è un’applicazione full-stack semplice, dove la parte backend è stata la mia priorità, 
mentre il frontend ha lo scopo di fornire un’interfaccia minimale ma funzionale per utilizzare le principali funzionalità.


CSVVALIDATORANALYZER È UN’APPLICAZIONE SVILUPPATA IN SPRING BOOT PER LAVORARE CON I FILE CSV (VALIDAZIONE, ANALISI, MERGE/ESPORTAZIONE).
Stato: Versione Alpha – alcune funzionalità sono ancora in sviluppo e soggette a cambiamenti.
L’app è progettata con una struttura modulare 
(controller → service su interfacce → repository/model) che rende semplice aggiungere o modificare il codice senza impattare sulle parti esistenti. 
L’autenticazione via JWT e la gestione ruoli (Primo Admin protetto, Admin, Employee) permettono di estendere in modo sicuro le aree funzionali.
L’APPLICAZIONE PERMETTE DI:
Validare i file CSV per verificarne la correttezza (struttura, colonne, formati).
Unire più CSV in un unico file tramite una chiave di relazione.
Gestire utenti e permessi tramite autenticazione JWT con ruoli Admin ed Employee.
Consentire all’Admin di configurare il database, registrare nuovi utenti e aggiornare in sicurezza le proprie credenziali.

📦 Librerie utilizzate
🚀 Spring Boot
spring-boot-starter-web → per creare REST API e gestire il web server integrato.
spring-boot-starter-data-jpa → per la persistenza dei dati con JPA/Hibernate.
spring-boot-starter-security → per la sicurezza e gestione dei ruoli.

🔐 Autenticazione
jjwt-api, jjwt-impl, jjwt-jackson (versione 0.11.5) → librerie per la gestione dei token JWT.

📂 CSV & Excel
opencsv (5.9) → lettura e scrittura di file CSV.
apache poi-ooxml (5.2.3) → generazione e gestione di file Excel (XLSX).

🗄️ Database
mysql-connector-j → connettore JDBC per MySQL.

🛠️ Utilità
lombok → riduce il boilerplate code (getter, setter, costruttori, ecc.).

🔐 RUOLI E PERMESSI
CsvValidatorAnalyzer usa JWT per autenticazione e autorizzazione.
Gli utenti sono divisi in due ruoli: Admin ed Employee. È previsto un Primo Admin Protetto con privilegi totali.

👑 PRIMO ADMIN PROTETTO
Alla prima esecuzione, l’applicazione controlla nella tabella users se esistono già utenti con ruolo ADMIN.
Se il numero di admin è zero, compare la finestra di registrazione del Primo Admin.
Il Primo Admin è speciale:
ha il controllo totale sull’applicazione,è protetto,
può cambiare le proprie credenziali solo tramite autenticazione personale.
Gli utenti con ruolo ADMIN possono:
aggiungere, modificare o rimuovere utenti con ruolo EMPLOYEE.
Solo il Primo Admin ha l’autorizzazione a rimuovere tutti gli utenti (inclusi altri admin).

👷 Employee
Analisi CSV: carica e valida file CSV.
Creazione/Esportazione file (CSV/Excel).
Merge di più CSV tramite chiave di relazione.
Nessun accesso a gestione utenti o configurazioni DB/alias.

🧭 Flusso tipico
Primo Admin Protetto è registrato (seed/DB).
L’Admin effettua il login, ottiene il JWT e gestisce Employee e Alias DB.
Gli Employee usano le funzioni CSV (analisi, export, merge) senza accesso alle sezioni amministrative.

STRUTTURA APPLICAZIONE
/api
├─ /auth                → AuthController           (login, registrazione employee)
├─ /admin-profile       → AdminProfileController   (profilo admin protetto)
├─ /db                  → DbConfigAdminController  (configurazione alias DB)
│   └─ /validate        → DbValidationController   (verifica connessione DB)
├─ /csv                 → CsvController            (validazione/analisi CSV)
└─ /csv-merge           → CsvMergeController       (merge multi-CSV + export)

(+) HomeController       → routing/home statiche (index, health, ecc.)



# 📑 Endpoint principali

## 🔐 AuthController — `/api/auth`
- **POST /login** → Effettua il login e restituisce un JWT (token + info).
- **POST /register-employee** *(SOLO ADMIN)* → Registra un nuovo utente con ruolo Employee.

## 👤 AdminProfileController — `/api/admin-profile`
- **PUT /update** *(SOLO Primo Admin Protetto)* → Aggiorna le proprie credenziali.
- **GET /is-protected-admin** → Ritorna true/false se l’utente del token è admin protetto.

## 🗄️ DbConfigAdminController — `/api/db`
- **POST /config** *(SOLO ADMIN)* → Salva/aggiorna una configurazione DB (con alias).
- **DELETE /config/{alias}** *(SOLO ADMIN)* → Rimuove configurazione DB per alias.
- **GET /configs** *(SOLO ADMIN)* → Elenca le configurazioni disponibili.

## ✅ DbValidationController — `/api/db/validate`
- **GET /?alias=...** *(SOLO ADMIN)* → Valida la connessione per un alias.
- **POST /** *(SOLO ADMIN)* → Valida parametri in body prima del salvataggio.

## 📂 CsvController — `/api/csv`
- **POST /validate** → Valida la struttura di un CSV (header, colonne, formati).
- **POST /analyze** → Analizza il contenuto di un CSV e restituisce dati/metriche.

## 🔗 CsvMergeController — `/api/csv-merge`
- **POST /upload** → Carica più CSV con chiave di relazione per la merge.
- **GET /export** → Scarica l’output unificato (CSV/Excel).

## 🏠 HomeController — `/`
- **GET /** → Landing page / index.
- **GET /health** → Verifica stato applicazione.
