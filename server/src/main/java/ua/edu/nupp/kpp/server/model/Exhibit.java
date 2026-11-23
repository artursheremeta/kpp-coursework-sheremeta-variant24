package ua.edu.nupp.kpp.server.model;
public record Exhibit(long id, String inventoryNumber, String title, String material, String location,
                      int sensitivityScore, int exhibitionScore) { }
