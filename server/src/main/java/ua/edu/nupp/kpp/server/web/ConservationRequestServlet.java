package ua.edu.nupp.kpp.server.web;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import ua.edu.nupp.kpp.server.model.ConservationRequest;
import ua.edu.nupp.kpp.server.service.ConservationRequestService;

@WebServlet("/requests")
public final class ConservationRequestServlet extends JsonServlet {
    private final ConservationRequestService service=new ConservationRequestService();
    
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        handle(resp,200,()->req.getParameter("id")==null ? service.all() : service.get(id(req)));
    }

    
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        if("transition".equals(req.getParameter("action"))) {
            handle(resp,200,()->service.transition(id(req),body(req,StateChange.class).state()));
        } else {
            handle(resp,201,()->service.create(body(req,ConservationRequest.class)));
        }
    }
    public record StateChange(String state) {}

    
    @Override protected void doPut(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        handle(resp,200,()->service.update(id(req),body(req,ConservationRequest.class)));
    }
    @Override protected void doDelete(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        handle(resp,200,()->{service.delete(id(req));return java.util.Map.of("ok",true);});
    }

}
