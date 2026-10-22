package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.Gender;
import com.littlehorizons.erp.model.enums.StudentStatus;
import java.time.LocalDate;
import java.time.Period;

/**
 * Estudiante matriculado en el colegio.
 * <p>Tabla: {@code estudiantes}</p>
 */
public class Student extends Person {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla estudiantes) ----------
    // Fecha de nacimiento (sirve para calcular la edad)
    private LocalDate birthDate;
    // Género (M, F u Otro)
    private Gender gender;
    // Dirección de residencia
    private String address;
    // Tipo de sangre, por ejemplo O+
    private String bloodType;
    // Alergias conocidas (texto libre)
    private String allergies;
    // Condiciones médicas relevantes (texto libre)
    private String medicalConditions;
    // Fecha en que ingresó al colegio
    private LocalDate admissionDate;
    // Estado del estudiante: Activo, Graduado, Retirado o Suspendido
    private StudentStatus status;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve la fecha de nacimiento. */
    public LocalDate getBirthDate() {
        return birthDate;
    }

    /** Asigna la fecha de nacimiento. */
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /** Devuelve el género. */
    public Gender getGender() {
        return gender;
    }

    /** Asigna el género. */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /** Devuelve la dirección. */
    public String getAddress() {
        return address;
    }

    /** Asigna la dirección. */
    public void setAddress(String address) {
        this.address = address;
    }

    /** Devuelve el tipo de sangre. */
    public String getBloodType() {
        return bloodType;
    }

    /** Asigna el tipo de sangre. */
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    /** Devuelve las alergias. */
    public String getAllergies() {
        return allergies;
    }

    /** Asigna las alergias. */
    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    /** Devuelve las condiciones médicas. */
    public String getMedicalConditions() {
        return medicalConditions;
    }

    /** Asigna las condiciones médicas. */
    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    /** Devuelve la fecha de ingreso. */
    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    /** Asigna la fecha de ingreso. */
    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    /** Devuelve el estado. */
    public StudentStatus getStatus() {
        return status;
    }

    /** Asigna el estado. */
    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    // ---------- Método de ayuda ----------

    /**
     * Calcula la edad en años cumplidos a una fecha.
     *
     * @param date fecha de referencia (normalmente hoy)
     * @return edad en años completos
     */
    public int getAgeAt(LocalDate date) {
        // Period.between calcula la diferencia entre dos fechas; getYears() son los años completos
        return Period.between(birthDate, date).getYears();
    }
}
