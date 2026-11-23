package ua.edu.nupp.kpp.server.dao;

import java.sql.*;
import java.util.List;
import ua.edu.nupp.kpp.server.model.ConservationRequest;

public final class ConservationRequestDAO {
    private static ConservationRequest map(ResultSet r) throws SQLException {
        long specialistId = r.getLong("specialist_id"); // NULL becomes 0 in the API
        return new ConservationRequest(r.getLong("id"),r.getLong("exhibit_id"),r.getLong("inspection_id"),
            specialistId,r.getString("state"),r.getInt("f1"),r.getInt("f2"),r.getInt("f3"),r.getDouble("integral_score"));
    }
    public List<ConservationRequest> all() throws SQLException {
        return Jdbc.list("SELECT * FROM conservation_request ORDER BY id", p -> {},ConservationRequestDAO::map);
    }
    public ConservationRequest get(long id) throws SQLException {
        return Jdbc.one("SELECT * FROM conservation_request WHERE id=?",p -> p.setLong(1,id),ConservationRequestDAO::map);
    }
    public ConservationRequest create(ConservationRequest r) throws SQLException {
        long id = Jdbc.insert("INSERT INTO conservation_request(exhibit_id,inspection_id,specialist_id,state,f1,f2,f3,integral_score) VALUES(?,?,?,?,?,?,?,?)",
                p -> fill(p,r));
        return get(id);
    }
    public int update(long id,ConservationRequest r) throws SQLException {
        return Jdbc.execute("UPDATE conservation_request SET exhibit_id=?,inspection_id=?,specialist_id=?,state=?,f1=?,f2=?,f3=?,integral_score=? WHERE id=?",
                p -> {fill(p,r);p.setLong(9,id);});
    }
    private static void fill(PreparedStatement p,ConservationRequest r) throws SQLException {
        p.setLong(1,r.exhibitId());p.setLong(2,r.inspectionId());
        if(r.specialistId() == 0)p.setNull(3,Types.BIGINT); else p.setLong(3,r.specialistId());
        p.setString(4,r.state());p.setInt(5,r.f1());p.setInt(6,r.f2());p.setInt(7,r.f3());
        p.setDouble(8,r.integralScore());
    }
    public int changeState(long id, String expectedOldState, String nextState) throws SQLException {
        return Jdbc.execute("UPDATE conservation_request SET state=? WHERE id=? AND state=?",
                p -> {p.setString(1,nextState);p.setLong(2,id);p.setString(3,expectedOldState);});
    }
    public int delete(long id) throws SQLException {
        return Jdbc.execute("DELETE FROM conservation_request WHERE id=?",p -> p.setLong(1,id));
    }
}
