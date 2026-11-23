package ua.edu.nupp.kpp.server.web;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import ua.edu.nupp.kpp.server.model.Exhibit;
import ua.edu.nupp.kpp.server.service.ExhibitService;

@WebServlet("/exhibits")
public final class ExhibitServlet extends JsonServlet {
    private final ExhibitService service=new ExhibitService();
    
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        handle(resp,200,()->req.getParameter("id")==null ? service.all() : service.get(id(req)));
    }

    
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        handle(resp,201,()->service.create(body(req,Exhibit.class)));
    }

    
    @Override protected void doPut(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        handle(resp,200,()->service.update(id(req),body(req,Exhibit.class)));
    }
    @Override protected void doDelete(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        handle(resp,200,()->{service.delete(id(req));return java.util.Map.of("ok",true);});
    }

}
