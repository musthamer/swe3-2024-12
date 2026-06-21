function switchTab(evt, tabName) {
  let tabContent = document.getElementsByClassName("tabcontent");
  for (let i = 0; i < tabContent.length; i++) {
    tabContent[i].classList.remove("active");
  }
  let tabLinks = document.getElementsByClassName("tablinks");
  for (let i = 0; i < tabLinks.length; i++) {
    tabLinks[i].classList.remove("active");
  }
  document.getElementById(tabName).classList.add("active");
  if (evt) {
    evt.currentTarget.classList.add("active");
  } else {
    for (let i = 0; i < tabLinks.length; i++) {
      if (tabLinks[i].getAttribute("onclick").includes(tabName)) {
        tabLinks[i].classList.add("active");
        break;
      }
    }
  }
  if (tabName === 'tabAppointments') {
    loadAppointments(); 
  }
}

// Nur definieren, falls nicht bereits zentral vorhanden (aus main.js)
if (typeof checkLoginStatus !== 'function') {
  function checkLoginStatus(callback) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', 'api/check-login', true);
    xhr.withCredentials = true;
    xhr.onload = function() {
      var data = { loggedIn: false };
      try { data = JSON.parse(xhr.responseText); } catch (e) {}
      callback(data);
    };
    xhr.onerror = function() { callback({ loggedIn: false }); };
    xhr.send();
  }
}

function loadVaccinationCenters() {
  let xhr = new XMLHttpRequest();
  xhr.open('GET', 'api/vaccination-centers', true);
  xhr.withCredentials = true;
  xhr.onload = function() {
    try {
      let data = JSON.parse(xhr.responseText);
      let centerSelect = document.getElementById('center');
      centerSelect.innerHTML = '<option value="">Bitte wählen</option>';
      if (data.success && data.centers && data.centers.length > 0) {
        data.centers.forEach(function(center) {
          let option = document.createElement('option');
          option.value = center.id;
          option.textContent = center.name + ' (' + center.address + ')';
          centerSelect.appendChild(option);
        });
      } else {
        centerSelect.innerHTML = '<option value="">Keine Zentren verfügbar</option>';
      }
    } catch (e) {
      document.getElementById('center').innerHTML = '<option value="">Fehler</option>';
    }
  };
  xhr.onerror = function() {
    document.getElementById('center').innerHTML = '<option value="">Netzwerkfehler</option>';
  };
  xhr.send();
}

function loadVaccinesForCenter(centerId) {
  let xhr = new XMLHttpRequest();
  xhr.open('GET', 'api/vaccines?center_id=' + centerId, true);
  xhr.withCredentials = true;
  xhr.onload = function() {
    try {
      let data = JSON.parse(xhr.responseText);
      let vaccineSelect = document.getElementById('vaccine');
      vaccineSelect.innerHTML = '<option value="">Bitte wählen</option>';
      if (data.success && data.vaccines && data.vaccines.length > 0) {
        data.vaccines.forEach(function(vaccine) {
          if (vaccine.availableDoses > 0) {
            let option = document.createElement('option');
            option.value = vaccine.id;
            option.textContent = vaccine.name + ' (verfügbar: ' + vaccine.availableDoses + ')';
            vaccineSelect.appendChild(option);
          }
        });
        vaccineSelect.disabled = false;
      } else {
        resetDropdown('vaccine');
      }
    } catch (e) {
      resetDropdown('vaccine');
    }
  };
  xhr.onerror = function() {
    resetDropdown('vaccine');
  };
  xhr.send();
}

