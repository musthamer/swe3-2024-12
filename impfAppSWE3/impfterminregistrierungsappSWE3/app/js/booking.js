function switchTab(evt, tabName) {
  const tabContent = document.getElementsByClassName('tabcontent');
  for (let i = 0; i < tabContent.length; i++) {
    tabContent[i].classList.remove('active');
  }
  const tabLinks = document.getElementsByClassName('tablinks');
  for (let i = 0; i < tabLinks.length; i++) {
    tabLinks[i].classList.remove('active');
  }
  document.getElementById(tabName).classList.add('active');
  if (evt) {
    evt.currentTarget.classList.add('active');
  } else {
    for (let i = 0; i < tabLinks.length; i++) {
      if (tabLinks[i].getAttribute('data-tab') === tabName) {
        tabLinks[i].classList.add('active');
        break;
      }
    }
  }
  if (tabName === 'tabAppointments') {
    loadAppointments();
  }
}

function loadVaccinationCenters() {
  window.sendRequest('GET', 'api/vaccination-centers', null, function(data) {
    const centerSelect = document.getElementById('center');
    if (!centerSelect) return;
    centerSelect.innerHTML = '<option value="">Bitte wählen</option>';
    if (data.success && data.centers && data.centers.length > 0) {
      data.centers.forEach(function(center) {
        const option = document.createElement('option');
        option.value = center.id;
        option.textContent = center.name + ' (' + center.address + ')';
        centerSelect.appendChild(option);
      });
    } else {
      centerSelect.innerHTML = '<option value="">Keine Zentren verfügbar</option>';
    }
  }, null, {
    onNetworkError: function() {
      const centerSelect = document.getElementById('center');
      if (centerSelect) centerSelect.innerHTML = '<option value="">Netzwerkfehler</option>';
    },
    onParseError: function() {
      const centerSelect = document.getElementById('center');
      if (centerSelect) centerSelect.innerHTML = '<option value="">Fehler</option>';
    }
  });
}

function loadVaccinesForCenter(centerId) {
  window.sendRequest('GET', 'api/vaccines?center_id=' + centerId, null, function(data) {
    const vaccineSelect = document.getElementById('vaccine');
    vaccineSelect.innerHTML = '<option value="">Bitte wählen</option>';
    if (data.success && data.vaccines && data.vaccines.length > 0) {
      data.vaccines.forEach(function(vaccine) {
        if (vaccine.availableDoses > 0) {
          const option = document.createElement('option');
          option.value = vaccine.id;
          option.textContent = vaccine.name + ' (verfügbar: ' + vaccine.availableDoses + ')';
          vaccineSelect.appendChild(option);
        }
      });
      vaccineSelect.disabled = false;
    } else {
      resetDropdown('vaccine');
    }
  }, null, {
    onNetworkError: function() { resetDropdown('vaccine'); },
    onParseError: function() { resetDropdown('vaccine'); }
  });
}

