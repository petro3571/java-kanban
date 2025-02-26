package typetask;

public class Subtask extends Task {
    private Epic epic;

    public Subtask(Status status, String description, String name, Epic epic) {
        super(status,description,name);
        this.epic = epic;
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