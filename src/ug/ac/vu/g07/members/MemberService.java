package ug.ac.vu.g07.members;

import ug.ac.vu.g07.core.FileStorage;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages members collection.
 * @author Ninsiima Douglas
 */
public class MemberService {
    private final List<Member> members = new ArrayList<>();
    private int nextSeq = 1;
    private static final String FILENAME = "members.dat";

    public MemberService() { load(); }

    private void load() {
        List<Member> loaded = FileStorage.loadObjects(FILENAME, line -> {
            try { return Member.fromFileString(line); }
            catch (Exception e) { return null; }
        });
        members.clear();
        members.addAll(loaded);
        for (Member m : members) {
            try {
                int n = Integer.parseInt(m.getId().replace("G07-M", ""));
                if (n >= nextSeq) nextSeq = n + 1;
            } catch (NumberFormatException ignored) {}
        }
    }

    private void save() {
        FileStorage.saveObjects(FILENAME, members);
    }

    public String generateId() {
        return String.format("G07-M%03d", nextSeq++);
    }

    public void add(Member m) {
        members.add(m);
        save();
    }

    public Member findById(String id) {
        if (id == null) return null;
        for (Member m : members) {
            if (m.getId().equalsIgnoreCase(id.trim())) return m;
        }
        return null;
    }

    public List<Member> findByName(String partial) {
        String q = partial.toLowerCase();
        return members.stream()
                .filter(m -> m.getFullName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Member> getAllSortedByName() {
        List<Member> sorted = new ArrayList<>(members);
        sorted.sort(Comparator.comparing(Member::getFullName));
        return sorted;
    }

    public List<Member> getAll() {
        return new ArrayList<>(members);
    }

    public int count() { return members.size(); }

    public void update(Member m) { save(); }
}
