package ua.edu.nupp.kpp.client.model;
public record Inspection(long id, long exhibitId, String inspectionDate, int damageScore, String notes) {}
