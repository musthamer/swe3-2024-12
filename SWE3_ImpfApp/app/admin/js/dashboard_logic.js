const API_BASE = '../api';

function xhrGetJson(url, onDone) {
  const xhr = new XMLHttpRequest();
  xhr.open('GET', url, true);
  xhr.withCredentials = true;
  xhr.onload = function () {
    let data;
    try {
      data = JSON.parse(xhr.responseText);
    } catch (e) {
      onDone(e, null, xhr.status);
      return;
    }
    onDone(null, data, xhr.status);
  };
  xhr.onerror = function () {
    onDone(new Error('Netzwerkfehler'), null, 0);
  };
  xhr.send();
}

function xhrPostForm(url, params, onDone) {
  const xhr = new XMLHttpRequest();
  xhr.open('POST', url, true);
  xhr.withCredentials = true;
  xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
  xhr.onload = function () {
    let data;
    try {
      data = JSON.parse(xhr.responseText);
    } catch (e) {
      onDone(e, null, xhr.status);
      return;
    }
    onDone(null, data, xhr.status);
  };
  xhr.onerror = function () {
    onDone(new Error('Netzwerkfehler'), null, 0);
  };
  xhr.send(params.toString());
}

function openTab(evt, tabId) {
  const tablinks = document.getElementsByClassName('tablinks');
  let i;
  for (i = 0; i < tablinks.length; i++) {
    tablinks[i].classList.remove('active');
  }
  const tabcontents = document.getElementsByClassName('tabcontent');
  for (i = 0; i < tabcontents.length; i++) {
    tabcontents[i].classList.remove('active');
  }

  if (evt && evt.currentTarget) {
    evt.currentTarget.classList.add('active');
  } else {
    for (i = 0; i < tablinks.length; i++) {
      const oc = tablinks[i].getAttribute('onclick') || '';
      if (oc.indexOf(tabId) !== -1) {
        tablinks[i].classList.add('active');
        break;
      }
    }
  }

  document.getElementById(tabId).classList.add('active');
}

window.addEventListener('DOMContentLoaded', function () {
  loadCenters();

  document.getElementById('createCenterForm').addEventListener('submit', createCenter);
  document.getElementById('updateInventoryForm').addEventListener('submit', updateInventory);
  document.getElementById('centerSelect').addEventListener('change', loadVaccines);
  document.getElementById('confirmAppointmentForm').addEventListener('submit', searchAppointment);

  processUrlParameters();
});

function processUrlParameters() {
  const urlParams = new URLSearchParams(window.location.search);
  const tabId = urlParams.get('tab');
  if (tabId && document.getElementById(tabId)) {
    openTab(null, tabId);
  }

  const bookingId = urlParams.get('id');
  if (bookingId && tabId === 'tabTermine') {
    document.getElementById('bookingId').value = bookingId;
    searchAppointment(new Event('submit'));
  }
}

function loadCenters() {
  xhrGetJson(API_BASE + '/vaccination-centers', function (err, data) {
    if (err) {
      console.error(err);
      document.getElementById('centersList').innerText = 'Fehler beim Laden.';
      return;
    }
    if (data.success && data.centers) {
      renderCentersList(data.centers);
      populateCenterSelect(data.centers);
    } else {
      document.getElementById('centersList').innerText = 'Keine Impfzentren verfügbar.';
    }
  });
}

function renderCentersList(centers) {
  const container = document.getElementById('centersList');
  if (!centers.length) {
    container.innerHTML = 'Keine Impfzentren verfügbar.';
    return;
  }
  let html = '<table><tr><th>Name</th><th>Adresse</th></tr>';
  centers.forEach(function (c) {
    html += '<tr><td>' + c.name + '</td><td>' + c.address + '</td></tr>';
  });
  html += '</table>';
  container.innerHTML = html;
}

function populateCenterSelect(centers) {
  const select = document.getElementById('centerSelect');
  while (select.options.length > 1) {
    select.remove(1);
  }
  centers.forEach(function (c) {
    const opt = document.createElement('option');
    opt.value = c.id;
    opt.textContent = c.name + ' - ' + c.address;
    select.appendChild(opt);
  });
}

