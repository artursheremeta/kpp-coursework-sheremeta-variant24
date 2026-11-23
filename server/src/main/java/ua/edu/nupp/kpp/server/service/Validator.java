package ua.edu.nupp.kpp.server.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import ua.edu.nupp.kpp.server.model.*;

/** Validation rules independent of MySQL, directly covered by JUnit 5 tests. */
public final class Validator {
    private Validator() { }
    public static void required(String text, String name) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException(name + " не може бути порожнім");
    }
    public static void positive(long id, String name) {
        if (id <= 0) throw new IllegalArgumentException(name + " має бути додатним");
    }
    public static void score(int n, String name) {
        if (n < 0 || n > 100) throw new IllegalArgumentException(name + " має бути в межах 0..100");
    }
    public static void exhibit(Exhibit e) {
        if (e == null) throw new IllegalArgumentException("Експонат не задано");
        required(e.inventoryNumber(), "Інвентарний номер");
        required(e.title(), "Назва експоната");
        required(e.material(), "Матеріал");
        required(e.location(), "Місце зберігання");
        score(e.sensitivityScore(), "F1");
        score(e.exhibitionScore(), "F3");
    }
    public static void inspection(Inspection i) {
        if (i == null) throw new IllegalArgumentException("Огляд не задано");
        positive(i.exhibitId(), "ID експоната");
        required(i.inspectionDate(), "Дата огляду");
        try { LocalDate.parse(i.inspectionDate()); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Дата має формат РРРР-ММ-ДД", e); }
        score(i.damageScore(), "F2");
        required(i.notes(), "Результат огляду");
    }
    public static void specialist(Specialist s) {
        if (s == null) throw new IllegalArgumentException("Фахівця не задано");
        required(s.fullName(), "ПІБ фахівця");
        required(s.specialization(), "Спеціалізація");
    }
    public static void requestIds(long exhibitId, long inspectionId, long specialistId) {
        positive(exhibitId, "ID експоната");
        positive(inspectionId, "ID огляду");
        if (specialistId < 0) throw new IllegalArgumentException("ID фахівця не може бути від'ємним");
    }
    public static void inspectionBelongsToExhibit(long exhibitId, long inspectionExhibitId) {
        if (exhibitId != inspectionExhibitId) throw new IllegalArgumentException("Огляд стосується іншого експоната");
    }
}
