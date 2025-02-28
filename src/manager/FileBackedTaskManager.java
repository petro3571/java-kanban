package manager;

import Exceptions.ManagerSaveException;
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

    private void save() throws IOException {
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

    public static FileBackedTaskManager loadFromFile(Path path) throws IOException {
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

    private String toString(Task task) {
        return String.join(",", String.valueOf(task.getId()), task.getTypeTask().toString(), task.getName(), task.getStatus().name(), task.getDescription());
    }

    private String toString(Epic epic) {
        return String.join(",", String.valueOf(epic.getId()), epic.getTypeTask().toString(), epic.getName(), epic.getStatus().name(), epic.getDescription(), epic.getSubtasksIds().toString());
    }

    private String toString(Subtask subtask) {
        return String.join(",", String.valueOf(subtask.getId()), subtask.getTypeTask().toString(), subtask.getName(), subtask.getStatus().name(), subtask.getDescription(), String.valueOf(subtask.getEpic().getId()));
    }

    public static Task fromString(String value, FileBackedTaskManager fileBackedTaskManager) throws IllegalArgumentException {
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
                return new Epic(id,status,name,description,newListSubId);
            case SUBTASK:
                    int epicId = Integer.parseInt(splitString[5]);
                    Epic epic = fileBackedTaskManager.getEpicByIndex(epicId);
                return new Subtask(status, description, name, epic,id);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }
}