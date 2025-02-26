package manager;

import typetask.Epic;
import typetask.Subtask;
import typetask.Task;

import java.util.List;

public interface TaskManager {
    Task getTaskByIndex(int id);

    Epic getEpicByIndex(int id);

    Subtask getSubtaskByIndex(int id);

    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubtask(Subtask subtask);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void deleteTaskByIndex(int id);

    void deleteEpicByIndex(int id);

    void deleteSubtaskByIndex(int id);

    void deleteTasks();

    void deleteEpics();

    void deleteSubtasks();

    List<Task> getHistory();

    List<Subtask> getEpicSubtasks(int id);
}