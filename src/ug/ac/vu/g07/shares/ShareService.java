package ug.ac.vu.g07.shares;

import ug.ac.vu.g07.core.FileStorage;
import ug.ac.vu.g07.members.Member;
import ug.ac.vu.g07.members.MemberService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Shares service.
 * @author Ssenvume Andrew
 */
public class ShareService {
    private final List<ShareHolding> holdings = new ArrayList<>();
    private int nextSeq = 1;
    private static final String FILENAME = "shares.dat";
    private final MemberService memberService;

    public ShareService(MemberService memberService) {
        this.memberService = memberService;
        load();
    }

    private void load() {
        List<ShareHolding> loaded = FileStorage.loadObjects(FILENAME, line -> {
            try { return ShareHolding.fromFileString(line); }
            catch (Exception e) { return null; }
        });
        holdings.clear();
        holdings.addAll(loaded);
        for (ShareHolding h : holdings) {
            try {
                int n = Integer.parseInt(h.getId().replace("G07-SH", ""));
                if (n >= nextSeq) nextSeq = n + 1;
            } catch (NumberFormatException ignored) {}
        }
    }

    private void save() {
        FileStorage.saveObjects(FILENAME, holdings);
    }

    public String generateId() {
        return String.format("G07-SH%03d", nextSeq++);
    }

    public void buyShares(String memberId, int numberOfShares) throws InvalidShareException {
        Member m = memberService.findById(memberId);
        if (m == null) throw new InvalidShareException("Member not found: " + memberId);
        if (m.getStatus() != Member.Status.ACTIVE)
            throw new InvalidShareException("Member is not Active");

        int current = getTotalShares(memberId);
        if (current == 0 && numberOfShares < 10) {
            throw new InvalidShareException("First purchase must be at least 10 shares");
        }

        ShareHolding h = new ShareHolding(generateId(), memberId, numberOfShares,
                java.time.LocalDate.now().toString());
        holdings.add(h);
        save();
    }

    public int getTotalShares(String memberId) {
        int total = 0;
        for (ShareHolding h : holdings) {
            if (h.getMemberId().equalsIgnoreCase(memberId)) {
                total += h.getNumberOfShares();
            }
        }
        return total;
    }

    public double getShareCapital(String memberId) {
        return getTotalShares(memberId) * ShareHolding.SHARE_VALUE;
    }

    public double getTotalShareCapital() {
        double total = 0;
        for (Member m : memberService.getAll()) {
            total += getShareCapital(m.getId());
        }
        return total;
    }

    public List<Map.Entry<String, Integer>> getRanking() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Member m : memberService.getAll()) {
            map.put(m.getId() + " - " + m.getFullName(), getTotalShares(m.getId()));
        }
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
    }
}
