/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EXAMEN;



import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class dao {

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        try (InputStream input = ExamConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (Exception e) {
            System.out.println("No se pudo leer database.properties, se usan valores por defecto.");
        }
   
        String url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/campuslands");
        String user = props.getProperty("db.user", "root");
        String password = props.getProperty("db.password", "");
        return DriverManager.getConnection(url, user, password);
        

    private int id;
    private String fullName;
    private String document;

    public Teacher(int id, String fullName, String document) {
        this.id = id;
        this.fullName = fullName;
        this.document = document;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDocument() {
        return document;
    }
}