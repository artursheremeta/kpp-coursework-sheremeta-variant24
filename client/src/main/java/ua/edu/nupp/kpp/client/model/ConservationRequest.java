package ua.edu.nupp.kpp.client.model;
public record ConservationRequest(long id, long exhibitId, long inspectionId, long specialistId, String state, int f1, int f2, int f3, double integralScore) {}
