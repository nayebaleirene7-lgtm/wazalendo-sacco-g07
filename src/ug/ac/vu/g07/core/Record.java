package ug.ac.vu.g07.core;

/**
 * Shared abstract class used by every module.
 * @author Group One (G07) - Integration Lead (Ssempewo Brian)
 */
public abstract class Record {
    private String id;
    private String createdDate;

    public Record(String id, String createdDate) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be blank");
        }
        this.id = id.trim();
        this.createdDate = (createdDate != null) ? createdDate : java.time.LocalDate.now().toString();
    }

    public String getId() { return id; }
    public String getCreatedDate() { return createdDate; }

    public abstract String describe();
    public abstract String toFileString();
}
