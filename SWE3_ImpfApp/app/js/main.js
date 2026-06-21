// Gemeinsame Hilfsfunktionen
function showMessage(elementId, message, isError = false) {
  const element = document.getElementById(elementId);
  if (element) {
    element.innerHTML = `<div class="${isError ? 'error-message' : 'success-message'}">${message}</div>`;
  }
}

window.sendRequest = function(method, url, params, callback, extraHeaders, options) {
  options = options || {};
  const xhr = new XMLHttpRequest();
  xhr.open(method, url, true);
  xhr.withCredentials = true;

  if (options.responseType) {
    xhr.responseType = options.responseType;
  }

  const isFormData = params instanceof FormData;
  if (method === 'POST' && !isFormData) {
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
  }
  if (extraHeaders) {
    Object.keys(extraHeaders).forEach(function(key) {
      xhr.setRequestHeader(key, extraHeaders[key]);
    });
  }

  xhr.onload = function() {
    if (options.responseType === 'blob') {
      if (callback) {
        callback(xhr.response, xhr.status);
      }
      return;
    }

    let data;
    try {
      data = JSON.parse(xhr.responseText);
    } catch (e) {
      console.error('Fehler beim Parsen der Antwort:', e);
      if (options.onParseError) {
        options.onParseError();
      }
      return;
    }

    if (callback) {
      callback(data, xhr.status);
    }
  };

  xhr.onerror = function() {
    console.error('Netzwerkfehler bei der Anfrage');
    if (options.onNetworkError) {
      options.onNetworkError();
    }
  };

  xhr.send(isFormData ? params : (params ? params.toString() : null));
};

function checkLoginStatus(callback) {
  window.sendRequest('GET', 'api/check-login', null, callback);
}

window.navigateAfterLogin = function(data) {
  if (data.redirectUrl === 'admin' || (data.redirectUrl && data.redirectUrl.endsWith('/admin'))) {
    window.location.href = 'admin';
    return;
  }
  window.location.hash = '#/booking';
};
