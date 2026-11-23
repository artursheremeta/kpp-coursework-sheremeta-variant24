package ua.edu.nupp.kpp.server.service;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import ua.edu.nupp.kpp.server.dao.*;
import ua.edu.nupp.kpp.server.model.*;

/** Calculates F1 and F3 from Exhibit and F2 from Inspection; UI cannot set the score. */
public final class ConservationRequestService {
    private static final Logger LOG=Logger.getLogger(ConservationRequestService.class.getName());
    private final ConservationRequestDAO dao=new ConservationRequestDAO();
    private final ExhibitDAO exhibits=new ExhibitDAO();
    private final InspectionDAO inspections=new InspectionDAO();
    private final SpecialistDAO specialists=new SpecialistDAO();

    public List<ConservationRequest> all() throws SQLException{return dao.all();}
    public ConservationRequest get(long id) throws SQLException {
        ConservationRequest r=dao.get(id);
        if(r==null)throw new NoSuchElementException("Заявку не знайдено: "+id);
        return r;
    }
    private ConservationRequest assembled(long id,ConservationRequest input,String state) throws SQLException {
        if(input==null)throw new IllegalArgumentException("Заявку не задано");
        Validator.requestIds(input.exhibitId(),input.inspectionId(),input.specialistId());
        Exhibit e=exhibits.get(input.exhibitId());
        if(e==null)throw new IllegalArgumentException("Експонат не існує: "+input.exhibitId());
        Inspection inspection=inspections.get(input.inspectionId());
        if(inspection==null)throw new IllegalArgumentException("Огляд не існує: "+input.inspectionId());
        Validator.inspectionBelongsToExhibit(input.exhibitId(),inspection.exhibitId());
        if(input.specialistId()>0 && specialists.get(input.specialistId())==null)
            throw new IllegalArgumentException("Фахівець не існує: "+input.specialistId());
        int f1=e.sensitivityScore(), f2=inspection.damageScore(), f3=e.exhibitionScore();
        return new ConservationRequest(id,e.id(),inspection.id(),input.specialistId(),state,
                                       f1,f2,f3,RiskCalculator.calculate(f1,f2,f3));
    }
    public ConservationRequest create(ConservationRequest input) throws SQLException {
        ConservationRequest prepared=assembled(0,input,RequestState.NEW.name());
        ConservationRequest saved=dao.create(prepared);
        LOG.info("Створено заявку: "+saved.id());return saved;
    }
    public ConservationRequest update(long id,ConservationRequest input) throws SQLException {
        Validator.positive(id,"ID заявки");ConservationRequest original=get(id);
        if(RequestState.valueOf(original.state())==RequestState.COMPLETED)
            throw new IllegalArgumentException("Завершену заявку не можна змінювати");
        ConservationRequest prepared=assembled(id,input,original.state());
        if(dao.update(id,prepared)==0)throw new NoSuchElementException("Заявку не знайдено: "+id);
        LOG.info("Оновлено заявку: "+id);return get(id);
    }
    public ConservationRequest transition(long id,String next) throws SQLException {
        Validator.positive(id,"ID заявки");Validator.required(next,"Новий стан");
        ConservationRequest current=get(id);
        RequestState desired;
        try { desired=RequestState.valueOf(next); }
        catch (IllegalArgumentException ex) {throw new IllegalArgumentException("Невідомий стан: "+next);}
        RequestState existing=RequestState.valueOf(current.state());
        if(!existing.canTransitionTo(desired))
            throw new IllegalArgumentException("Заборонений перехід: "+existing+" → "+desired);
        if(dao.changeState(id,current.state(),desired.name())==0)
            throw new IllegalStateException("Стан заявки вже змінено. Оновіть таблицю.");
        LOG.info("Змінено стан заявки "+id+" на "+desired);return get(id);
    }
    public void delete(long id) throws SQLException {
        Validator.positive(id,"ID заявки");if(dao.delete(id)==0)throw new NoSuchElementException("Заявку не знайдено: "+id);
        LOG.info("Видалено заявку: "+id);
    }
}
