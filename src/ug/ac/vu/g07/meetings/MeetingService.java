package ug.ac.vu.g07.meetings;

import ug.ac.vu.g07.core.FileStorage;
import ug.ac.vu.g07.members.Member;
import ug.ac.vu.g07.members.MemberService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Meetings and fines service.
 * @author Kazi Fredrick
 */
public class MeetingService {
    private final List<Fine> fines = new ArrayList<>();
    private int nextSeq = 1;
    private static final String FILENAME = "fines.dat";
    private final MemberService memberService;

    public MeetingService(MemberService memberService) {
        this.memberService = memberService;
        load();
    }

    private void load() {
        List<Fine> loaded = FileStorage.loadObjects(FILENAME, line -> {
            try { return Fine.fromFileString(line); }
            catch (Exception e) { return null; }
        });
        fines.clear();
        fines.addAll(loaded);
        for (Fine f : fines) {
            try {
                int n = Integer.parseInt(f.getId().replace("G07-F", ""));
                if (n >= nextSeq) nextSeq = n + 1;
            } catch (NumberFormatException ignored) {}
        }
    }

    private void save() {
        FileStorage.saveObjects(FILENAME, fines);
    }

    public String generateId() {
        return String.format("G07-F%03d", nextSeq++);
    }

    public Fine createAbsenceFine(String memberId) throws InvalidMeetingException {
        Member m = memberService.findById(memberId);
        if (m == null) throw new InvalidMeetingException("Member not found");
        Fine f = new Fine(generateId(), memberId, Fine.ABSENCE_FINE,
                "Absent without apology", java.time.LocalDate.now().toString());
        fines.add(f);
        save();
        return f;
    }

    public Fine createLateFine(String memberId) throws InvalidMeetingException {
        Member m = memberService.findById(memberId);
        if (m == null) throw new InvalidMeetingException("Member not found");
        Fine f = new Fine(generateId(), memberId, Fine.LATE_FINE,
                "Late arrival", java.time.LocalDate.now().toString());
        fines.add(f);
        save();
        return f;
    }

    public void payFine(String fineId) throws InvalidMeetingException {
        Fine f = findById(fineId);
        if (f == null) throw new InvalidMeetingException("Fine not found");
        if (f.isPaid()) throw new InvalidMeetingException("Fine already paid");
        f.setPaid(true);
        save();
    }

    public Fine findById(String id) {
        for (Fine f : fines) {
            if (f.getId().equalsIgnoreCase(id)) return f;
        }
        return null;
    }

    public List<Fine> getUnpaidFinesForMember(String memberId) {
        return fines.stream()
                .filter(f -> f.getMemberId().equalsIgnoreCase(memberId) && !f.isPaid())
                .collect(Collectors.toList());
    }

    public double getUnpaidTotalForMember(String memberId) {
        return getUnpaidFinesForMember(memberId).stream()
                .mapToDouble(Fine::getAmount)
                .sum();
    }

    public double getTotalFinesCollected() {
        return fines.stream()
                .filter(Fine::isPaid)
                .mapToDouble(Fine::getAmount)
                .sum();
    }

    public List<Fine> getAllFines() {
        return new ArrayList<>(fines);
    }
}