function loadTimeSlotsForCenter(centerId) {
  window.sendRequest('GET', 'api/timeslots?center_id=' + centerId, null, function(data) {
    const timeslotSelect = document.getElementById('timeslot');
    timeslotSelect.innerHTML = '<option value="">Bitte wählen</option>';
    if (data.success && data.timeslots && data.timeslots.length > 0) {
      let available = false;
      data.timeslots.forEach(function(slot) {
        const capacity = slot.capacity || 0;
        const booked = slot.bookedCount || 0;
        const remaining = (slot.remainingCapacity !== undefined) ? slot.remainingCapacity : (capacity - booked);
        if (remaining > 0) {
          const option = document.createElement('option');
          option.value = slot.id;
          const date = new Date(slot.startTime || slot.start_time);
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
  }, null, {
    onNetworkError: function() { resetDropdown('timeslot'); },
    onParseError: function() { resetDropdown('timeslot'); }
  });
}

function bookAppointment() {
  const form = document.getElementById('vaccineBookingForm');
  const formData = new FormData(form);
  window.sendRequest('POST', 'api/book-appointment', formData, function(data) {
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
  }, null, {
    onNetworkError: function() { showMessage('statusMessage', 'Netzwerkfehler', true); },
    onParseError: function() { showMessage('statusMessage', 'Verarbeitungsfehler', true); }
  });
}

function downloadConfirmationPDF(appointmentId) {
  window.sendRequest('GET', 'booking-pdf?id=' + appointmentId, null, function(blob, status) {
    if (status === 200) {
      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = 'Bestaetigung_' + appointmentId + '.pdf';
      link.click();
    } else {
      showMessage('statusMessage', 'Fehler beim Herunterladen der Bestätigung.', true);
    }
  }, null, {
    responseType: 'blob',
    onNetworkError: function() {
      showMessage('statusMessage', 'Netzwerkfehler beim Herunterladen der Bestätigung.', true);
    }
  });
}

function loadAppointments() {
  window.sendRequest('GET', 'api/appointments', null, function(data) {
    if (data.success && data.appointments) {
      displayAppointments(data.appointments);
    } else {
      document.getElementById('appointmentsList').innerHTML = '<p>Keine Termine gefunden.</p>';
    }
  }, null, {
    onNetworkError: function() {
      document.getElementById('appointmentsList').innerHTML = '<p>Netzwerkfehler beim Laden der Termine.</p>';
    },
    onParseError: function() {
      document.getElementById('appointmentsList').innerHTML = '<p>Fehler beim Laden der Termine.</p>';
    }
  });
}

function cancelAppointment(id) {
  if (!confirm('Möchten Sie diesen Termin wirklich stornieren?')) return;
  const formData = new FormData();
  formData.append('action', 'cancel');
  formData.append('id', id);
  window.sendRequest('POST', 'api/appointments', formData, function(data) {
    if (data.success) {
      displayAppointments(data.appointments);
      showMessage('statusMessage', 'Termin storniert.', false);
    } else {
      showMessage('statusMessage', data.message || 'Fehler beim Stornieren', true);
    }
  }, null, {
    onNetworkError: function() { showMessage('statusMessage', 'Netzwerkfehler', true); },
    onParseError: function() { showMessage('statusMessage', 'Verarbeitungsfehler', true); }
  });
}

function displayAppointments(appointments) {
  const container = document.getElementById('appointmentsList');
  if (!appointments || appointments.length === 0) {
    container.innerHTML = '<p>Keine gebuchten Termine.</p>';
    return;
  }
  let html = '<table class="appointments-table"><thead><tr><th>Patient</th><th>Datum</th><th>Uhrzeit</th><th>Zentrum</th><th>Impfstoff</th><th>Status</th><th>Aktion</th></tr></thead><tbody>';
  appointments.forEach(function(appointment) {
    const date = appointment.startTime ? appointment.startTime.split(' ')[0] : 'Unbekannt';
    const time = appointment.startTime ? appointment.startTime.split(' ')[1].substring(0, 5) : 'Unbekannt';
    const status = appointment.status || 'UNBEKANNT';
    const patientName = ((appointment.firstName || '') + ' ' + (appointment.lastName || '')).trim() || 'Unbekannt';
    const isActive = (status === 'BOOKED' || status === 'CONFIRMED');
    let actions = '';
    if (isActive) {
      actions += '<button class="cancel-button" onclick="cancelAppointment(' + appointment.id + ')">Stornieren</button>';
    }
    if (appointment.status === 'CONFIRMED') {
      actions += ' <button class="download-button" onclick="downloadConfirmationPDF(' + appointment.id + ')">PDF herunterladen</button>';
    }
    if (actions === '') {
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

function resetDropdown(id) {
  const sel = document.getElementById(id);
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

function applyLoginState(data) {
  const loginRequired = document.getElementById('loginRequired');
  const dashboardContainer = document.getElementById('dashboardContainer');
  const userNameEl = document.getElementById('userName');

  if (data && data.loggedIn) {
    if (loginRequired) loginRequired.classList.add('hidden');
    if (dashboardContainer) dashboardContainer.classList.remove('hidden');
    if (userNameEl) userNameEl.textContent = 'Angemeldet als: ' + (data.userName || data.email || 'Benutzer');
    applyUserProfile(data);
    loadVaccinationCenters();
  } else {
    if (loginRequired) loginRequired.classList.remove('hidden');
    if (dashboardContainer) dashboardContainer.classList.add('hidden');
  }
}

function initBookingUI(loginData) {
  if (window.__bookingUiInitialized) return;
  window.__bookingUiInitialized = true;

  document.querySelectorAll('.tablinks[data-tab]').forEach(function(btn) {
    btn.addEventListener('click', function(e) {
      const tabId = btn.getAttribute('data-tab');
      if (tabId) switchTab(e, tabId);
    });
  });

  applyLoginState(loginData);

  setupBookingForToggle();

  const logoutLink = document.getElementById('logoutLink');
  if (logoutLink) {
    logoutLink.addEventListener('click', function(e) {
      e.preventDefault();
      window.location.hash = '#/logout';
    });
  }

  const bookingForm = document.getElementById('vaccineBookingForm');
  if (bookingForm) {
    bookingForm.addEventListener('submit', function(e) {
      e.preventDefault();
      bookAppointment();
    });
  }

  const centerSelect = document.getElementById('center');
  if (centerSelect) {
    centerSelect.addEventListener('change', function() {
      const centerId = this.value;
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
