package ua.edu.nupp.kpp.server.dao;

import java.sql.*;
import java.util.List;
import ua.edu.nupp.kpp.server.model.Exhibit;

public final class ExhibitDAO {
    private static Exhibit map(ResultSet r) throws SQLException {
        return new Exhibit(r.getLong("id"), r.getString("inventory_number"), r.getString("title"),
            r.getString("material"), r.getString("location"), r.getInt("sensitivity_score"), r.getInt("exhibition_score"));
    }
    public List<Exhibit> all() throws SQLException {
        return Jdbc.list("SELECT * FROM exhibit ORDER BY id", p -> {}, ExhibitDAO::map);
    }
    public Exhibit get(long id) throws SQLException {
        return Jdbc.one("SELECT * FROM exhibit WHERE id=?", p -> p.setLong(1,id), ExhibitDAO::map);
    }
    public Exhibit create(Exhibit e) throws SQLException {
        long id = Jdbc.insert("INSERT INTO exhibit(inventory_number,title,material,location,sensitivity_score,exhibition_score) VALUES(?,?,?,?,?,?)",
            p -> fill(p,e,1));
        return get(id);
    }
    public int update(long id, Exhibit e) throws SQLException {
        return Jdbc.execute("UPDATE exhibit SET inventory_number=?,title=?,material=?,location=?,sensitivity_score=?,exhibition_score=? WHERE id=?",
            p -> { fill(p,e,1); p.setLong(7,id); });
    }
    private static void fill(PreparedStatement p, Exhibit e, int i) throws SQLException {
        p.setString(i,e.inventoryNumber().trim()); p.setString(i+1,e.title().trim());
        p.setString(i+2,e.material().trim()); p.setString(i+3,e.location().trim());
        p.setInt(i+4,e.sensitivityScore()); p.setInt(i+5,e.exhibitionScore());
    }
    public int delete(long id) throws SQLException {
        return Jdbc.execute("DELETE FROM exhibit WHERE id=?", p -> p.setLong(1,id));
    }
}
