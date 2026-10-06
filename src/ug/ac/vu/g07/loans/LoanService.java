package ug.ac.vu.g07.loans;

import ug.ac.vu.g07.core.FileStorage;
import ug.ac.vu.g07.members.Member;
import ug.ac.vu.g07.members.MemberService;
import ug.ac.vu.g07.savings.SavingsService;
import ug.ac.vu.g07.shares.ShareService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Loan service with 3x eligibility rule.
 * @author Ssempewo Brian / Nayebale Irene
 */
public class LoanService {
    private final List<Loan> loans = new ArrayList<>();
    private int nextSeq = 1;
    private static final String FILENAME = "loans.dat";

    private final MemberService memberService;
    private final SavingsService savingsService;
    private final ShareService shareService;

    public LoanService(MemberService memberService, SavingsService savingsService, ShareService shareService) {
        this.memberService = memberService;
        this.savingsService = savingsService;
        this.shareService = shareService;
        load();
    }

    private void load() {
        List<Loan> loaded = FileStorage.loadObjects(FILENAME, line -> {
            try { return Loan.fromFileString(line); }
            catch (Exception e) { return null; }
        });
        loans.clear();
        loans.addAll(loaded);
        for (Loan l : loans) {
            try {
                int n = Integer.parseInt(l.getId().replace("G07-L", ""));
                if (n >= nextSeq) nextSeq = n + 1;
            } catch (NumberFormatException ignored) {}
        }
    }

    private void save() {
        FileStorage.saveObjects(FILENAME, loans);
    }

    public String generateId() {
        return String.format("G07-L%03d", nextSeq++);
    }

    public double getMaxLoan(String memberId) {
        double savings = savingsService.getBalance(memberId);
        double shares = shareService.getShareCapital(memberId);
        return 3.0 * (savings + shares);
    }

    public Loan applyForLoan(String memberId, double principal, int cycleMonths)
            throws InvalidLoanException {
        Member m = memberService.findById(memberId);
        if (m == null) throw new InvalidLoanException("Member not found");
        if (m.getStatus() != Member.Status.ACTIVE)
            throw new InvalidLoanException("Only Active members can borrow");

        double max = getMaxLoan(memberId);
        if (principal > max) {
            throw new InvalidLoanException(String.format(
                    "Requested %,.0f exceeds maximum allowed %,.0f (3 x savings+shares)",
                    principal, max));
        }

        for (Loan l : loans) {
            if (l.getMemberId().equalsIgnoreCase(memberId) && l.getStatus() == Loan.Status.ACTIVE) {
                throw new InvalidLoanException("Member already has an active loan");
            }
        }

        Loan loan = new Loan(generateId(), memberId, principal, cycleMonths,
                java.time.LocalDate.now().toString());
        loans.add(loan);
        save();
        return loan;
    }

    public void repay(String loanId, double amount) throws InvalidLoanException {
        Loan loan = findById(loanId);
        if (loan == null) throw new InvalidLoanException("Loan not found");
        loan.recordRepayment(amount);
        save();
    }

    public void markOverdue(String loanId) throws InvalidLoanException {
        Loan loan = findById(loanId);
        if (loan == null) throw new InvalidLoanException("Loan not found");
        loan.markOverdue();
        save();
    }

    public Loan findById(String id) {
        for (Loan l : loans) {
            if (l.getId().equalsIgnoreCase(id)) return l;
        }
        return null;
    }

    public List<Loan> getActiveLoansSortedByOutstanding() {
        return loans.stream()
                .filter(l -> l.getStatus() == Loan.Status.ACTIVE)
                .sorted(Comparator.comparingDouble(Loan::getOutstanding).reversed())
                .collect(Collectors.toList());
    }

    public List<Loan> getLoansForMember(String memberId) {
        return loans.stream()
                .filter(l -> l.getMemberId().equalsIgnoreCase(memberId))
                .collect(Collectors.toList());
    }

    public double getTotalOutstanding() {
        return loans.stream()
                .filter(l -> l.getStatus() == Loan.Status.ACTIVE)
                .mapToDouble(Loan::getOutstanding)
                .sum();
    }

    public double getOutstandingForMember(String memberId) {
        return getLoansForMember(memberId).stream()
                .filter(l -> l.getStatus() == Loan.Status.ACTIVE)
                .mapToDouble(Loan::getOutstanding)
                .sum();
    }
}
