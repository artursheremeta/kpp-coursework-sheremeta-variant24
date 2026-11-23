package ua.edu.nupp.kpp.server.service;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import ua.edu.nupp.kpp.server.dao.*;
import ua.edu.nupp.kpp.server.model.Inspection;

public final class InspectionService {
    private static final Logger LOG=Logger.getLogger(InspectionService.class.getName());
    private final InspectionDAO dao=new InspectionDAO();
    private final ExhibitDAO exhibits=new ExhibitDAO();
    public List<Inspection> all() throws SQLException {return dao.all();}
    public Inspection get(long id) throws SQLException {
        Inspection i=dao.get(id);if(i==null)throw new NoSuchElementException("Огляд не знайдено: "+id);return i;
    }
    private void validate(Inspection i) throws SQLException {
        Validator.inspection(i);
        if(exhibits.get(i.exhibitId())==null)throw new IllegalArgumentException("Експонат не існує: "+i.exhibitId());
    }
    public Inspection create(Inspection i) throws SQLException {
        validate(i);Inspection saved=dao.create(i);LOG.info("Додано огляд: "+saved.id());return saved;
    }
    public Inspection update(long id,Inspection i) throws SQLException {
        Validator.positive(id,"ID огляду");validate(i);
        if(dao.update(id,i)==0)throw new NoSuchElementException("Огляд не знайдено: "+id);
        LOG.info("Оновлено огляд: "+id);return get(id);
    }
    public void delete(long id) throws SQLException {
        Validator.positive(id,"ID огляду");if(dao.delete(id)==0)throw new NoSuchElementException("Огляд не знайдено: "+id);
        LOG.info("Видалено огляд: "+id);
    }
}
