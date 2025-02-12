package typetask;

public class Task {
    private String name;
    private String description;
    private Status status;
    private int id;

    public Task(Status status, String description, String name) {
        this.status = status;
        this.description = description;
        this.name = name;
    }

    private Task(int id, Status status, String description, String name) {
        this.id = id;
        this.status = status;
        this.description = description;
        this.name = name;
    }

    public Task getSnapshot() {
        return new Task(this.getId(), this.getStatus(), this.getDescription(), this.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                '}';
    }
}