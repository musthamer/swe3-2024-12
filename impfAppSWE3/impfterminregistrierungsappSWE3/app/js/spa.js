(function() {
  const appRoot = document.getElementById('app');

  function redirectToAdmin() {
    window.location.replace('admin');
  }

  function bindLoginForm(statusElementId) {
    const form = document.getElementById('loginForm');
    if (!form || form.dataset.bound === 'true') {
      return;
    }
    form.dataset.bound = 'true';

    form.addEventListener('submit', function(e) {
      e.preventDefault();
      const email = document.getElementById('email').value;
      const password = document.getElementById('password').value;

      if (!email || !password) {
        showMessage(statusElementId, 'Bitte füllen Sie alle Felder aus.', true);
        return;
      }

      const params = new URLSearchParams();
      params.append('email', email);
      params.append('password', password);

      window.sendRequest('POST', 'login', params, function(data, status) {
        if (status >= 200 && status < 300 && data.success) {
          window.navigateAfterLogin(data);
        } else {
          showMessage(statusElementId, data.message || 'Fehler bei der Anmeldung', true);
        }
      });
    });
  }

  const Views = {
    home: () => {
      if (!appRoot) return;
      appRoot.className = 'centered-container';
      appRoot.innerHTML = [
        '<div class="logo">',
        '  <h1>Impftermin-Portal</h1>',
        '  <p>Willkommen beim Impftermin-Portal der Stadt Nemerb</p>',
        '</div>',
        '<div id="loginStatus"></div>',
        '<div class="form-container">',
        '  <form id="loginForm">',
        '    <div class="form-group">',
        '      <label for="email">E-Mail:</label>',
        '      <input type="email" id="email" name="email" required>',
        '    </div>',
        '    <div class="form-group">',
        '      <label for="password">Passwort:</label>',
        '      <input type="password" id="password" name="password" required>',
        '    </div>',
        '    <div class="form-group">',
        '      <input type="submit" value="Anmelden" class="btn-primary">',
        '    </div>',
        '  </form>',
        '</div>',
        '<div class="auth-links">',
        '  <p><a href="#/forgot">Passwort vergessen?</a></p>',
        '  <p>Noch kein Konto? <a href="#/register">Hier registrieren</a></p>',
        '</div>'
      ].join('');

      bindLoginForm('loginStatus');
      const email = document.getElementById('email');
      if (email) email.focus();
    },

    register: () => {
      if (!appRoot) return;
      appRoot.className = 'centered-container';
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
        '<p>Bereits registriert? <a href="#/">Anmelden</a></p>'
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

          window.sendRequest('POST', 'register', params, function(data, status) {
            if (status >= 200 && status < 300 && data.success) {
              showMessage('registerStatus', 'Registrierung erfolgreich! Bitte öffnen Sie die Mock-E-Mails und aktivieren Sie Ihren Account.', false);
              form.reset();
              const basePath = window.location.pathname.replace(/[^/]*$/, '');
              appRoot.insertAdjacentHTML('beforeend',
                '<p><a href="' + basePath + 'emails" class="btn">Mock-E-Mails öffnen</a></p>');
            } else {
              showMessage('registerStatus', data.message || 'Fehler bei der Registrierung', true);
            }
          }, { 'X-Requested-With': 'XMLHttpRequest' });
        });
      }
    },

    forgot: () => {
      if (!appRoot) return;
      appRoot.className = 'centered-container';
      appRoot.innerHTML = [
        '<h1>Passwort zurücksetzen</h1>',
        '<div id="forgotStatus"></div>',
        '<div class="form-container">',
        '  <form id="forgotForm">',
        '    <div class="form-group">',
        '      <label for="forgotEmail">E-Mail:</label>',
        '      <input type="email" id="forgotEmail" name="email" required>',
        '    </div>',
        '    <div class="form-group">',
        '      <input type="submit" value="Link anfordern" class="btn-primary">',
        '    </div>',
        '  </form>',
        '  <p><a href="#/">Zurück zum Login</a></p>',
        '</div>'
      ].join('');

      const form = document.getElementById('forgotForm');
      if (form) {
        form.addEventListener('submit', function(e) {
          e.preventDefault();
          const email = document.getElementById('forgotEmail').value.trim();
          if (!email) {
            showMessage('forgotStatus', 'Bitte geben Sie Ihre E-Mail-Adresse ein.', true);
            return;
          }

          const params = new URLSearchParams();
          params.append('email', email);

          window.sendRequest('POST', 'forgot-password', params, function(data, status) {
            if (status >= 200 && status < 300 && data.success) {
              showMessage('forgotStatus', data.message, false);
              form.reset();
              const basePath = window.location.pathname.replace(/[^/]*$/, '');
              appRoot.insertAdjacentHTML('beforeend',
                '<p><a href="' + basePath + 'emails" class="btn">Mock-E-Mails öffnen</a></p>');
            } else {
              showMessage('forgotStatus', data.message || 'Fehler bei der Anfrage', true);
            }
          }, { 'X-Requested-With': 'XMLHttpRequest' });
        });
      }
    },

    booking: () => {
      if (!appRoot) return;

      checkLoginStatus(function(data) {
        if (data && data.loggedIn && data.userRole === 'ADMIN') {
          redirectToAdmin();
          return;
        }

        appRoot.className = 'booking-view';
        appRoot.innerHTML = [
          '<div class="user-info" id="userInfo"><span id="userName">Nicht angemeldet</span></div>',
          '<ul class="nav-tabs">',
          '  <li class="nav-right"><a href="#/logout" id="logoutLink">Abmelden</a></li>',
          '</ul>',
          '<div id="statusMessage"></div>',
          '<div id="loginRequired" class="hidden">',
          '  <p>Sie müssen angemeldet sein, um diese Seite zu nutzen.</p>',
          '  <p><a href="#/">Zum Login</a></p>',
          '</div>',
          '<div id="dashboardContainer" class="hidden">',
          '  <div class="tab">',
          '    <button class="tablinks active" data-tab="tabBooking">Impftermin buchen</button>',
          '    <button class="tablinks" data-tab="tabAppointments">Meine Termine</button>',
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
          '  </div>',
          '  <div id="tabAppointments" class="tabcontent"><div id="appointmentsList"></div></div>',
          '</div>'
        ].join('');

        window.__bookingUiInitialized = false;
        if (typeof initBookingUI === 'function') {
          initBookingUI(data);
        }
      });
    },

    admin: () => {
      redirectToAdmin();
    },

    logout: () => {
      window.sendRequest('POST', 'logout', null, function() {
        window.__bookingUiInitialized = false;
        appRoot.className = 'centered-container';
        window.location.hash = '#/';
      });
    }
  };

  function router() {
    const hash = window.location.hash || '#/';
    if (hash.startsWith('#/register')) return Views.register();
    if (hash.startsWith('#/forgot')) return Views.forgot();
    if (hash.startsWith('#/booking')) return Views.booking();
    if (hash.startsWith('#/admin')) return Views.admin();
    if (hash.startsWith('#/logout')) return Views.logout();
    return Views.home();
  }

  function checkIfAlreadyLoggedIn(done) {
    const hash = window.location.hash || '';
    if (
      hash.startsWith('#/register') ||
      hash.startsWith('#/forgot') ||
      hash.startsWith('#/booking') ||
      hash.startsWith('#/admin') ||
      hash.startsWith('#/logout')
    ) {
      done();
      return;
    }

    checkLoginStatus(function(data) {
      if (data.loggedIn) {
        if (data.userRole === 'ADMIN') {
          window.location.href = 'admin';
          return;
        }
        window.location.hash = '#/booking';
        return;
      }
      done();
    });
  }

  window.addEventListener('hashchange', router);
  checkIfAlreadyLoggedIn(router);
})();
