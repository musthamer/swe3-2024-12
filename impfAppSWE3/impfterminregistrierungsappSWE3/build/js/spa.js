(function() {
  const appRoot = document.getElementById('app');
  const welcomeBox = document.getElementById('welcomeBox');
  function redirectToAdmin() {
    window.location.replace('admin');
  }

  // Hilfsfunktion: Script nur einmal laden
  const loadedScripts = new Set();
  function loadScriptOnce(src, callback) {
    if (loadedScripts.has(src)) { if (callback) callback(); return; }
    const s = document.createElement('script');
    s.src = src;
    s.onload = function() { loadedScripts.add(src); if (callback) callback(); };
    s.onerror = function() { console.error('Fehler beim Laden von', src); if (callback) callback(); };
    document.body.appendChild(s);
  }

  // Views als einfache Renderer-Funktionen
  const Views = {
    home: () => {
      // Startseite zeigt einfach die Login-Box (bereits im DOM)
      if (welcomeBox) welcomeBox.style.display = '';
      if (appRoot) appRoot.style.display = 'none';
    },
    register: () => {
      if (!appRoot) return;
      if (welcomeBox) welcomeBox.style.display = 'none';
      appRoot.style.display = '';
      appRoot.innerHTML = [
        '<h1>Registrieren</h1>',
        '<div id="registerStatus"></div>',
        '<div class="form-container">',
        '  <form id="registerForm">',
        '    <div class="form-group">',
        '      <label for="regFirstName">Vorname:</label>',
        '      <input type="text" id="regFirstName" name="firstName" required>',
        '    </div>',
        '    <div class="form-group">',
        '      <label for="regLastName">Nachname:</label>',
        '      <input type="text" id="regLastName" name="lastName" required>',
        '    </div>',
        '    <div class="form-group">',
        '      <label for="regDateOfBirth">Geburtsdatum:</label>',
        '      <input type="date" id="regDateOfBirth" name="dateOfBirth" required>',
        '    </div>',
        '    <div class="form-group">',
        '      <label for="regEmail">E-Mail:</label>',
        '      <input type="email" id="regEmail" name="email" required>',
        '    </div>',
        '    <div class="form-group">',
        '      <label for="regPassword">Passwort:</label>',
        '      <input type="password" id="regPassword" name="password" required>',
        '    </div>',
        '    <div class="form-group">',
        '      <label for="regPasswordConfirm">Passwort wiederholen:</label>',
        '      <input type="password" id="regPasswordConfirm" name="passwordConfirm" required>',
        '    </div>',
        '    <div class="form-group">',
        '      <input type="submit" value="Registrieren">',
        '    </div>',
        '  </form>',
        '</div>',
        '<p>Bereits registriert? <a href="#/login">Anmelden</a></p>'
      ].join('');

      const form = document.getElementById('registerForm');
      if (form) {
        form.addEventListener('submit', function(e) {
          e.preventDefault();
          const firstName = document.getElementById('regFirstName').value;
          const lastName = document.getElementById('regLastName').value;
          const dateOfBirth = document.getElementById('regDateOfBirth').value;
          const email = document.getElementById('regEmail').value;
          const password = document.getElementById('regPassword').value;
          const passwordConfirm = document.getElementById('regPasswordConfirm').value;
          if (!firstName || !lastName || !dateOfBirth || !email || !password || !passwordConfirm) {
            showMessage('registerStatus', 'Bitte füllen Sie alle Felder aus.', true);
            return;
          }
          if (password !== passwordConfirm) {
            showMessage('registerStatus', 'Die Passwörter stimmen nicht überein.', true);
            return;
          }
          const params = new URLSearchParams();
          params.append('firstName', firstName);
          params.append('lastName', lastName);
          params.append('dateOfBirth', dateOfBirth);
          params.append('email', email);
          params.append('password', password);
          params.append('passwordConfirm', passwordConfirm);
          const xhr = new XMLHttpRequest();
          xhr.open('POST', 'register', true);
          xhr.withCredentials = true;
          xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
          xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
          xhr.onload = function() {
            try {
              const data = JSON.parse(xhr.responseText);
              if (xhr.status >= 200 && xhr.status < 300 && data.success) {
                showMessage('registerStatus', 'Registrierung erfolgreich! Bitte öffnen Sie die Mock-E-Mails und aktivieren Sie Ihren Account.', false);
                form.reset();
                var basePath = window.location.pathname.replace(/[^\/]*$/, '');
                var emailsHref = basePath + 'emails';
                appRoot.insertAdjacentHTML('beforeend', '<p><a href="' + emailsHref + '" target="_blank" rel="noopener" class="btn">Mock-E-Mails öffnen</a></p>');
              } else {
                showMessage('registerStatus', data.message || 'Fehler bei der Registrierung', true);
              }
            } catch (e) {
              showMessage('registerStatus', 'Fehler beim Parsen der Antwort', true);
            }
          };
          xhr.onerror = function() { showMessage('registerStatus', 'Netzwerkfehler', true); };
          xhr.send(params.toString());
        });
      }
    },
    forgot: () => {
      if (!appRoot) return;
      if (welcomeBox) welcomeBox.style.display = 'none';
      appRoot.style.display = '';
      appRoot.innerHTML = [
        '<h1>Passwort zurücksetzen</h1>',
        '<div class="form-container">',
        '  <p>Zum Zurücksetzen des Passworts verwenden wir die klassische Seite.</p>',
        '  <p><a class="btn btn-primary" href="forgot-password">Link anfordern</a></p>',
        '  <p><a href="#/login">Zurück zum Login</a></p>',
        '</div>'
      ].join('');
    },
    booking: () => {
      if (!appRoot) return;

      function renderBookingView() {
        if (welcomeBox) welcomeBox.style.display = 'none';
        appRoot.style.display = '';
        appRoot.innerHTML = [
          '<div class="user-info" id="userInfo"><span id="userName">Nicht angemeldet</span></div>',
          '<ul class="nav-tabs">',
          '  <li class="nav-right"><a href="#/logout" id="logoutLink">Abmelden</a></li>',
          '</ul>',
          '<div id="statusMessage"></div>',
          '<div id="loginRequired" style="display:none;">',
          '  <p>Sie müssen angemeldet sein, um diese Seite zu nutzen.</p>',
          '  <p><a href="#/login">Zum Login</a></p>',
          '</div>',
          '<div id="dashboardContainer" style="display:none;">',
          '  <div class="tab">',
          '    <button class="tablinks active" data-tab="tabBooking" onclick="switchTab(event, \"tabBooking\")">Impftermin buchen</button>',
          '    <button class="tablinks" data-tab="tabAppointments" onclick="switchTab(event, \"tabAppointments\")">Meine Termine</button>',
          '  </div>',
          '  <div id="tabBooking" class="tabcontent active">',
          '    <form id="vaccineBookingForm">',
          '      <div class="form-group booking-for-group">',
          '        <label>Buchung für:</label>',
          '        <label class="inline-radio"><input type="radio" name="booking_for" value="self" id="bookingForSelf" checked> Mich selbst</label>',
          '        <label class="inline-radio"><input type="radio" name="booking_for" value="other" id="bookingForOther"> Andere Person (z.&nbsp;B. Familienmitglied)</label>',
          '      </div>',
          '      <div class="form-group"><label for="firstName">Vorname:</label><input type="text" id="firstName" name="first_name" required></div>',
          '      <div class="form-group"><label for="lastName">Nachname:</label><input type="text" id="lastName" name="last_name" required></div>',
          '      <div class="form-group" id="accountEmailGroup"><label>Kontakt-E-Mail (Bestätigung):</label><span id="accountEmailDisplay" class="readonly-field"></span></div>',
          '      <div class="form-group"><label for="dateOfBirth">Geburtsdatum:</label><input type="date" id="dateOfBirth" name="date_of_birth" required></div>',
          '      <div class="form-group"><label for="center">Impfzentrum:</label><select id="center" name="center_id" required><option value="">Bitte wählen</option></select></div>',
          '      <div class="form-group"><label for="vaccine">Impfstoff:</label><select id="vaccine" name="vaccine_id" required disabled><option value="">Bitte wählen</option></select></div>',
          '      <div class="form-group"><label for="timeslot">Termin:</label><select id="timeslot" name="timeslot_id" required disabled><option value="">Bitte wählen</option></select></div>',
          '      <div class="form-group"><button type="submit">Termin buchen</button></div>',
          '    </form>',
          '    <div id="userAppointments"></div>',
          '  </div>',
          '  <div id="tabAppointments" class="tabcontent"><div id="appointmentsList"></div></div>',
          '</div>'
        ].join('');

        loadScriptOnce('js/booking.js', function() {
          if (typeof initBookingUI === 'function') {
            window.__bookingUiInitialized = false;
            initBookingUI();
          } else if (typeof checkLoginStatus === 'function') {
            checkLoginStatus(function(data) {
              if (data && data.loggedIn) {
                document.getElementById('loginRequired').style.display = 'none';
                document.getElementById('dashboardContainer').style.display = 'block';
                document.getElementById('userName').textContent = 'Angemeldet als: ' + (data.userName || data.email || 'Benutzer');
                if (typeof loadVaccinationCenters === 'function') loadVaccinationCenters();
              } else {
                document.getElementById('loginRequired').style.display = 'block';
                document.getElementById('dashboardContainer').style.display = 'none';
              }
            });
          }
        });
      }

      if (typeof checkLoginStatus === 'function') {
        checkLoginStatus(function(data) {
          if (data && data.loggedIn && data.userRole === 'ADMIN') {
            redirectToAdmin();
            return;
          }
          renderBookingView();
        });
        return;
      }
      renderBookingView();
    },
    admin: () => {
      redirectToAdmin();
    },
    login: () => {
      // Fokussiert die Login-Box auf der Startseite
      if (welcomeBox) {
        welcomeBox.style.display = '';
        const email = document.getElementById('email');
        if (email) email.focus();
      }
      if (appRoot) appRoot.style.display = 'none';
    },
    logout: () => {
      window.sendRequest('POST', 'logout', null, function() {
        window.location.href = './';
      });
    }
  };

  function router() {
    const hash = window.location.hash || '#/';
    if (hash.startsWith('#/register')) return Views.register();
    if (hash.startsWith('#/forgot')) return Views.forgot();
    if (hash.startsWith('#/booking')) return Views.booking();
    if (hash.startsWith('#/admin')) return Views.admin();
    if (hash.startsWith('#/login')) return Views.login();
    if (hash.startsWith('#/logout')) return Views.logout();
    return Views.home();
  }

  window.addEventListener('hashchange', router);
  router();
})();


