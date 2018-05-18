# AppStar
Questa applicazione e' un progetto universitario che permette di interrogare un database PostgreSQL contenente dati su stelle e filamenti di gas, attraverso un'interfaccia desktop.

Funzioni disponibili:

- Menu Login: inserimento user e password di un utente registrato, tasto Login, tasto Esci.
	
- Menu Home: scelta dell'operazione da effettuare. Funzioni amministratore (ADMIN) nascoste per gli utenti semplici. Tasto Avanti e Logout

<<<<<<< HEAD
	- (ADMIN) Registrazione di un nuovo utente: aggiunge l'utente al database
=======
	- (ADMIN) Registrazione di un nuovo utente: Permette di aggiungere l'utente al database
	- (ADMIN) Importazione di file csv: per ora sovrascrive solo la tabella "contorni" sul database
	- TODO: (ADMIN) Inserire dati satelliti
	- TODO: (ADMIN) Inserire dati strumenti
	- TODO: Ricerca filamenti
	- TODO. Ricerca stelle
>>>>>>> Branch_Lorenzo
## Descrizione Applicazione
- [x] __Requisito 1__
	- Menu Login: inserimento user e password di un utente registrato. L'amministratore ha (id, password) = (lorzar, lorzar)
	- Menu Home: scelta dell'operazione da effettuare. Funzioni amministratore (ADMIN) bloccate per gli utenti semplici. Cliccare il 	   tasto Avanti per andare alla schermata della funzione scelta. Funzioni dell'applicazione:
- [x] __Requisiti 2-4__
	- [x] (ADMIN) Registrazione di un nuovo utente: Permette di aggiungere l'utente al database
	- [x] (ADMIN) Importazione di file csv: importa i file in una tra le tabelle "contorni_imp" , "filamenti_imp", 				"scheletri_imp", "stelle_imp", per poi smistarli nel database.
	- [x] (ADMIN) Inserire dati satelliti
	- [x] (ADMIN) Inserire dati strumenti
- [ ] __Requisiti 5-12__
	- [x] Calcolo Centroide, Estensione e numero segmenti di un filamento
	- [x] Ricerca filamenti per contrasto e ellitticità
	- [x] Ricerca filamenti per numero di segmenti
	- [x] Ricerca di filamenti in regioni quadrate o circolari di mappa
	- [ ] Ricerca delle stelle interne a un filamento
	- [ ] Ricerca di stelle in regioni rettangolari, interne o esterne ai filamenti
	- [ ] Calcolo della distanza minima degli estremi di un segmento di un filamento dal contorno
	- [ ] Calcolo della distanza minima dalla spina dorsale per stelle interne a un filamento
