package ua.edu.nupp.kpp.server.service;

/** All three factors are already normalized to 0..100. */
public final class RiskCalculator {
    private RiskCalculator() { }

    public static double calculate(int f1, int f2, int f3) {
        Validator.score(f1, "F1 — чутливість матеріалу");
        Validator.score(f2, "F2 — тяжкість пошкодження");
        Validator.score(f3, "F3 — значущість експонування");
        return Math.round((0.40 * f1 + 0.35 * f2 + 0.25 * f3) * 100.0) / 100.0;
    }
}
