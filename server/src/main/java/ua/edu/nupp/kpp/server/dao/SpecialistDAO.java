package ua.edu.nupp.kpp.server.dao;

import java.sql.*;
import java.util.List;
import ua.edu.nupp.kpp.server.model.Specialist;

public final class SpecialistDAO {
    private static Specialist map(ResultSet r) throws SQLException {
        return new Specialist(r.getLong("id"), r.getString("full_name"),r.getString("specialization"),r.getString("contact"));
    }
    public List<Specialist> all() throws SQLException {
        return Jdbc.list("SELECT * FROM specialist ORDER BY id",p -> {},SpecialistDAO::map);
    }
    public Specialist get(long id) throws SQLException {
        return Jdbc.one("SELECT * FROM specialist WHERE id=?",p -> p.setLong(1,id),SpecialistDAO::map);
    }
    public Specialist create(Specialist s) throws SQLException {
        long id=Jdbc.insert("INSERT INTO specialist(full_name,specialization,contact) VALUES(?,?,?)", p -> fill(p,s));
        return get(id);
    }
    public int update(long id,Specialist s) throws SQLException {
        return Jdbc.execute("UPDATE specialist SET full_name=?,specialization=?,contact=? WHERE id=?",
                p -> {fill(p,s);p.setLong(4,id);});
    }
    private static void fill(PreparedStatement p,Specialist s) throws SQLException {
        p.setString(1,s.fullName().trim());p.setString(2,s.specialization().trim());
        p.setString(3,s.contact()==null?"":s.contact().trim());
    }
    public int delete(long id) throws SQLException {
        return Jdbc.execute("DELETE FROM specialist WHERE id=?",p -> p.setLong(1,id));
    }
}
