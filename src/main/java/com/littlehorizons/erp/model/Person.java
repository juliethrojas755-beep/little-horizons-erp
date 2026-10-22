package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.DocumentType;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Datos de identidad comunes a estudiantes, acudientes y profesores.
 * Es una clase abstracta: no se puede crear una Person sola, solo sus hijas (herencia).
 */
public abstract class Person {

    // ---------- Atributos ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Tipo de documento (TI, CC, CE o Pasaporte)
    private DocumentType documentType;
    // Número de documento (es único en la base de datos)
    private String documentNumber;
    // Primer nombre (obligatorio)
    private String firstName;
    // Segundo nombre (opcional, puede ser null)
    private String middleName;
    // Primer apellido (obligatorio)
    private String lastName;
    // Segundo apellido (opcional, puede ser null)
    private String secondLastName;
    // Correo electrónico
    private String email;
    // Teléfono de contacto
    private String phone;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el identificador único. */
    public int getId() {
        return id;
    }

    /** Asigna el identificador único. */
    public void setId(int id) {
        this.id = id;
    }

    /** Devuelve el tipo de documento. */
    public DocumentType getDocumentType() {
        return documentType;
    }

    /** Asigna el tipo de documento. */
    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    /** Devuelve el número de documento. */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /** Asigna el número de documento. */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /** Devuelve el primer nombre. */
    public String getFirstName() {
        return firstName;
    }

    /** Asigna el primer nombre. */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** Devuelve el segundo nombre. */
    public String getMiddleName() {
        return middleName;
    }

    /** Asigna el segundo nombre. */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /** Devuelve el primer apellido. */
    public String getLastName() {
        return lastName;
    }

    /** Asigna el primer apellido. */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /** Devuelve el segundo apellido. */
    public String getSecondLastName() {
        return secondLastName;
    }

    /** Asigna el segundo apellido. */
    public void setSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
    }

    /** Devuelve el correo electrónico. */
    public String getEmail() {
        return email;
    }

    /** Asigna el correo electrónico. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Devuelve el teléfono. */
    public String getPhone() {
        return phone;
    }

    /** Asigna el teléfono. */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // ---------- Métodos de ayuda ----------

    /**
     * Arma el nombre completo omitiendo las partes opcionales que estén vacías.
     *
     * @return por ejemplo "Ana María Gómez López"
     */
    public String getFullName() {
        // Stream.of crea un flujo con las 4 partes del nombre
        return Stream.of(firstName, middleName, lastName, secondLastName)
                // se descartan las partes null o en blanco (nombre y apellido opcionales)
                .filter(part -> part != null && !part.isBlank())
                // se unen las partes que quedaron separándolas con un espacio
                .collect(Collectors.joining(" "));
    }

    /** Texto con el que se muestra la persona en listas y menús. */
    @Override
    public String toString() {
        return getFullName() + " (" + documentType + " " + documentNumber + ")";
    }
}
