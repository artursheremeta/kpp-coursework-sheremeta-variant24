package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import ua.edu.nupp.kpp.server.service.*;
import ua.edu.nupp.kpp.server.model.*;

/** 27 deterministic domain tests without MySQL or Tomcat. */
public class CourseworkTest {
    @Test void integralScoreFor806040() { assertEquals(63.00,RiskCalculator.calculate(80,60,40),0.001); }
    @Test void integralScoreFor907080() { assertEquals(80.50,RiskCalculator.calculate(90,70,80),0.001); }
    @Test void integralScoreFor203040() { assertEquals(28.50,RiskCalculator.calculate(20,30,40),0.001); }
    @Test void integralScoreFor50070() { assertEquals(37.50,RiskCalculator.calculate(50,0,70),0.001); }
    @Test void zeroFactors() { assertEquals(0,RiskCalculator.calculate(0,0,0),0.001); }
    @Test void upperBoundFactors() { assertEquals(100,RiskCalculator.calculate(100,100,100),0.001); }
    @Test void rejectsNegativeF1() { assertThrows(IllegalArgumentException.class,()->RiskCalculator.calculate(-1,30,40)); }
    @Test void rejectsF2Over100() { assertThrows(IllegalArgumentException.class,()->RiskCalculator.calculate(10,101,40)); }
    @Test void rejectsF3Over100() { assertThrows(IllegalArgumentException.class,()->RiskCalculator.calculate(10,30,101)); }
    @Test void allowsNewToAssessed() { assertTrue(RequestState.NEW.canTransitionTo(RequestState.ASSESSED)); }
    @Test void allowsAssessedToProgress() { assertTrue(RequestState.ASSESSED.canTransitionTo(RequestState.IN_PROGRESS)); }
    @Test void allowsProgressToCompleted() { assertTrue(RequestState.IN_PROGRESS.canTransitionTo(RequestState.COMPLETED)); }
    @Test void forbidsNewToCompleted() { assertFalse(RequestState.NEW.canTransitionTo(RequestState.COMPLETED)); }
    @Test void forbidsSkippingAssessed() { assertFalse(RequestState.NEW.canTransitionTo(RequestState.IN_PROGRESS)); }
    @Test void forbidsBackwards() { assertFalse(RequestState.ASSESSED.canTransitionTo(RequestState.NEW)); }
    @Test void forbidsRepeatState() { assertFalse(RequestState.NEW.canTransitionTo(RequestState.NEW)); }
    @Test void forbidsTransitionAfterCompleted() { assertFalse(RequestState.COMPLETED.canTransitionTo(RequestState.NEW)); }
    @Test void rejectsNullTransition() { assertFalse(RequestState.NEW.canTransitionTo(null)); }
    @Test void rejectsBlankInventory() {
        assertThrows(IllegalArgumentException.class,()->Validator.exhibit(new Exhibit(0," ","Ваза","кераміка","Зал",80,40)));
    }
    @Test void rejectsMissingExhibitTitle() {
        assertThrows(IllegalArgumentException.class,()->Validator.exhibit(new Exhibit(0,"M-1","","кераміка","Зал",80,40)));
    }
    @Test void acceptsValidExhibit() {
        assertDoesNotThrow(()->Validator.exhibit(new Exhibit(0,"M-1","Ваза","кераміка","Зал",80,40)));
    }
    @Test void rejectsExhibitSensitivityOver100() {
        assertThrows(IllegalArgumentException.class,()->Validator.exhibit(new Exhibit(0,"M-1","Ваза","кераміка","Зал",101,40)));
    }
    @Test void rejectsInspectionWithInvalidDate() {
        assertThrows(IllegalArgumentException.class,()->Validator.inspection(new Inspection(0,1,"2026-13-30",10,"Огляд")));
    }
    @Test void rejectsInspectionDamageBelowZero() {
        assertThrows(IllegalArgumentException.class,()->Validator.inspection(new Inspection(0,1,"2026-10-20",-1,"Огляд")));
    }
    @Test void acceptsValidInspection() {
        assertDoesNotThrow(()->Validator.inspection(new Inspection(0,1,"2026-10-20",30,"Огляд")));
    }
    @Test void rejectsSpecialistWithoutName() {
        assertThrows(IllegalArgumentException.class,()->Validator.specialist(new Specialist(0," ","Реставратор","")));
    }
    @Test void acceptsSpecialist() {
        assertDoesNotThrow(()->Validator.specialist(new Specialist(0,"Олена Петренко","Реставратор","")));
    }
    @Test void rejectsInspectionForWrongExhibit() {
        assertThrows(IllegalArgumentException.class,()->Validator.inspectionBelongsToExhibit(1,2));
    }
    @Test void rejectsNegativeSpecialistId() {
        assertThrows(IllegalArgumentException.class,()->Validator.requestIds(1,1,-2));
    }
    @Test void acceptsUnassignedSpecialist() {
        assertDoesNotThrow(()->Validator.requestIds(1,1,0));
    }
}
