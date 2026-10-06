package ug.ac.vu.g07.members;

import ug.ac.vu.g07.core.Record;
import ug.ac.vu.g07.core.Reportable;

/**
 * Member model.
 * @author Ninsiima Douglas
 */
public class Member extends Record implements Reportable {

    public enum Status { ACTIVE, SUSPENDED, WITHDRAWN }

    private String fullName;
    private String nationalId;
    private String phone;
    private String nextOfKinName;
    private String nextOfKinPhone;
    private Status status;

    public Member(String id, String fullName, String nationalId, String phone,
                  String nextOfKinName, String nextOfKinPhone, String createdDate)
            throws InvalidMemberException {
        super(id, createdDate);
        setFullName(fullName);
        setNationalId(nationalId);
        setPhone(phone);
        setNextOfKinName(nextOfKinName);
        setNextOfKinPhone(nextOfKinPhone);
        this.status = Status.ACTIVE;
    }

    public String getFullName() { return fullName; }
    public String getNationalId() { return nationalId; }
    public String getPhone() { return phone; }
    public String getNextOfKinName() { return nextOfKinName; }
    public String getNextOfKinPhone() { return nextOfKinPhone; }
    public Status getStatus() { return status; }

    public void setFullName(String fullName) throws InvalidMemberException {
        if (fullName == null || fullName.trim().isEmpty())
            throw new InvalidMemberException("Full name cannot be blank.");
        this.fullName = fullName.trim();
    }

    public void setNationalId(String nationalId) throws InvalidMemberException {
        if (nationalId == null || nationalId.trim().isEmpty())
            throw new InvalidMemberException("National ID cannot be blank.");
        this.nationalId = nationalId.trim();
    }

    public void setPhone(String phone) throws InvalidMemberException {
        if (phone == null || phone.trim().isEmpty())
            throw new InvalidMemberException("Phone cannot be blank.");
        this.phone = phone.trim();
    }

    public void setNextOfKinName(String nextOfKinName) throws InvalidMemberException {
        if (nextOfKinName == null || nextOfKinName.trim().isEmpty())
            throw new InvalidMemberException("Next of kin name cannot be blank.");
        this.nextOfKinName = nextOfKinName.trim();
    }

    public void setNextOfKinPhone(String nextOfKinPhone) {
        this.nextOfKinPhone = nextOfKinPhone != null ? nextOfKinPhone.trim() : "";
    }

    public void setStatus(Status status) { this.status = status; }

    @Override
    public String describe() {
        return String.format("%s | %s | %s | %s", getId(), fullName, phone, status);
    }

    @Override
    public String generateReport() { return describe(); }

    @Override
    public String toFileString() {
        return String.join("|", getId(), fullName, nationalId, phone,
                nextOfKinName, nextOfKinPhone, status.name(), getCreatedDate());
    }

    public static Member fromFileString(String line) throws InvalidMemberException {
        String[] p = line.split("\\|", -1);
        if (p.length < 8) throw new InvalidMemberException("Damaged member record");
        Member m = new Member(p[0], p[1], p[2], p[3], p[4], p[5], p[7]);
        m.setStatus(Status.valueOf(p[6]));
        return m;
    }

    @Override
    public String toString() { return describe(); }
}
