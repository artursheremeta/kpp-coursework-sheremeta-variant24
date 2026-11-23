package ua.edu.nupp.kpp.server.service;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import ua.edu.nupp.kpp.server.dao.SpecialistDAO;
import ua.edu.nupp.kpp.server.model.Specialist;

public final class SpecialistService {
    private static final Logger LOG=Logger.getLogger(SpecialistService.class.getName());
    private final SpecialistDAO dao=new SpecialistDAO();
    public List<Specialist> all() throws SQLException{return dao.all();}
    public Specialist get(long id) throws SQLException {
        Specialist s=dao.get(id);if(s==null)throw new NoSuchElementException("Фахівця не знайдено: "+id);return s;
    }
    public Specialist create(Specialist s) throws SQLException {
        Validator.specialist(s);Specialist saved=dao.create(s);LOG.info("Додано фахівця: "+saved.id());return saved;
    }
    public Specialist update(long id,Specialist s) throws SQLException {
        Validator.positive(id,"ID фахівця");Validator.specialist(s);
        if(dao.update(id,s)==0)throw new NoSuchElementException("Фахівця не знайдено: "+id);
        LOG.info("Оновлено фахівця: "+id);return get(id);
    }
    public void delete(long id) throws SQLException {
        Validator.positive(id,"ID фахівця");if(dao.delete(id)==0)throw new NoSuchElementException("Фахівця не знайдено: "+id);
        LOG.info("Видалено фахівця: "+id);
    }
}
