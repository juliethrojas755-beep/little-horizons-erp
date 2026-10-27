
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EXAMEN;

ackage examen.controller;

import examen.dao.TeacherDao;
import examen.model.AssignedSubject;
import examen.model.Teacher;
import examen.view.ExportView;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Lógica del exportador: pide el id, valida y crea el archivo
public class controller {

    // El año siempre es 2026 (no se toma del computador)
    private static final int YEAR = 2026;
    private static final String LINE = "======================================================================";
    private static final String DIVIDER = "----------------------------------------------------------------------";

    private ExportView view = new ExportView();
    private TeacherDao dao = new TeacherDao();

    public void start() {
        view.showHeader(YEAR);
        try {
            Teacher teacher = null;
            // Se repite hasta que el id sea un número y el profesor exista
            while (teacher == null) {
                String text = view.readTeacherId();
                int id;
                // 1. Validar que sea un número entero
                try {
                    id = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    view.showError("Debe ingresar un número válido.");
                    continue;
                }
                // 2. Validar que el profesor exista
                teacher = dao.findById(id);
                if (teacher == null) {
                    view.showError("No se encontró ningún profesor con el ID " + id + ".");
                }