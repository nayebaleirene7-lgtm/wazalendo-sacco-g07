package ug.ac.vu.g07.savings;

import ug.ac.vu.g07.core.FileStorage;
import ug.ac.vu.g07.members.Member;
import ug.ac.vu.g07.members.MemberService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Savings service - live balances.
 * @author Mugeni Felestar Auma
 */
public class SavingsService {
    private final List<SavingsTransaction> transactions = new ArrayList<>();
    private int nextSeq = 1;
    private static final String FILENAME = "savings.dat";
    private static final double MIN_DEPOSIT = 5000.0;
    private final MemberService memberService;

    public SavingsService(MemberService memberService) {
        this.memberService = memberService;
        load();
    }

    private void load() {
        List<SavingsTransaction> loaded = FileStorage.loadObjects(FILENAME, line -> {
            try { return SavingsTransaction.fromFileString(line); }
            catch (Exception e) { return null; }
        });
        transactions.clear();
        transactions.addAll(loaded);
        for (SavingsTransaction t : transactions) {
            try {
                int n = Integer.parseInt(t.getId().replace("G07-S", ""));
                if (n >= nextSeq) nextSeq = n + 1;
            } catch (NumberFormatException ignored) {}
        }
    }

    private void save() {
        FileStorage.saveObjects(FILENAME, transactions);
    }

    public String generateId() {
        return String.format("G07-S%03d", nextSeq++);
    }

    public void deposit(String memberId, double amount, String note) throws InvalidSavingsException {
        Member m = memberService.findById(memberId);
        if (m == null) throw new InvalidSavingsException("Member not found: " + memberId);
        if (m.getStatus() != Member.Status.ACTIVE)
            throw new InvalidSavingsException("Member is not Active");
        if (amount < MIN_DEPOSIT)
            throw new InvalidSavingsException("Minimum deposit is " + (int) MIN_DEPOSIT + " UGX");

        SavingsTransaction t = new SavingsTransaction(generateId(), memberId,
                SavingsTransaction.Type.DEPOSIT, amount, note,
                java.time.LocalDate.now().toString());
        transactions.add(t);
        save();
    }

    public void withdraw(String memberId, double amount, String note) throws InvalidSavingsException {
        Member m = memberService.findById(memberId);
        if (m == null) throw new InvalidSavingsException("Member not found: " + memberId);
        if (m.getStatus() != Member.Status.ACTIVE)
            throw new InvalidSavingsException("Member is not Active");
        double bal = getBalance(memberId);
        if (amount > bal)
            throw new InvalidSavingsException("Insufficient balance. Available: " + (int) bal + " UGX");

        SavingsTransaction t = new SavingsTransaction(generateId(), memberId,
                SavingsTransaction.Type.WITHDRAWAL, amount, note,
                java.time.LocalDate.now().toString());
        transactions.add(t);
        save();
    }

    public double getBalance(String memberId) {
        double bal = 0;
        for (SavingsTransaction t : transactions) {
            if (t.getMemberId().equalsIgnoreCase(memberId)) {
                if (t.getType() == SavingsTransaction.Type.DEPOSIT) bal += t.getAmount();
                else bal -= t.getAmount();
            }
        }
        return bal;
    }

    public List<SavingsTransaction> getTransactionsForMember(String memberId) {
        return transactions.stream()
                .filter(t -> t.getMemberId().equalsIgnoreCase(memberId))
                .collect(Collectors.toList());
    }

    public double getTotalSavings() {
        double total = 0;
        Set<String> seen = new HashSet<>();
        for (SavingsTransaction t : transactions) {
            if (seen.add(t.getMemberId())) {
                total += getBalance(t.getMemberId());
            }
        }
        return total;
    }

    public List<Map.Entry<String, Double>> getTopSavers(int n) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Member m : memberService.getAll()) {
            map.put(m.getId() + " - " + m.getFullName(), getBalance(m.getId()));
        }
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toList());
    }
}
