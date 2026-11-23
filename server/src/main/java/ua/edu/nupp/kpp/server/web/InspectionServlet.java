package ua.edu.nupp.kpp.server.web;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import ua.edu.nupp.kpp.server.model.Inspection;
import ua.edu.nupp.kpp.server.service.InspectionService;

@WebServlet("/inspections")
public final class InspectionServlet extends JsonServlet {
    private final InspectionService service=new InspectionService();
    
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        handle(resp,200,()->req.getParameter("id")==null ? service.all() : service.get(id(req)));
    }

    
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        handle(resp,201,()->service.create(body(req,Inspection.class)));
    }

    
    @Override protected void doPut(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        handle(resp,200,()->service.update(id(req),body(req,Inspection.class)));
    }
    @Override protected void doDelete(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        handle(resp,200,()->{service.delete(id(req));return java.util.Map.of("ok",true);});
    }

}
