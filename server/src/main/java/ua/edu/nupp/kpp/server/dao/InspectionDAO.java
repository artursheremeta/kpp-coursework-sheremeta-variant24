package ua.edu.nupp.kpp.server.dao;

import java.sql.*;
import java.util.List;
import ua.edu.nupp.kpp.server.model.Inspection;

public final class InspectionDAO {
    private static Inspection map(ResultSet r) throws SQLException {
        return new Inspection(r.getLong("id"), r.getLong("exhibit_id"),r.getDate("inspection_date").toString(),
                r.getInt("damage_score"),r.getString("notes"));
    }
    public List<Inspection> all() throws SQLException {
        return Jdbc.list("SELECT * FROM inspection ORDER BY id", p -> {}, InspectionDAO::map);
    }
    public Inspection get(long id) throws SQLException {
        return Jdbc.one("SELECT * FROM inspection WHERE id=?",p -> p.setLong(1,id),InspectionDAO::map);
    }
    public Inspection create(Inspection v) throws SQLException {
        long id=Jdbc.insert("INSERT INTO inspection(exhibit_id,inspection_date,damage_score,notes) VALUES(?,?,?,?)",
                p -> fill(p,v));
        return get(id);
    }
    public int update(long id, Inspection v) throws SQLException {
        return Jdbc.execute("UPDATE inspection SET exhibit_id=?,inspection_date=?,damage_score=?,notes=? WHERE id=?",
                p -> { fill(p,v); p.setLong(5,id); });
    }
    private static void fill(PreparedStatement p,Inspection v) throws SQLException {
        p.setLong(1,v.exhibitId()); p.setDate(2,Date.valueOf(v.inspectionDate()));
        p.setInt(3,v.damageScore()); p.setString(4,v.notes().trim());
    }
    public int delete(long id) throws SQLException {
        return Jdbc.execute("DELETE FROM inspection WHERE id=?",p -> p.setLong(1,id));
    }
}