function loadTimeSlotsForCenter(centerId) {
  let xhr = new XMLHttpRequest();
  xhr.open('GET', 'api/timeslots?center_id=' + centerId, true);
  xhr.withCredentials = true;
  xhr.onload = function() {
    try {
      let data = JSON.parse(xhr.responseText);
      let timeslotSelect = document.getElementById('timeslot');
      timeslotSelect.innerHTML = '<option value="">Bitte wählen</option>';
      if (data.success && data.timeslots && data.timeslots.length > 0) {
        let available = false;
        data.timeslots.forEach(function(slot) {
          let capacity = slot.capacity || 0;
          let booked = slot.bookedCount || 0;
          let remaining = (slot.remainingCapacity !== undefined) ? slot.remainingCapacity : (capacity - booked);
          if (remaining > 0) {
            let option = document.createElement('option');
            option.value = slot.id;
            let date = new Date(slot.startTime || slot.start_time);
            option.textContent = date.toLocaleDateString('de-DE') + ', ' +
              date.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' }) +
              ' Uhr (' + remaining + ' Plätze frei)';
            timeslotSelect.appendChild(option);
            available = true;
          }
        });
        if (!available) {
          timeslotSelect.innerHTML = '<option value="">Keine freien Termine</option>';
          timeslotSelect.disabled = true;
        } else {
          timeslotSelect.disabled = false;
        }
      } else {
        resetDropdown('timeslot');
      }
    } catch (e) {
      resetDropdown('timeslot');
    }
  };
  xhr.onerror = function() {
    resetDropdown('timeslot');
  };
  xhr.send();
}

function bookAppointment() {
  let form = document.getElementById('vaccineBookingForm');
  let formData = new FormData(form);
  let xhr = new XMLHttpRequest();
  xhr.open('POST', 'api/book-appointment', true);
  xhr.withCredentials = true;
  xhr.onload = function() {
    try {
      let data = JSON.parse(xhr.responseText);
      if (data.success) {
        showMessage('statusMessage', 'Termin gebucht.', false);
        form.reset();
        resetBookingForSelf();
        resetDropdown('vaccine');
        resetDropdown('timeslot');
        switchTab(null, 'tabAppointments');
      } else {
        showMessage('statusMessage', data.message || 'Fehler', true);
      }
    } catch (e) {
      showMessage('statusMessage', 'Verarbeitungsfehler', true);
    }
  };
  xhr.onerror = function() {
    showMessage('statusMessage', 'Netzwerkfehler', true);
  };
  xhr.send(formData);
}

function downloadConfirmationPDF(appointmentId) {
  let xhr = new XMLHttpRequest();
  xhr.open('GET', 'booking-pdf?id=' + appointmentId, true);
  xhr.withCredentials = true;
  xhr.responseType = 'blob'; // Wir erwarten einen PDF-Blob
  xhr.onload = function() {
    if (xhr.status === 200) {
      let blob = new Blob([xhr.response], { type: 'application/pdf' });
      let link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = "Bestaetigung_" + appointmentId + ".pdf";
      link.click();
    } else {
      showMessage('statusMessage', 'Fehler beim Herunterladen der Bestätigung.', true);
    }
  };
  xhr.onerror = function() {
    showMessage('statusMessage', 'Netzwerkfehler beim Herunterladen der Bestätigung.', true);
  };
  xhr.send();
}

function loadAppointments() {
  let xhr = new XMLHttpRequest();
  xhr.open('GET', 'api/appointments', true);
  xhr.withCredentials = true;
  xhr.onload = function() {
    try {
      let data = JSON.parse(xhr.responseText);
      if (data.success && data.appointments) {
        displayAppointments(data.appointments);
      } else {
        document.getElementById('appointmentsList').innerHTML = '<p>Keine Termine gefunden.</p>';
      }
    } catch (e) {
      document.getElementById('appointmentsList').innerHTML = '<p>Fehler beim Laden der Termine: ' + e + '</p>';
    }
  };
  xhr.onerror = function() {
    document.getElementById('appointmentsList').innerHTML = '<p>Netzwerkfehler beim Laden der Termine.</p>';
  };
  xhr.send();
}

