package manager;

import exceptions.ManagerSaveException;
import typetask.Epic;
import typetask.Status;
import typetask.Subtask;
import typetask.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistoryManager();
    protected Set<Task> prioritizedTasksSet = new TreeSet<>(Comparator.comparing(Task::getStartTime));
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
        try {
            isTaskCrossAnother(task);
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException("Ошибка: пересечение времен.");
        }
        task.setId(idCounter());
        tasks.put(task.getId(), task);
        prioritizedTasksSet.add(task);
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(idCounter());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpic().getId());
        if (epic != null) {
            try {
                isTaskCrossAnother(subtask);
            } catch (ManagerSaveException e) {
                throw new ManagerSaveException("Ошибка: пересечение времен.");
            }
            subtask.setId(idCounter());
            subtasks.put(subtask.getId(), subtask);
            epic.addSubtasksId(subtask.getId());
            setDurationStartAndEndTimeEpic(subtask.getEpic());
            prioritizedTasksSet.add(subtask);

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
        if (task != null) {
            try {
                isTaskCrossAnother(task);
            } catch (ManagerSaveException e) {
                throw new ManagerSaveException("Ошибка: пересечение времен.");
            }
                tasks.put(task.getId(), task);
                for (Task t : getPrioritizedTasks()) {
                    if (task.getId() == t.getId()) {
                        prioritizedTasksSet.remove(t);
                    }
                }
                prioritizedTasksSet.add(task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask != null) {
            try {
                isTaskCrossAnother(subtask); // Проверка перекрытия
            } catch (ManagerSaveException e) {
                throw new ManagerSaveException("Ошибка: пересечение времен.");
            }
            subtasks.put(subtask.getId(), subtask);
            Epic epic = subtask.getEpic();
            if (epic != null) {
                updateEpicStatus(epic);
                setDurationStartAndEndTimeEpic(epic);
                for (Task t : getPrioritizedTasks()) {
                    if (subtask.getId() == t.getId()) {
                        prioritizedTasksSet.remove(t);
                    }
                }
                prioritizedTasksSet.add(subtask);
            }
        }
    }

    @Override
    public void deleteTaskByIndex(int id) {
        tasks.remove(id);
        historyManager.remove(id);
        prioritizedTasksSet.remove(getTaskByIndex(id));
    }

    @Override
    public void deleteEpicByIndex(int id) {
        Epic epic = epics.remove(id);
        historyManager.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtasksIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
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
                setDurationStartAndEndTimeEpic(epic);
                historyManager.remove(subtask.getId());
                for (Task t : getPrioritizedTasks()) {
                    if (subtask.getId() == t.getId()) {
                        prioritizedTasksSet.remove(t);
                    }
                }
            }
        }
    }

    @Override
    public void deleteTasks() {
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
            //
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
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasksSet);
    }

    @Override
    public void isTaskCrossAnother(Task task) throws ManagerSaveException {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return;
        }

        if (prioritizedTasksSet.isEmpty()) {
            prioritizedTasksSet.add(task);
            return;
        }

        for (Task t : prioritizedTasksSet) {
            if (task.getEndTime().isAfter(t.getStartTime()) && task.getStartTime().isBefore(t.getEndTime())) {
                throw new ManagerSaveException("Ошибка: пересечение времен.");
            }
        }
        prioritizedTasksSet.add(task);
    }

    @Override
    public void setDurationStartAndEndTimeEpic(Epic epic) {
        List<Integer> newList = epic.getSubtasksIds();
        Duration newDuration = Duration.ZERO;
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
            newDuration = epic.getDuration();
        }
        updateEpicStatus(epic);
    }

    private int idCounter() {
        return index++;
    }
}