package ug.ac.vu.g07.meetings;

import ug.ac.vu.g07.core.Record;
import ug.ac.vu.g07.core.Reportable;

/**
 * A fine for absence or lateness.
 * @author Kazi Fredrick
 */
public class Fine extends Record implements Reportable {

    public static final double ABSENCE_FINE = 2000.0;
    public static final double LATE_FINE = 500.0;

    private final String memberId;
    private final double amount;
    private final String reason;
    private boolean paid;

    public Fine(String id, String memberId, double amount, String reason, String createdDate)
            throws InvalidMeetingException {
        super(id, createdDate);
        if (memberId == null || memberId.trim().isEmpty())
            throw new InvalidMeetingException("Member ID required");
        if (amount <= 0)
            throw new InvalidMeetingException("Fine amount must be positive");
        this.memberId = memberId.trim();
        this.amount = amount;
        this.reason = reason != null ? reason : "";
        this.paid = false;
    }

    public String getMemberId() { return memberId; }
    public double getAmount() { return amount; }
    public String getReason() { return reason; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    @Override
    public String describe() {
        return String.format("%s | %s | %,.0f UGX | %s | %s",
                getId(), memberId, amount, reason, paid ? "PAID" : "UNPAID");
    }

    @Override
    public String generateReport() { return describe(); }

    @Override
    public String toFileString() {
        return String.join("|", getId(), memberId,
                String.valueOf(amount), reason, String.valueOf(paid), getCreatedDate());
    }

    public static Fine fromFileString(String line) throws InvalidMeetingException {
        String[] p = line.split("\\|", -1);
        if (p.length < 6) throw new InvalidMeetingException("Damaged fine record");
        Fine f = new Fine(p[0], p[1], Double.parseDouble(p[2]), p[3], p[5]);
        f.setPaid(Boolean.parseBoolean(p[4]));
        return f;
    }
}
