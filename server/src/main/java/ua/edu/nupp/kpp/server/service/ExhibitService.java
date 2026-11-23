package ua.edu.nupp.kpp.server.service;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import ua.edu.nupp.kpp.server.dao.ExhibitDAO;
import ua.edu.nupp.kpp.server.model.Exhibit;

public final class ExhibitService {
    private static final Logger LOG=Logger.getLogger(ExhibitService.class.getName());
    private final ExhibitDAO dao = new ExhibitDAO();
    public List<Exhibit> all() throws SQLException {return dao.all();}
    public Exhibit get(long id) throws SQLException {
        Exhibit e=dao.get(id);if(e==null)throw new NoSuchElementException("Експонат не знайдено: "+id);return e;
    }
    public Exhibit create(Exhibit e) throws SQLException {
        Validator.exhibit(e);Exhibit saved=dao.create(e);LOG.info("Додано експонат: "+saved.id());return saved;
    }
    public Exhibit update(long id,Exhibit e) throws SQLException {
        Validator.positive(id,"ID експоната");Validator.exhibit(e);
        if(dao.update(id,e)==0)throw new NoSuchElementException("Експонат не знайдено: "+id);
        LOG.info("Оновлено експонат: "+id);return get(id);
    }
    public void delete(long id) throws SQLException {
        Validator.positive(id,"ID експоната");if(dao.delete(id)==0)throw new NoSuchElementException("Експонат не знайдено: "+id);
        LOG.info("Видалено експонат: "+id);
    }
}
