package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.ScheduleDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.TeacherAssignmentDao;
import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.Schedule;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.model.enums.TeacherStatus;
import com.littlehorizons.erp.util.BusinessException;
import com.littlehorizons.erp.util.Validator;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Directorio de profesores, asignación de materias y horarios con validación de conflictos.
 */
public class TeacherService {

    // Jornada escolar: ningún bloque puede empezar antes ni terminar después
    private static final LocalTime SCHOOL_START = LocalTime.of(6, 0);
    private static final LocalTime SCHOOL_END = LocalTime.of(18, 0);

    private final TeacherDao teacherDao;
    private final TeacherAssignmentDao assignmentDao;
    private final ScheduleDao scheduleDao;
    private final SubjectDao subjectDao;

    public TeacherService(TeacherDao teacherDao, TeacherAssignmentDao assignmentDao, ScheduleDao scheduleDao,
                          SubjectDao subjectDao) {
        this.teacherDao = teacherDao;
        this.assignmentDao = assignmentDao;
        this.scheduleDao = scheduleDao;
        this.subjectDao = subjectDao;
    }

    // ====================== DIRECTORIO ======================

    /** Registra un profesor nuevo después de validar sus datos. */
    public int registerTeacher(Teacher teacher) {
        if (!Validator.isValidDocument(teacher.getDocumentNumber())) {
            throw new BusinessException("El documento debe tener entre 5 y 20 letras o números.");
        }
        if (!Validator.isValidName(teacher.getFirstName()) || !Validator.isValidName(teacher.getLastName())) {
            throw new BusinessException("El nombre y el apellido son obligatorios y solo admiten letras.");
        }
        if (!Validator.isValidEmail(teacher.getEmail())) {
            throw new BusinessException("El correo institucional es obligatorio y debe ser válido.");
        }
        // El documento no puede repetirse
        if (teacherDao.findByDocument(teacher.getDocumentNumber()).isPresent()) {
            throw new BusinessException("Ya existe un profesor con el documento " + teacher.getDocumentNumber() + ".");
        }
        teacher.setStatus(TeacherStatus.ACTIVE);       // un profesor nuevo empieza Activo
        return teacherDao.insert(teacher);
    }

    public boolean updateTeacher(Teacher teacher) {
        return teacherDao.update(teacher);
    }

    public List<Teacher> findAll() {
        return teacherDao.findAll();
    }

    /** Directorio filtrado por texto de especialización (sin distinguir mayúsculas). */
    public List<Teacher> findBySpecialization(String text) {
        String needle = text.toLowerCase();
        return teacherDao.findAll().stream()
                .filter(t -> t.getSpecialization() != null && t.getSpecialization().toLowerCase().contains(needle))
                .toList();
    }

    public Teacher getTeacher(int teacherId) {
        return teacherDao.findById(teacherId)
                .orElseThrow(() -> new BusinessException("No existe un profesor con id " + teacherId + "."));
    }

    public Optional<Teacher> findByDocument(String documentNumber) {
        return teacherDao.findByDocument(documentNumber);
    }

    // ====================== ASIGNACIÓN DE MATERIAS (muchos a muchos) ======================

    /** Asigna una materia a un profesor para un año y una sección. */
    public int assignSubject(int teacherId, int subjectId, int academicYear, String section) {
        Teacher teacher = getTeacher(teacherId);
        // Solo profesores activos reciben materias
        if (teacher.getStatus() != TeacherStatus.ACTIVE) {
            throw new BusinessException("Solo se pueden asignar materias a profesores activos.");
        }
        subjectDao.findById(subjectId).orElseThrow(() -> new BusinessException("La materia no existe."));
        // No se duplica la misma materia, año y sección para el mismo profesor
        boolean duplicated = assignmentDao.findByTeacher(teacherId).stream()
                .anyMatch(a -> a.getSubjectId() == subjectId && a.getAcademicYear() == academicYear
                        && section.equalsIgnoreCase(a.getSection()));
        if (duplicated) {
            throw new BusinessException("El profesor ya tiene esa materia asignada en " + academicYear + " sección " + section + ".");
        }
        TeacherAssignment assignment = new TeacherAssignment();
        assignment.setTeacherId(teacherId);
        assignment.setSubjectId(subjectId);
        assignment.setAcademicYear(academicYear);
        assignment.setSection(section.toUpperCase());
        return assignmentDao.insert(assignment);
    }

    public List<TeacherAssignment> getAssignments(int teacherId) {
        return assignmentDao.findByTeacher(teacherId);
    }

    public List<TeacherAssignment> findAllAssignments() {
        return assignmentDao.findAll();
    }

    // ====================== HORARIOS ======================

    /**
     * Agrega un bloque de horario a una asignación después de comprobar que ni el profesor
     * ni el salón estén ocupados a esa hora.
     */
    public int addSchedule(Schedule schedule) {
        // La hora de fin debe ser posterior a la de inicio
        if (!schedule.getEndTime().isAfter(schedule.getStartTime())) {
            throw new BusinessException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        // El bloque debe estar dentro de la jornada escolar
        if (schedule.getStartTime().isBefore(SCHOOL_START) || schedule.getEndTime().isAfter(SCHOOL_END)) {
            throw new BusinessException("El horario debe estar dentro de la jornada escolar (06:00 a 18:00).");
        }
        TeacherAssignment assignment = assignmentDao.findById(schedule.getAssignmentId())
                .orElseThrow(() -> new BusinessException("La asignación no existe."));

        // Conflicto de profesor: se compara con todos los bloques que ya tiene ese profesor
        findConflict(scheduleDao.findByTeacher(assignment.getTeacherId()), schedule).ifPresent(conflict -> {
            throw new BusinessException("Conflicto de horario: el profesor ya tiene clase el " + conflict.getWeekDay()
                    + " de " + conflict.getStartTime() + " a " + conflict.getEndTime() + ".");
        });
        // Conflicto de salón: solo si se indicó un salón
        if (Validator.isNotBlank(schedule.getClassroom())) {
            findConflict(scheduleDao.findByClassroom(schedule.getClassroom()), schedule).ifPresent(conflict -> {
                throw new BusinessException("Conflicto de salón: " + schedule.getClassroom() + " está ocupado el "
                        + conflict.getWeekDay() + " de " + conflict.getStartTime() + " a " + conflict.getEndTime() + ".");
            });
        }
        return scheduleDao.insert(schedule);
    }

    public List<Schedule> getTeacherSchedule(int teacherId) {
        return scheduleDao.findByTeacher(teacherId);
    }

    public boolean removeSchedule(int scheduleId) {
        return scheduleDao.delete(scheduleId);
    }

    /** Tabla de búsqueda id -> profesor para vistas y reportes. */
    public Map<Integer, Teacher> teachersById() {
        return teacherDao.findAll().stream().collect(Collectors.toMap(Teacher::getId, Function.identity()));
    }

    /** Primer bloque existente que se cruza con el candidato (usa Schedule.overlapsWith). */
    private Optional<Schedule> findConflict(List<Schedule> existing, Schedule candidate) {
        return existing.stream().filter(candidate::overlapsWith).findFirst();
    }
}