function createCenter(e) {
  e.preventDefault();
  const formData = new FormData(e.target);
  const params = new URLSearchParams();
  params.append('action', 'create-center');
  params.append('name', formData.get('name'));
  params.append('address', formData.get('address'));

  xhrPostForm(API_BASE + '/admin', params, function (err, data) {
    if (err) {
      console.error(err);
      alert('Fehler beim Erstellen des Impfzentrums.');
      return;
    }
    if (data.success) {
      alert('Impfzentrum erfolgreich erstellt.');
      e.target.reset();
      loadCenters();
    } else {
      alert('Fehler beim Erstellen: ' + (data.message || ''));
    }
  });
}

function loadCurrentInventory(centerId, thenFn) {
  const inventoryDiv = document.getElementById('currentInventory');
  inventoryDiv.innerText = 'Lade aktuellen Impfbestand...';

  xhrGetJson(API_BASE + '/vaccine-inventory?center_id=' + encodeURIComponent(centerId), function (err, data) {
    if (err) {
      console.error(err);
      inventoryDiv.innerText = 'Fehler beim Laden des Impfbestands.';
      if (thenFn) thenFn();
      return;
    }
    if (data.vaccines && data.vaccines.length > 0) {
      let html = '<table><tr><th>Impfstoff</th><th>Hersteller</th><th>Dosen</th></tr>';
      data.vaccines.forEach(function (v) {
        html += '<tr><td>' + v.name + '</td><td>' + (v.manufacturer || '') + '</td><td>' + (v.availableDoses || 0) + '</td></tr>';
      });
      html += '</table>';
      inventoryDiv.innerHTML = html;
    } else {
      inventoryDiv.innerText = 'Keine Daten zum Impfbestand verfügbar.';
    }
    if (thenFn) thenFn();
  });
}

function loadVaccines() {
  const centerId = document.getElementById('centerSelect').value;
  const vaccineSelect = document.getElementById('vaccineSelect');
  const inventoryDiv = document.getElementById('currentInventory');
  if (!centerId) {
    vaccineSelect.innerHTML = '<option value="">-- Bitte wählen --</option>';
    inventoryDiv.innerText = 'Bitte wähle ein Impfzentrum.';
    return;
  }
  vaccineSelect.innerHTML = '<option value="">Lade Impfstoffe...</option>';

  loadCurrentInventory(centerId, function () {
    xhrGetJson(API_BASE + '/vaccines?center_id=' + encodeURIComponent(centerId), function (err, data) {
      if (err) {
        console.error(err);
        vaccineSelect.innerHTML = '<option value="">Fehler beim Laden</option>';
        return;
      }
      if (data.success && data.vaccines) {
        vaccineSelect.innerHTML = '<option value="">-- Bitte wählen --</option>';
        data.vaccines.forEach(function (v) {
          const opt = document.createElement('option');
          opt.value = v.id;
          opt.textContent = v.name + ' (verfügbar: ' + (v.availableDoses || 0) + ')';
          vaccineSelect.appendChild(opt);
        });
      } else {
        vaccineSelect.innerHTML = '<option value="">Keine Impfstoffe verfügbar</option>';
      }
    });
  });
}

function updateInventory(e) {
  e.preventDefault();
  const formData = new FormData(e.target);
  const centerId = formData.get('center_id');
  const params = new URLSearchParams();
  params.append('action', 'update-inventory');
  params.append('center_id', centerId);
  params.append('vaccine_id', formData.get('vaccine_id'));
  params.append('doses', formData.get('doses'));

  xhrPostForm(API_BASE + '/admin', params, function (err, data) {
    if (err) {
      console.error(err);
      alert('Fehler beim Aktualisieren des Impfbestands.');
      return;
    }
    if (data.success) {
      alert('Impfbestand erfolgreich aktualisiert.');
      e.target.reset();
      if (centerId) {
        document.getElementById('centerSelect').value = centerId;
        loadVaccines();
      }
    } else {
      alert('Fehler beim Aktualisieren: ' + (data.message || ''));
    }
  });
}