function cancelAppointment(id) {
  if (!confirm('Möchten Sie diesen Termin wirklich stornieren?')) return;
  let formData = new FormData();
  formData.append('action', 'cancel');
  formData.append('id', id);
  let xhr = new XMLHttpRequest();
  xhr.open('POST', 'api/appointments', true);
  xhr.withCredentials = true;
  xhr.onload = function() {
    try {
      let data = JSON.parse(xhr.responseText);
      if (data.success) {
        displayAppointments(data.appointments);
        showMessage('statusMessage', 'Termin storniert.', false);
      } else {
        showMessage('statusMessage', data.message || 'Fehler beim Stornieren', true);
      }
    } catch (e) {
      showMessage('statusMessage', 'Verarbeitungsfehler', true);
    }
  };
  xhr.onerror = function() {
    showMessage('statusMessage', 'Netzwerkfehler', true);
  };
  xhr.send(formData);
}

function displayAppointments(appointments) {
  let container = document.getElementById('appointmentsList');
  if (!appointments || appointments.length === 0) {
    container.innerHTML = '<p>Keine gebuchten Termine.</p>';
    return;
  }
  let html = '<table class="appointments-table"><thead><tr><th>Patient</th><th>Datum</th><th>Uhrzeit</th><th>Zentrum</th><th>Impfstoff</th><th>Status</th><th>Aktion</th></tr></thead><tbody>';
  appointments.forEach(function(appointment) {
    let date = appointment.startTime ? appointment.startTime.split(' ')[0] : 'Unbekannt';
    let time = appointment.startTime ? appointment.startTime.split(' ')[1].substring(0, 5) : 'Unbekannt';
    let status = appointment.status || 'UNBEKANNT';
    let patientName = ((appointment.firstName || '') + ' ' + (appointment.lastName || '')).trim() || 'Unbekannt';
    let isActive = (status === 'BOOKED' || status === 'CONFIRMED');
    let actions = "";
    if (isActive) {
      actions += '<button class="cancel-button" onclick="cancelAppointment(' + appointment.id + ')">Stornieren</button>';
    }
    if (appointment.status === 'CONFIRMED') {
      actions += ' <button class="download-button" onclick="downloadConfirmationPDF(' + appointment.id + ')">PDF herunterladen</button>';
    }
    if (actions === "") {
      actions = '-';
    }
    html += '<tr>' +
              '<td>' + patientName + '</td>' +
              '<td>' + date + '</td>' +
              '<td>' + time + '</td>' +
              '<td>' + (appointment.centerName || 'Unbekannt') + '</td>' +
              '<td>' + (appointment.vaccineName || 'Unbekannt') + '</td>' +
              '<td>' + translateStatus(status) + '</td>' +
              '<td>' + actions + '</td>' +
            '</tr>';
  });
  html += '</tbody></table>';
  container.innerHTML = html;
}

function translateStatus(status) {
  switch (status) {
    case 'BOOKED': return 'Gebucht';
    case 'CONFIRMED': return 'Bestätigt';
    case 'COMPLETED': return 'Abgeschlossen';
    case 'CANCELLED': return 'Storniert';
    default: return status;
  }
}

if (typeof showMessage !== 'function') {
  function showMessage(elementId, message, isError) {
    var el = document.getElementById(elementId);
    el.innerHTML = '<div class="' + (isError ? 'error' : 'success') + '-message">' + message + '</div>';
    setTimeout(function() { el.innerHTML = ''; }, 3000);
  }
}

function resetDropdown(id) {
  let sel = document.getElementById(id);
  sel.innerHTML = '<option value="">Bitte wählen</option>';
  sel.disabled = true;
}

let cachedUserProfile = null;

function applyUserProfile(profile) {
  if (!profile) return;
  cachedUserProfile = profile;
  const emailDisplay = document.getElementById('accountEmailDisplay');
  if (emailDisplay) {
    emailDisplay.textContent = profile.email || '';
  }
  if (isBookingForSelf()) {
    fillSelfProfileFields(profile);
  }
}

function isBookingForSelf() {
  const selfRadio = document.getElementById('bookingForSelf');
  return !selfRadio || selfRadio.checked;
}

