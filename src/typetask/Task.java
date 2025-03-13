package typetask;

import java.time.Duration;
import java.time.LocalDateTime;

public class Task {
    private String name;
    private String description;
    private Status status;
    private int id;
    protected TypeTask typeTask;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(Status status, String description, String name) {
        this.status = status;
        this.description = description;
        this.name = name;
        this.typeTask = TypeTask.TASK;
    }

    public Task(int id, Status status, String description, String name, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.status = status;
        this.description = description;
        this.name = name;
        this.typeTask = TypeTask.TASK;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(Status status, String description, String name, Duration duration, LocalDateTime startTime) {
        this.status = status;
        this.description = description;
        this.name = name;
        this.duration = duration;
        this.startTime = startTime;
        this.typeTask = TypeTask.TASK;
    }

    public Task getSnapshot() {
        return new Task(this.getId(), this.getStatus(), this.getDescription(), this.getName(), this.getDuration(), this.getStartTime());
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

    public TypeTask getTypeTask() {
        return this.typeTask;
    }

    public void setTypeTask(TypeTask typeTask) {
        this.typeTask = typeTask;
    }

    public Duration getDuration() {
        return duration;
    }

    public long getDurationInMinutes(Duration duration) {
        return duration.toMinutes();
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    public boolean isTimeCross(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null ||
                this.getStartTime() == null || this.getEndTime() == null) {
            return false;
        }
        return !(this.getStartTime().isAfter(task.getEndTime()) || this.getEndTime().isBefore(task.getStartTime()));
    }
    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", id=" + id +
                '}';
    }
}