function searchAppointment(e) {
  if (e) e.preventDefault();

  const bookingIdInput = document.getElementById('bookingId');
  const urlParams = new URLSearchParams(window.location.search);
  const urlBookingId = urlParams.get('id');
  const bookingId = (bookingIdInput && bookingIdInput.value) ? bookingIdInput.value : null;
  const idToSearch = bookingId || urlBookingId;
  const detailsEl = document.getElementById('appointmentDetails');

  if (!idToSearch) {
    detailsEl.innerHTML = '<div class="error">Bitte geben Sie eine Buchungs-ID ein oder scannen Sie einen QR-Code.</div>';
    return;
  }

  xhrGetJson(API_BASE + '/appointment-details?id=' + encodeURIComponent(idToSearch), function (err, data, status) {
    if (err) {
      detailsEl.innerHTML = '<div class="error">Fehler beim Laden der Termindaten.</div>';
      return;
    }
    if (status === 401 || status === 403) {
      detailsEl.innerHTML = '<div class="error">Sie sind nicht angemeldet oder haben keine Berechtigung.</div>';
      return;
    }
    if (data.success && data.appointment) {
      displayAppointmentDetails(data.appointment);
    } else {
      detailsEl.innerHTML = '<div class="error">' + (data.message || 'Termin nicht gefunden.') + '</div>';
    }
  });
}

function displayAppointmentDetails(appointment) {
  const detailsContainer = document.getElementById('appointmentDetails');

  let statusClass = 'status-pending';
  let statusText = 'Ausstehend';
  if (appointment.status === 'CONFIRMED') {
    statusClass = 'status-confirmed';
    statusText = 'Bestätigt';
  } else if (appointment.status === 'COMPLETED') {
    statusClass = 'status-completed';
    statusText = 'Durchgeführt';
  } else if (appointment.status === 'CANCELLED') {
    statusClass = 'status-cancelled';
    statusText = 'Storniert';
  }

  const appointmentDate = new Date(appointment.startTime);
  const dateFormatted =
    appointmentDate.toLocaleDateString('de-DE') + ' ' +
    appointmentDate.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' });

  let html =
    '<table class="appointment-details">' +
    '<tr><th>Name:</th><td>' + appointment.firstName + ' ' + appointment.lastName + '</td></tr>' +
    '<tr><th>Termin:</th><td>' + dateFormatted + '</td></tr>' +
    '<tr><th>Impfzentrum:</th><td>' + appointment.centerName + '</td></tr>' +
    '<tr><th>Impfstoff:</th><td>' + appointment.vaccineName + '</td></tr>' +
    '<tr><th>Status:</th><td><span class="' + statusClass + '">' + statusText + '</span></td></tr>' +
    '</table>';

  if (appointment.status === 'CONFIRMED') {
    html +=
      '<button onclick="completeAppointment(' + appointment.id + ')" class="confirm-button">' +
      'Impfung als durchgeführt markieren</button><div id="completion-result"></div>';
  } else if (appointment.status === 'COMPLETED') {
    html += '<div class="success-message">Dieser Termin wurde bereits als durchgeführt markiert.</div>';
  } else if (appointment.status === 'CANCELLED') {
    html += '<div class="error-message">Dieser Termin wurde storniert und kann nicht bestätigt werden.</div>';
  }

  detailsContainer.innerHTML = html;
}

function completeAppointment(bookingId) {
  const resultContainer = document.getElementById('completion-result');
  if (resultContainer) {
    resultContainer.innerHTML = 'Bestätige Termin...';
  }

  const params = new URLSearchParams();
  params.append('action', 'complete-appointment');
  params.append('booking_id', bookingId);

  xhrPostForm(API_BASE + '/admin', params, function (err, data) {
    if (err) {
      console.error(err);
      if (resultContainer) {
        resultContainer.innerHTML = '<div class="error-message">Netzwerkfehler bei der Bestätigung</div>';
      }
      return;
    }
    if (data.success) {
      if (resultContainer) {
        resultContainer.innerHTML = '<div class="success-message">Termin erfolgreich als durchgeführt markiert!</div>';
      }
      document.getElementById('bookingId').value = bookingId;
      searchAppointment(new Event('submit'));
    } else {
      if (resultContainer) {
        resultContainer.innerHTML = '<div class="error-message">Fehler: ' + (data.message || 'Termin konnte nicht aktualisiert werden') + '</div>';
      }
    }
  });
}