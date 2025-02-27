import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.Test;
import typetask.Epic;
import typetask.Status;
import typetask.Subtask;
import typetask.Task;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    TaskManager taskManager =  Managers.getDefaultTaskManager();

    @Test
    void addNewTask(){
        Task task = new Task(Status.NEW, "description1", "task1");
        taskManager.addTask(task);
        final Task savedTask = taskManager.getTaskByIndex(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void addToHistory() {
        Task task = new Task(Status.NEW, "description1", "task1");
        taskManager.addTask(task);
        Task taskFirst = taskManager.getTaskByIndex(task.getId());

        taskManager.getHistory().add(task);

        assertNotNull(taskManager.getHistory(), "История не пустая");
        assertEquals(1, taskManager.getHistory().size(), "История не пустая");

    }

    @Test
    void noConflictBtwGeneratedAndSpecifiedIds() {
        Task task1 = new Task(Status.NEW, "description1", "task1");
        task1.setId(1);
        taskManager.addTask(task1);

        Task task2 = new Task(Status.NEW, "description2", "task2");
        taskManager.addTask(task2);

        assertNotEquals(task1.getId(),task2.getId());
    }

    @Test
    void immutabilityWhenAddToManager() {
        Task task2 = new Task(Status.NEW, "description2", "task2");
        taskManager.addTask(task2);

        Task newtask = taskManager.getTaskByIndex(task2.getId());
        assertEquals(task2.getName(), newtask.getName());
        assertEquals(task2.getDescription(), newtask.getDescription());
        assertEquals(task2.getStatus(), newtask.getStatus());
    }

    @Test
    void changesInHistory() {
        Task task1 = new Task(Status.NEW, "description1" , "task1");
        taskManager.addTask(task1);

        Task taskFirst = taskManager.getTaskByIndex(task1.getId());
        taskFirst.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(taskFirst);

        Task taskSecond = taskManager.getTaskByIndex(taskFirst.getId());
        taskSecond.setDescription("fwdfdsf");
        taskManager.updateTask(taskSecond);

        Task task3 = taskManager.getTaskByIndex(taskSecond.getId());
        assertEquals(1, taskManager.getHistory().size());
    }

    @Test
    void differentTypesInHistory() {
        Task task1 = new Task(Status.NEW, "description1" , "task1");
        taskManager.addTask(task1);

        Task taskFirst = taskManager.getTaskByIndex(task1.getId());
        taskFirst.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(taskFirst);

        Task taskSecond = taskManager.getTaskByIndex(taskFirst.getId());

        Epic epic1 = new Epic("Epic1", "description1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "description1", "Subtask1", epic1);
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.NEW, "description2", "subtask2", epic1);
        taskManager.addSubtask(subtask2);

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        System.out.println("new Epic " + taskManager.getEpicByIndex(epic1.getId()));

        boolean isDifferentType = taskManager.getHistory().get(0).getName().equals(epic1.getName());

        assertFalse(isDifferentType);
    }

    @Test
    void shouldBeDeletedEpicInHistory() {
        Task task1 = new Task(Status.NEW, "description1" , "task1");
        taskManager.addTask(task1);

        Task taskFirst = taskManager.getTaskByIndex(task1.getId());
        taskFirst.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(taskFirst);

        Task taskSecond = taskManager.getTaskByIndex(taskFirst.getId());

        Epic epic1 = new Epic("Epic1", "description1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "description1", "Subtask1", epic1);
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.NEW, "description2", "subtask2", epic1);
        taskManager.addSubtask(subtask2);

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        System.out.println("new Epic " + taskManager.getEpicByIndex(epic1.getId()));
        taskManager.deleteEpicByIndex(epic1.getId());

        assertEquals(1, taskManager.getHistory().size());
        assertEquals(Task.class , taskManager.getHistory().get(0).getClass());
    }

    @Test
    void shouldBeDeletedSubtaskFromEpicInHistory() {
        Epic epic1 = new Epic("Новый год 2025", "Подготовка к нг");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "Выбрать и купить салют", "Салют", epic1);
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.NEW, "Пригласить близких", "Обзвонить близких", epic1);
        taskManager.addSubtask(subtask2);

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        System.out.println("Обновленный эпик " + taskManager.getSubtaskByIndex(subtask1.getId()));

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);
        System.out.println("Обновленный эпик " + taskManager.getSubtaskByIndex(subtask2.getId()));

        System.out.println("Обновленный эпик " + taskManager.getEpicByIndex(epic1.getId()));

        taskManager.deleteSubtaskByIndex(subtask1.getId());

        assertEquals(2, taskManager.getHistory().size());
    }
}