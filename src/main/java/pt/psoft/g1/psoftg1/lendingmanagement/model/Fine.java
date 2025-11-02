package pt.psoft.g1.psoftg1.lendingmanagement.model;

import java.util.Objects;

public class Fine {

    private final int dailyRate;
    private final int totalAmount;
    private final Lending associatedLending;

    // Construtor principal
    public Fine(Lending associatedLending) {
        validateLending(associatedLending);

        int delayDays = associatedLending.calculateDelayDays();
        if (delayDays <= 0) {
            throw new IllegalArgumentException("Cannot create fine for lending without delay");
        }

        this.associatedLending = associatedLending;
        this.dailyRate = associatedLending.getDailyFineAmount();
        this.totalAmount = this.dailyRate * delayDays;
    }

    // Construtor alternativo (para reconstrução)
    protected Fine(int dailyRate, int totalAmount, Lending associatedLending) {
        this.dailyRate = dailyRate;
        this.totalAmount = totalAmount;
        this.associatedLending = Objects.requireNonNull(associatedLending,
                "Associated lending is required");
    }

    // Validação
    private void validateLending(Lending lending) {
        if (lending == null) {
            throw new IllegalArgumentException("Associated lending is required");
        }
    }

    // Métodos de acesso
    public int getDailyRate() {
        return dailyRate;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public Lending getAssociatedLending() {
        return associatedLending;
    }

    // Método auxiliar para obter valor em euros
    public double getTotalAmountInEuros() {
        return totalAmount / 100.0;
    }
}