function fillSelfProfileFields(profile) {
  const firstName = document.getElementById('firstName');
  const lastName = document.getElementById('lastName');
  const dateOfBirth = document.getElementById('dateOfBirth');
  if (firstName) firstName.value = profile.firstName || '';
  if (lastName) lastName.value = profile.lastName || '';
  if (dateOfBirth) dateOfBirth.value = profile.dateOfBirth || '';
  setPersonFieldsReadOnly(true);
}

function clearPersonFields() {
  const firstName = document.getElementById('firstName');
  const lastName = document.getElementById('lastName');
  const dateOfBirth = document.getElementById('dateOfBirth');
  if (firstName) firstName.value = '';
  if (lastName) lastName.value = '';
  if (dateOfBirth) dateOfBirth.value = '';
  setPersonFieldsReadOnly(false);
}

function setPersonFieldsReadOnly(readonly) {
  ['firstName', 'lastName', 'dateOfBirth'].forEach(function(id) {
    const el = document.getElementById(id);
    if (el) el.readOnly = readonly;
  });
}

function resetBookingForSelf() {
  const selfRadio = document.getElementById('bookingForSelf');
  if (selfRadio) selfRadio.checked = true;
  if (cachedUserProfile) {
    fillSelfProfileFields(cachedUserProfile);
  }
}

function setupBookingForToggle() {
  const selfRadio = document.getElementById('bookingForSelf');
  const otherRadio = document.getElementById('bookingForOther');
  if (!selfRadio || !otherRadio) return;

  function onChange() {
    if (isBookingForSelf()) {
      if (cachedUserProfile) {
        fillSelfProfileFields(cachedUserProfile);
      }
    } else {
      clearPersonFields();
    }
  }

  selfRadio.addEventListener('change', onChange);
  otherRadio.addEventListener('change', onChange);
}

function initBookingUI() {
  // In der SPA kann booking.js mehrfach geladen/initialisiert werden.
  if (window.__bookingUiInitialized) return;
  window.__bookingUiInitialized = true;

  // Tab-Buttons robust binden (Inline-onclick bleibt als Fallback)
  const tabButtons = document.querySelectorAll('.tablinks[data-tab]');
  tabButtons.forEach(function(btn) {
    btn.addEventListener('click', function(e) {
      const tabId = btn.getAttribute('data-tab');
      if (tabId) switchTab(e, tabId);
    });
  });

  const loginRequired = document.getElementById('loginRequired');
  const dashboardContainer = document.getElementById('dashboardContainer');
  const userNameEl = document.getElementById('userName');
  const logoutLink = document.getElementById('logoutLink');
  const bookingForm = document.getElementById('vaccineBookingForm');
  const centerSelect = document.getElementById('center');

  if (typeof checkLoginStatus === 'function') {
    checkLoginStatus(function(data) {
      if (data && data.loggedIn) {
        if (loginRequired) loginRequired.style.display = 'none';
        if (dashboardContainer) dashboardContainer.style.display = 'block';
        if (userNameEl) userNameEl.textContent = 'Angemeldet als: ' + (data.userName || data.email || 'Benutzer');
        applyUserProfile(data);
        if (typeof loadVaccinationCenters === 'function') loadVaccinationCenters();
      } else {
        if (loginRequired) loginRequired.style.display = 'block';
        if (dashboardContainer) dashboardContainer.style.display = 'none';
      }
    });
  }

  setupBookingForToggle();

  if (logoutLink) {
    logoutLink.addEventListener('click', function(e) {
      e.preventDefault();
      window.location.href = 'logout';
    });
  }

  if (bookingForm) {
    bookingForm.addEventListener('submit', function(e) {
      e.preventDefault();
      bookAppointment();
    });
  }

  if (centerSelect) {
    centerSelect.addEventListener('change', function() {
      let centerId = this.value;
      if (centerId) {
        loadVaccinesForCenter(centerId);
        loadTimeSlotsForCenter(centerId);
      } else {
        resetDropdown('vaccine');
        resetDropdown('timeslot');
      }
    });
  }
}

document.addEventListener('DOMContentLoaded', function() {
  initBookingUI();
});
