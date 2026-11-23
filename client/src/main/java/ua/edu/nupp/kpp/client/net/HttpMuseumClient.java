package ua.edu.nupp.kpp.client.net;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import ua.edu.nupp.kpp.client.model.*;

/** JavaFX is not connected to JDBC; every operation goes through HTTP/JSON. */
public final class HttpMuseumClient {
    private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json=new ObjectMapper();
    private final String base;

    public HttpMuseumClient() {
        String configured=System.getenv("MUSEUM_API_BASE");
        base=(configured==null||configured.isBlank())?"http://localhost:8080/museum-exhibits-server":configured.replaceAll("/$","");
    }
    private String send(String method,String resource,Object value) throws IOException,InterruptedException {
        HttpRequest.Builder b=HttpRequest.newBuilder(URI.create(base+resource))
                .timeout(Duration.ofSeconds(15)).header("Accept","application/json");
        if(value!=null)b.header("Content-Type","application/json; charset=UTF-8");
        HttpRequest.BodyPublisher body=value==null?HttpRequest.BodyPublishers.noBody():
                HttpRequest.BodyPublishers.ofString(json.writeValueAsString(value));
        b.method(method,body);
        HttpResponse<String> response=http.send(b.build(),HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()>=400) {
            String message="HTTP "+response.statusCode();
            try{message=json.readTree(response.body()).path("error").asText(message);}
            catch(Exception ignored) { }
            throw new IOException(message);
        }
        return response.body();
    }
    private <T> List<T> getAll(String url,Class<T> cls) throws IOException,InterruptedException {
        return json.readValue(send("GET",url,null),json.getTypeFactory().constructCollectionType(List.class,cls));
    }
    private <T> T create(String url,Object data,Class<T> cls) throws IOException,InterruptedException {
        return json.readValue(send("POST",url,data),cls);
    }
    private <T> T update(String url,long id,Object data,Class<T> cls) throws IOException,InterruptedException {
        return json.readValue(send("PUT",url+"?id="+id,data),cls);
    }
    private void delete(String url,long id) throws IOException,InterruptedException {
        send("DELETE",url+"?id="+id,null);
    }
    public List<Exhibit> getExhibits() throws IOException,InterruptedException {return getAll("/exhibits",Exhibit.class);}
    public Exhibit addExhibit(Exhibit v) throws IOException,InterruptedException {return create("/exhibits",v,Exhibit.class);}
    public Exhibit updateExhibit(long id,Exhibit v) throws IOException,InterruptedException {return update("/exhibits",id,v,Exhibit.class);}
    public void deleteExhibit(long id) throws IOException,InterruptedException {delete("/exhibits",id);}

    public List<Inspection> getInspections() throws IOException,InterruptedException {return getAll("/inspections",Inspection.class);}
    public Inspection addInspection(Inspection v) throws IOException,InterruptedException {return create("/inspections",v,Inspection.class);}
    public Inspection updateInspection(long id,Inspection v) throws IOException,InterruptedException {return update("/inspections",id,v,Inspection.class);}
    public void deleteInspection(long id) throws IOException,InterruptedException {delete("/inspections",id);}

    public List<Specialist> getSpecialists() throws IOException,InterruptedException {return getAll("/specialists",Specialist.class);}
    public Specialist addSpecialist(Specialist v) throws IOException,InterruptedException {return create("/specialists",v,Specialist.class);}
    public Specialist updateSpecialist(long id,Specialist v) throws IOException,InterruptedException {return update("/specialists",id,v,Specialist.class);}
    public void deleteSpecialist(long id) throws IOException,InterruptedException {delete("/specialists",id);}

    public List<ConservationRequest> getRequests() throws IOException,InterruptedException {return getAll("/requests",ConservationRequest.class);}
    public ConservationRequest addRequest(ConservationRequest v) throws IOException,InterruptedException {return create("/requests",v,ConservationRequest.class);}
    public ConservationRequest updateRequest(long id,ConservationRequest v) throws IOException,InterruptedException {return update("/requests",id,v,ConservationRequest.class);}
    public void deleteRequest(long id) throws IOException,InterruptedException {delete("/requests",id);}
    public ConservationRequest changeRequestState(long id,String state) throws IOException,InterruptedException {
        return create("/requests?id="+id+"&action=transition",Map.of("state",state),ConservationRequest.class);
    }
}
