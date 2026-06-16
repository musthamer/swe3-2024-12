// Eine gemeinsame main.js-Datei für grundlegende Funktionen
function showMessage(elementId, message, isError = false) {
  const element = document.getElementById(elementId);
  if (element) {
    element.innerHTML = `<div class="${isError ? 'error-message' : 'success-message'}">${message}</div>`;
  }
}

// Zentrale Request-Funktion (immer verfügbar, nicht erst nach checkLoginStatus())
window.sendRequest = window.sendRequest || function(method, url, params, callback) {
  const xhr = new XMLHttpRequest();
  xhr.open(method, url, true);
  xhr.withCredentials = true;

  if (method === 'POST') {
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
  }

  xhr.onload = function() {
    let data;
    try {
      data = JSON.parse(xhr.responseText);
    } catch (e) {
      console.error('Fehler beim Parsen der Antwort:', e);
      return;
    }

    if (xhr.status >= 200 && xhr.status < 300) {
      if (callback) callback(data);
    } else {
      console.error('HTTP-Fehler:', xhr.status);
      if (callback) callback(data);
    }
  };

  xhr.onerror = function() {
    console.error('Netzwerkfehler bei der Anfrage');
  };

  xhr.send(params ? params.toString() : null);
};

function sendFormData(url, formData, successCallback, errorCallback) {
  const xhr = new XMLHttpRequest();
  
  xhr.open('POST', url, true);
  
  xhr.withCredentials = true;
  
  xhr.onload = function() {
    if (xhr.status >= 200 && xhr.status < 300) {
      // Erfolgreich: JSON parsen und Callback aufrufen
      try {
        const data = JSON.parse(xhr.responseText);
        if (successCallback) {
          successCallback(data);
        }
      } catch (e) {
        // Fehler beim JSON-Parsing
        if (errorCallback) {
          errorCallback(new Error('Fehler beim Parsen der JSON-Antwort'));
        } else {
          console.error('Fehler beim Parsen der Antwort:', e);
        }
      }
    } else {
      if (errorCallback) {
        errorCallback(new Error('Netzwerkantwort war nicht ok: ' + xhr.status));
      } else {
        console.error('HTTP-Fehler:', xhr.status);
      }
    }
  };
  
  xhr.onerror = function() {
    if (errorCallback) {
      errorCallback(new Error('Netzwerkfehler bei der Anfrage'));
    } else {
      console.error('Netzwerkfehler bei der Anfrage');
    }
  };
  
  // Anfrage senden
  xhr.send(formData);
}

// Funktion zum Prüfen des Login-Status mit XMLHttpRequest
function checkLoginStatus(callback) {
  window.sendRequest('GET', 'api/check-login', null, callback);
}

function logout() {
  window.sendRequest('POST', 'logout', null, function() {
    window.location.href = 'index.html';
  });
}

// Funktion zum Umleiten nicht angemeldeter Benutzer
function redirectIfNotLoggedIn() {
  checkLoginStatus(function(data) {
    if (!data.loggedIn) {
      window.location.href = 'login.html?redirect=' + encodeURIComponent(window.location.href);
    }
  });
}

// Datum formatieren
function formatDate(dateString) {
  const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
  return new Date(dateString).toLocaleDateString('de-DE', options);
}

// Standardfunktion für alle Formulare
function setupForm(formId, submitHandler) {
  const form = document.getElementById(formId);
  if (form) {
    form.addEventListener('submit', function(e) {
      e.preventDefault();
      submitHandler(this);
    });
  }
} 