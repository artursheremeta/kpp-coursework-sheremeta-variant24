package ua.edu.nupp.kpp.server.model;
public record Inspection(long id, long exhibitId, String inspectionDate, int damageScore, String notes) { }
