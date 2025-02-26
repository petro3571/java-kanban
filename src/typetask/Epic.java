package typetask;

import java.util.List;
import java.util.ArrayList;

public class Epic extends Task {
    private List<Integer> subtaskIds;

    public Epic(String name, String description) {
        super(Status.NEW, description, name);
        subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtasksIds() {
        return subtaskIds;
    }

    public void addSubtasksId(int subtaskId) {
        subtaskIds.add(subtaskId);

    }

    public void deleteSubtasksId(Integer subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id = " + getId() + ", name = " + getName() + ", description = " +
                getDescription() + ", status = " + getStatus() +
                ", subtaskIds =" + subtaskIds +
                '}';
    }
}