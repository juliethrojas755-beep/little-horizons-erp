/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EXAMEN;



import java.util.Scanner;


public class view {

    private Scanner scanner = new Scanner(System.in);

    public void showHeader(int year) {
        System.out.println("==================================================");
        System.out.println("EXPORTADOR DE MATERIAS ASIGNADAS - AÑO " + year);
        System.out.println("==================================================");
    }

    public String readTeacherId() {
        System.out.print("Ingrese el ID del profesor: ");
        return scanner.nextLine().trim();
    }

    public void showError(String message) {
        System.out.println("ERROR: " + message);
    }

    public void showSuccess(String teacherName, String fileName) {
        System.out.println("Archivo generado exitosamente.");
        System.out.println("Profesor: " + teacherName);
        System.out.println("Archivo: " + fileName);
    }
}

