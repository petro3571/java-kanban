package typetask;

public class Subtask extends Task {
    private Epic epic;

    public Subtask(Status status, String description, String name, Epic epic) {
        super(status, description, name);
        this.epic = epic;
        this.typeTask = TypeTask.SUBTASK;
    }

    public Subtask(Status status, String description, String name, Epic epic, int id) {
        super(status, description, name);
        this.epic = epic;
        this.setId(id);
        this.typeTask = TypeTask.SUBTASK;
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id = " + getId() + ", name = " + getName() + ", description = " +
                getDescription() + ", status = " + getStatus() +
                '}';
    }
}