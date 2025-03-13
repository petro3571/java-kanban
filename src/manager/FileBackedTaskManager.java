package manager;

import exceptions.ManagerSaveException;
import typetask.*;
import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private Path path;
    private TreeSet<Task> prioritizedTasksSet;
    private boolean isPrioritizedTasksSetUpdated = false;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy | HH:mm");

    public FileBackedTaskManager(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        setDurationStartAndEndTimeEpic(subtask.getEpic());
        save();
        isPrioritizedTasksSetUpdated = false;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
            save();
        isPrioritizedTasksSetUpdated = false;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
            save();
        isPrioritizedTasksSetUpdated = false;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        setDurationStartAndEndTimeEpic(subtask.getEpic());
            save();
        isPrioritizedTasksSetUpdated = false;
    }

    @Override
    public void deleteTaskByIndex(int id) {
        super.deleteTaskByIndex(id);
            save();
        isPrioritizedTasksSetUpdated = false;
    }

    @Override
    public void deleteEpicByIndex(int id) {
        super.deleteEpicByIndex(id);
            save();
        isPrioritizedTasksSetUpdated = false;
    }

    @Override
    public void deleteSubtaskByIndex(int id) {
        super.deleteSubtaskByIndex(id);
        setDurationStartAndEndTimeEpic(getSubtaskByIndex(id).getEpic());
            save();
        isPrioritizedTasksSetUpdated = false;
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
            save();
        isPrioritizedTasksSetUpdated = false;
    }

    @Override
    public void addTask(Task task) {
        for (Task t : getAllTasks()) {
            if (t.isTimeCross(task)) {
                throw new ManagerSaveException("Ошибка: пересечение времен.");
            }
        }
        super.addTask(task);
        if (!(task.getDuration() == null || task.getStartTime() == null)) {
            save();
            isPrioritizedTasksSetUpdated = false;
        }
    }

    public static FileBackedTaskManager loadFromFile(Path path) throws FileNotFoundException {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(path);
        FileReader reader = new FileReader(String.valueOf(path));
        int id;
        int maxId = 0;
        try (BufferedReader br = new BufferedReader(reader)) {
            while (br.ready()) {
                String str = br.readLine();
                if (!str.isEmpty()) {
                    TypeTask typeTask = fromString(str,fileBackedTaskManager).getTypeTask();
                    switch (typeTask) {
                        case TASK:
                            Task task = fromString(str, fileBackedTaskManager);
                            id = task.getId();
                            if (id > maxId) {
                                maxId = id;
                            }
                            fileBackedTaskManager.tasks.put(id, task);
                            break;
                        case EPIC:
                            Epic epic = (Epic) fromString(str, fileBackedTaskManager);
                            id = epic.getId();
                            if (id > maxId) {
                                maxId = id;
                            }
                            fileBackedTaskManager.epics.put(id, epic);
                            break;
                        case SUBTASK:
                            Subtask subtask = (Subtask) fromString(str, fileBackedTaskManager);
                            id = subtask.getId();
                            if (id > maxId) {
                                maxId = id;
                            }
                            fileBackedTaskManager.subtasks.put(id, subtask);
                            subtask.getEpic().addSubtasksId(id);
                            break;
                        default:
                            throw new IllegalArgumentException("Неизвестный тип задачи");
                    }
                }
            }
        }  catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла.");
        }
        fileBackedTaskManager.index = maxId;
        return fileBackedTaskManager;
    }

    public static Task fromString(String value, FileBackedTaskManager fileBackedTaskManager) throws IllegalArgumentException {
        String[] splitString = value.split(",");
        int id = Integer.parseInt(splitString[0]);
        TypeTask type = TypeTask.valueOf(splitString[1]);
        String name = splitString[2];
        Status status = Status.valueOf(splitString[3]);
        String description = splitString[4];
        Long durationLong = Long.parseLong(splitString[5]);
        Duration duration = Duration.ofMinutes(durationLong);
        LocalDateTime localDateTime = LocalDateTime.parse(splitString[6]);
        switch (type) {
            case TASK:
                return new Task(id, status, description, name, duration, localDateTime);
            case EPIC:
                List<Integer> newListSubId = new ArrayList<>();
                return new Epic(id,status,name,description,newListSubId, duration, localDateTime);
            case SUBTASK:
                int epicId = Integer.parseInt(splitString[7]);
                Epic epic = fileBackedTaskManager.getEpicByIndex(epicId);
                return new Subtask(status, description, name, epic,id,duration,localDateTime);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }

    public TreeSet<Task> getPrioritizedTasksSet() {
        if (!isPrioritizedTasksSetUpdated) {
            updatePrioritizedTasksSet();
        }
        return prioritizedTasksSet;
    }

    private void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(String.valueOf(path)))) {
            for (Task task : getAllTasks()) {
                bw.write(toString(task));
                bw.newLine();
            }
            for (Epic epic : getAllEpics()) {
                bw.write(toString(epic));
                bw.newLine();
            }
            for (Subtask subtask : getAllSubtasks()) {
                bw.write(toString(subtask));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи в файл.");
        }
    }

    private String toString(Task task) {
        return String.join(",", String.valueOf(task.getId()), task.getTypeTask().toString(), task.getName(),
                task.getStatus().name(), task.getDescription(), String.valueOf(task.getDuration().toMinutes()), String.valueOf(task.getStartTime()));
    }

    private String toString(Epic epic) {
        return String.join(",", String.valueOf(epic.getId()), epic.getTypeTask().toString(), epic.getName(),
                epic.getStatus().name(), epic.getDescription(), String.valueOf(epic.getDuration().toMinutes()),String.valueOf(epic.getStartTime()), epic.getSubtasksIds().toString());
    }

    private String toString(Subtask subtask) {
        return String.join(",", String.valueOf(subtask.getId()), subtask.getTypeTask().toString(),
                subtask.getName(), subtask.getStatus().name(), subtask.getDescription(),String.valueOf(subtask.getDuration().toMinutes()),String.valueOf(subtask.getStartTime()), String.valueOf(subtask.getEpic().getId()));
    }

    private void updatePrioritizedTasksSet() {
        prioritizedTasksSet = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        for (Task task : getAllTasks()) {
            if (!(task.getStartTime() == null)) {
                prioritizedTasksSet.add(task);
            }
        }
        for (Subtask subtask : getAllSubtasks()) {
            if (!(subtask.getStartTime() == null)) {
                prioritizedTasksSet.add(subtask);
            }
        }
        isPrioritizedTasksSetUpdated = true;
    }

    private void setDurationStartAndEndTimeEpic(Epic epic) {
        List<Integer> newList = epic.getSubtasksIds();
        Duration newDuration = epic.getDuration();
        for (Integer id : newList) {
            Subtask newSubtask = getSubtaskByIndex(id);
            LocalDateTime endSubTaskTime = newSubtask.getEndTime();
            if (epic.getStartTime() == null || newSubtask.getStartTime().isBefore(epic.getStartTime())) {
                epic.setStartTime(newSubtask.getStartTime());
            }
            if (epic.getEndTime() == null || newSubtask.getEndTime().isAfter(epic.getEndTime())) {
                epic.setEndTime(endSubTaskTime);
            }
            epic.setDuration(newDuration.plus(newSubtask.getDuration()));
        }
//        epic.setDuration(Duration.between(epic.getStartTime(), epic.getEndTime()));

        updateEpicStatus(epic);
    }
}