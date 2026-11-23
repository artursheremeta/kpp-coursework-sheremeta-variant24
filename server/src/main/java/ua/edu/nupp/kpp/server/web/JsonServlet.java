package ua.edu.nupp.kpp.server.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.*;
import javax.servlet.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Shared JSON transport, HTTP status codes, and error reporting. */
public abstract class JsonServlet extends HttpServlet {
    protected static final ObjectMapper JSON=new ObjectMapper();
    private static final Logger LOG=Logger.getLogger(JsonServlet.class.getName());
    @FunctionalInterface protected interface Work {Object run() throws Exception;}
    protected void handle(HttpServletResponse resp,int success,Work work) throws IOException {
        resp.setCharacterEncoding("UTF-8");resp.setContentType("application/json; charset=UTF-8");
        try { Object result=work.run();resp.setStatus(success);JSON.writeValue(resp.getWriter(),result); }
        catch(com.fasterxml.jackson.core.JsonProcessingException e){error(resp,400,"Некоректний формат JSON: "+e.getOriginalMessage());}
        catch(NoSuchElementException e){error(resp,404,e.getMessage());}
        catch(IllegalArgumentException e){error(resp,400,e.getMessage());}
        catch(IllegalStateException e){error(resp,409,e.getMessage());}
        catch(SQLException e){
            LOG.log(Level.WARNING,"Database request failed",e);
            if(e.getSQLState()!=null && e.getSQLState().startsWith("23"))
                error(resp,409,"Конфлікт даних: дубль інвентарного номера або запис використовується в іншій таблиці");
            else error(resp,500,"Помилка бази даних. Перевірте MySQL і параметри підключення.");
        }
        catch(Exception e){LOG.log(Level.SEVERE,"Unexpected server error",e);error(resp,500,"Внутрішня помилка сервера");}
    }
    protected long id(HttpServletRequest req){
        String raw=req.getParameter("id");
        if(raw==null)throw new IllegalArgumentException("Не вказано параметр id");
        try{long id=Long.parseLong(raw);if(id<=0)throw new NumberFormatException();return id;}
        catch(NumberFormatException e){throw new IllegalArgumentException("Некоректний id: "+raw);}
    }
    protected <T> T body(HttpServletRequest req,Class<T> klass) throws IOException {
        return JSON.readValue(req.getInputStream(),klass);
    }
    protected void error(HttpServletResponse resp,int status,String message) throws IOException {
        resp.setStatus(status);JSON.writeValue(resp.getWriter(),Map.of("error",message==null?"Помилка":message));
    }
}
