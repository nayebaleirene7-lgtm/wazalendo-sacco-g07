package ug.ac.vu.g07.shares;

import ug.ac.vu.g07.core.Record;
import ug.ac.vu.g07.core.Reportable;

/**
 * Records a share purchase.
 * @author Ssenvume Andrew
 */
public class ShareHolding extends Record implements Reportable {

    public static final double SHARE_VALUE = 10000.0;

    private final String memberId;
    private final int numberOfShares;

    public ShareHolding(String id, String memberId, int numberOfShares, String createdDate)
            throws InvalidShareException {
        super(id, createdDate);
        if (memberId == null || memberId.trim().isEmpty())
            throw new InvalidShareException("Member ID required");
        if (numberOfShares <= 0)
            throw new InvalidShareException("Number of shares must be positive");
        this.memberId = memberId.trim();
        this.numberOfShares = numberOfShares;
    }

    public String getMemberId() { return memberId; }
    public int getNumberOfShares() { return numberOfShares; }
    public double getCapital() { return numberOfShares * SHARE_VALUE; }

    @Override
    public String describe() {
        return String.format("%s | %s | %d shares | %,.0f UGX",
                getId(), memberId, numberOfShares, getCapital());
    }

    @Override
    public String generateReport() { return describe(); }

    @Override
    public String toFileString() {
        return String.join("|", getId(), memberId,
                String.valueOf(numberOfShares), getCreatedDate());
    }

    public static ShareHolding fromFileString(String line) throws InvalidShareException {
        String[] p = line.split("\\|", -1);
        if (p.length < 4) throw new InvalidShareException("Damaged share record");
        return new ShareHolding(p[0], p[1], Integer.parseInt(p[2]), p[3]);
    }
}
