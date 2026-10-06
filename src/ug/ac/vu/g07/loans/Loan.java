package ug.ac.vu.g07.loans;

import ug.ac.vu.g07.core.Record;
import ug.ac.vu.g07.core.Reportable;

/**
 * Loan record with outstanding balance and late penalty support.
 * @author Ssempewo Brian (core) / Nayebale Irene (repayments & arrears)
 */
public class Loan extends Record implements Reportable {

    public enum Status { ACTIVE, FULLY_PAID, DEFAULTED }

    private final String memberId;
    private final double principal;
    private final double interestAmount;   // 10% flat
    private final int cycleMonths;
    private double amountRepaid;
    private Status status;
    private boolean overdue;

    public Loan(String id, String memberId, double principal, int cycleMonths, String createdDate)
            throws InvalidLoanException {
        super(id, createdDate);
        if (memberId == null || memberId.trim().isEmpty())
            throw new InvalidLoanException("Member ID required");
        if (principal <= 0)
            throw new InvalidLoanException("Principal must be positive");
        if (cycleMonths != 3 && cycleMonths != 6)
            throw new InvalidLoanException("Cycle must be 3 or 6 months");
        this.memberId = memberId.trim();
        this.principal = principal;
        this.interestAmount = principal * 0.10;
        this.cycleMonths = cycleMonths;
        this.amountRepaid = 0;
        this.status = Status.ACTIVE;
        this.overdue = false;
    }

    public String getMemberId() { return memberId; }
    public double getPrincipal() { return principal; }
    public double getInterestAmount() { return interestAmount; }
    public double getTotalRepayable() { return principal + interestAmount; }
    public double getAmountRepaid() { return amountRepaid; }
    public int getCycleMonths() { return cycleMonths; }
    public Status getStatus() { return status; }
    public boolean isOverdue() { return overdue; }

    public double getOutstanding() {
        double out = getTotalRepayable() - amountRepaid;
        if (overdue && out > 0) {
            out += out * 0.02;   // 2% penalty
        }
        return Math.max(0, out);
    }

    public void recordRepayment(double amount) throws InvalidLoanException {
        if (amount <= 0) throw new InvalidLoanException("Repayment must be positive");
        if (status != Status.ACTIVE) throw new InvalidLoanException("Loan is not active");
        amountRepaid += amount;
        if (amountRepaid >= getTotalRepayable()) {
            amountRepaid = getTotalRepayable();
            status = Status.FULLY_PAID;
            overdue = false;
        }
    }

    public void markOverdue() {
        if (status == Status.ACTIVE) overdue = true;
    }

    @Override
    public String describe() {
        return String.format("%s | Member %s | Principal %,.0f | Outstanding %,.0f | %s%s",
                getId(), memberId, principal, getOutstanding(), status,
                overdue ? " (OVERDUE)" : "");
    }

    @Override
    public String generateReport() { return describe(); }

    @Override
    public String toFileString() {
        return String.join("|",
                getId(), memberId,
                String.valueOf(principal),
                String.valueOf(interestAmount),
                String.valueOf(cycleMonths),
                String.valueOf(amountRepaid),
                status.name(),
                String.valueOf(overdue),
                getCreatedDate());
    }

    public static Loan fromFileString(String line) throws InvalidLoanException {
        String[] p = line.split("\\|", -1);
        if (p.length < 9) throw new InvalidLoanException("Damaged loan record");
        Loan loan = new Loan(p[0], p[1], Double.parseDouble(p[2]), Integer.parseInt(p[4]), p[8]);
        loan.amountRepaid = Double.parseDouble(p[5]);
        loan.status = Status.valueOf(p[6]);
        loan.overdue = Boolean.parseBoolean(p[7]);
        return loan;
    }
}
