package manager;

import typetask.*;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private Path path;

    public FileBackedTaskManager(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteTaskByIndex(int id) {
        super.deleteTaskByIndex(id);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEpicByIndex(int id) {
        super.deleteEpicByIndex(id);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteSubtaskByIndex(int id) {
        super.deleteSubtaskByIndex(id);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() throws IOException {
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
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    static FileBackedTaskManager loadFromFile(Path path) throws FileNotFoundException {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(path);
        FileReader reader = new FileReader(String.valueOf(path));
        try (BufferedReader br = new BufferedReader(reader)) {
            while (br.ready()) {
                String str = br.readLine();
                if (!str.isEmpty()) {
                    Task task = fromString(str);
                    if (task.getClass().toString().equals("class typetask.Task")) {
                        fileBackedTaskManager.tasks.put(task.getId(), task);
                    } else if (task.getClass().toString().equals("class typetask.Epic")) {
                        fileBackedTaskManager.epics.put(task.getId(), (Epic) task);
                    } else if (task.getClass().toString().equals("class typetask.Subtask")) {
                        fileBackedTaskManager.subtasks.put(task.getId(), (Subtask) task);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileBackedTaskManager;
    }

    private String toString(Task task) {
        return String.join(",", String.valueOf(task.getId()), task.getClass().toString().substring(15).toUpperCase(), task.getName(), task.getStatus().name(), task.getDescription(), " ");
    }

    private String toString(Epic epic) {
        return String.join(",", String.valueOf(epic.getId()), epic.getClass().toString().substring(15).toUpperCase(), epic.getName(), epic.getStatus().name(), epic.getDescription(), epic.getSubtasksIds().toString());
    }

    private String toString(Subtask subtask) {
        return String.join(",", String.valueOf(subtask.getId()), subtask.getClass().toString().substring(15).toUpperCase(), subtask.getName(), subtask.getStatus().name(), subtask.getDescription(), String.valueOf(subtask.getEpic().getId()));
    }

    public static Task fromString(String value) throws IllegalArgumentException {
        String[] splitString = value.split(",");
        int id = Integer.parseInt(splitString[0]);
        TypeTask type = TypeTask.valueOf(splitString[1]);
        String name = splitString[2];
        Status status = Status.valueOf(splitString[3]);
        String description = splitString[4];
        switch (type) {
            case TASK:
                return new Task(id, status, description, name);
            case EPIC:
                List<Integer> newListSubId = new ArrayList<>();
                String str = splitString[5].substring(1);
                String newStr = splitString[6].substring(1,2);
                int firstId = Integer.parseInt(str);
                int secondId = Integer.parseInt(newStr);
                newListSubId.add(firstId);
                newListSubId.add(secondId);
                return new Epic(id,status,name,description,newListSubId);
            case SUBTASK:
                    int count = Integer.parseInt(splitString[5]);
                return new Subtask(status, description, name, id);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }
}