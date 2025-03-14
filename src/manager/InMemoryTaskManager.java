package manager;

import exceptions.ManagerSaveException;
import typetask.Epic;
import typetask.Status;
import typetask.Subtask;
import typetask.Task;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistoryManager();
    protected TreeSet<Task> prioritizedTasksSet;
    protected boolean isPrioritizedTasksSetUpdated = false;
    protected int index = 1;

    @Override
    public Task getTaskByIndex(int id) {
        historyManager.addTask(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpicByIndex(int id) {
        historyManager.addTask(epics.get(id));
        return epics.get(id);
    }

    @Override
    public Subtask getSubtaskByIndex(int id) {
        historyManager.addTask(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public void addTask(Task task) {
        task.setId(idCounter());
        tasks.put(task.getId(), task);
        isPrioritizedTasksSetUpdated = false;
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(idCounter());
        epics.put(epic.getId(), epic);
        isPrioritizedTasksSetUpdated = false;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpic().getId());
        if (epic != null) {
            subtask.setId(idCounter());
            subtasks.put(subtask.getId(), subtask);
            epic.addSubtasksId(subtask.getId());
            isPrioritizedTasksSetUpdated = false;
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void updateTask(Task task) {
        for (Task t : getAllTasks()) {
            if (t.getId() != task.getId() && t.isTimeCross(task)) {
                throw new ManagerSaveException("Ошибка: пересечение времен.");
            }
        }
        if (task != null) {
            tasks.put(task.getId(), task);
            isPrioritizedTasksSetUpdated = false;
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            isPrioritizedTasksSetUpdated = false;
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask != null) {
            subtasks.put(subtask.getId(), subtask);
            Epic epic = subtask.getEpic();
            if (epic != null) {
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public void deleteTaskByIndex(int id) {
        tasks.remove(id);
        historyManager.remove(id);
        isPrioritizedTasksSetUpdated = false;
    }

    @Override
    public void deleteEpicByIndex(int id) {
        Epic epic = epics.remove(id);
        historyManager.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtasksIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
                isPrioritizedTasksSetUpdated = false;
            }
        }
    }

    @Override
    public void deleteSubtaskByIndex(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = subtask.getEpic();
            if (epic != null) {
                epic.deleteSubtasksId(subtask.getId());
                updateEpicStatus(epic);
                historyManager.remove(subtask.getId());
                isPrioritizedTasksSetUpdated = false;
            }
        }
    }

    @Override
    public void deleteTasks() {
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        for (Integer epicId : epics.keySet()) {
            historyManager.remove(epicId);
        }
        epics.clear();
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasksIds().clear();
            updateEpicStatus(epic);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Subtask> getEpicSubtasks(int id) {
        return subtasks.values().stream()
                .filter(subtask -> subtask.getEpic().getId() == id)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtasksIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                if (subtask.getStatus() != Status.NEW) {
                    allNew = false;
                }
                if (subtask.getStatus() != Status.DONE) {
                    allDone = false;
                }
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public TreeSet<Task> getPrioritizedTasksSet() {
        if (!isPrioritizedTasksSetUpdated) {
            updatePrioritizedTasksSet();
        }
        return prioritizedTasksSet;
    }

    @Override
    public void updatePrioritizedTasksSet() {
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

    private int idCounter() {
        return index++;
    }
}