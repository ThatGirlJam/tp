package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PATIENT = "Patients list contains duplicate patient(s).";
    public static final String MESSAGE_DUPLICATE_APPOINTMENT = "Appointment list contains duplicate appointment(s)";
    public static final String MESSAGE_OVERLAPPING_APPOINTMENT =
            "Appointment list contains overlapping appointment(s) for the same patient on the same date";
    public static final String MESSAGE_NRIC_DOES_NOT_EXIST =
            "Appointment list contains appointment(s) with NRIC that does not belong to any patient";


    private final List<JsonAdaptedPatient> patients = new ArrayList<>();
    private final List<JsonAdaptedAppointment> appointments = new ArrayList<>();


    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given patients.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("patients") List<JsonAdaptedPatient> patients,
                                       @JsonProperty("appointments") List<JsonAdaptedAppointment> appointments) {
        this.patients.addAll(patients);
        if (appointments != null) {
            this.appointments.addAll(appointments);
        }
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        patients.addAll(source.getPatientList().stream().map(JsonAdaptedPatient::new).collect(Collectors.toList()));
        appointments.addAll(source.getAppointmentList().stream()
                .map(JsonAdaptedAppointment::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedPatient jsonAdaptedPatient : patients) {
            Patient patient = jsonAdaptedPatient.toModelType();
            if (addressBook.hasPatientWithNric(patient.getNric())) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PATIENT);
            }
            addressBook.addPatient(patient);
        }
        for (JsonAdaptedAppointment jsonAdaptedAppointment : appointments) {
            Appointment appointment = jsonAdaptedAppointment.toModelType();
            if (addressBook.hasAppointment(appointment)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_APPOINTMENT);
            }
            if (addressBook.samePatientHasOverlappingAppointment(appointment)) {
                throw new IllegalValueException(MESSAGE_OVERLAPPING_APPOINTMENT);
            }
            if (!addressBook.hasPatientWithNric(appointment.getNric())) {
                throw new IllegalValueException(MESSAGE_NRIC_DOES_NOT_EXIST);
            }
            addressBook.addAppointment(appointment);
        }
        return addressBook;
    }

}
