package typetask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class Epic extends Task {
    private List<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String description, String name) {
        super(Status.NEW, description, name, Duration.ofMinutes(1), LocalDateTime.of(1980, 1,1, 1,1));
        this.subtaskIds = new ArrayList<>();
        this.typeTask = TypeTask.EPIC;
        this.endTime = this.startTime.plusMinutes(1);
    }

    public Epic(Integer id, Status status,String name, String description, List<Integer> subtaskIds, Duration duration, LocalDateTime startTime) {
        super(id, status, name, description, duration, startTime);
        this.subtaskIds = subtaskIds;
        this.typeTask = TypeTask.EPIC;
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

    public void setEndTime(LocalDateTime ldt) {
        this.endTime = ldt;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id = " + getId() + ", name = " + getName() + ", description = " +
                getDescription() + ", status = " + getStatus() + ", duration=" +
                duration +
                ", startTime=" + startTime.format(DATE_TIME_FORMATTER) +
                ", subtaskIds =" + subtaskIds +
                '}';
    }
}