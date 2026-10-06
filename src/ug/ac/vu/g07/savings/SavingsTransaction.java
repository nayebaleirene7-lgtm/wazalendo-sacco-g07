package ug.ac.vu.g07.savings;

import ug.ac.vu.g07.core.Record;
import ug.ac.vu.g07.core.Reportable;

/**
 * One savings deposit or withdrawal.
 * @author Mugeni Felestar Auma
 */
public class SavingsTransaction extends Record implements Reportable {

    public enum Type { DEPOSIT, WITHDRAWAL }

    private final String memberId;
    private final Type type;
    private final double amount;
    private final String note;

    public SavingsTransaction(String id, String memberId, Type type, double amount,
                              String note, String createdDate) throws InvalidSavingsException {
        super(id, createdDate);
        if (memberId == null || memberId.trim().isEmpty())
            throw new InvalidSavingsException("Member ID required");
        if (amount <= 0)
            throw new InvalidSavingsException("Amount must be positive");
        this.memberId = memberId.trim();
        this.type = type;
        this.amount = amount;
        this.note = note != null ? note : "";
    }

    public String getMemberId() { return memberId; }
    public Type getType() { return type; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }

    @Override
    public String describe() {
        return String.format("%s | %s | %s | %,.0f UGX | %s",
                getId(), memberId, type, amount, getCreatedDate());
    }

    @Override
    public String generateReport() { return describe(); }

    @Override
    public String toFileString() {
        return String.join("|", getId(), memberId, type.name(),
                String.valueOf(amount), note, getCreatedDate());
    }

    public static SavingsTransaction fromFileString(String line) throws InvalidSavingsException {
        String[] p = line.split("\\|", -1);
        if (p.length < 6) throw new InvalidSavingsException("Damaged savings record");
        return new SavingsTransaction(p[0], p[1], Type.valueOf(p[2]),
                Double.parseDouble(p[3]), p[4], p[5]);
    }
}
