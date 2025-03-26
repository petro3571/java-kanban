import manager.TaskManager;
import org.junit.jupiter.api.Test;
import typetask.Epic;
import typetask.Status;
import typetask.Subtask;
import typetask.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AbstractClassTMTest<T extends TaskManager> {
    T taskManager;

    @Test
    void addNewTask() {
        Task task = new Task(Status.NEW, "description1", "task1", Duration.ofMinutes(30), LocalDateTime.now().minusDays(1));
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
        Task task = new Task(Status.NEW, "description1", "task1", Duration.ofMinutes(30), LocalDateTime.now().minusDays(1));
        taskManager.addTask(task);
        Task taskFirst = taskManager.getTaskByIndex(task.getId());

        taskManager.getHistory().add(task);

        assertNotNull(taskManager.getHistory(), "История не пустая");
        assertEquals(1, taskManager.getHistory().size(), "История не пустая");

    }

    @Test
    void noConflictBtwGeneratedAndSpecifiedIds() {
        Task task1 = new Task(Status.NEW, "description1", "task1", Duration.ofMinutes(30), LocalDateTime.now().minusDays(1));
        task1.setId(1);
        taskManager.addTask(task1);

        Task task2 = new Task(Status.NEW, "description2", "task2", Duration.ofMinutes(30), LocalDateTime.now().minusWeeks(1));
        taskManager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId());
    }

    @Test
    void immutabilityWhenAddToManager() {
        Task task2 = new Task(Status.NEW, "description2", "task2", Duration.ZERO, LocalDateTime.now());
        taskManager.addTask(task2);

        Task newtask = taskManager.getTaskByIndex(task2.getId());
        assertEquals(task2.getName(), newtask.getName());
        assertEquals(task2.getDescription(), newtask.getDescription());
        assertEquals(task2.getStatus(), newtask.getStatus());
    }

    @Test
    void changesInHistory() {
        Task task1 = new Task(Status.NEW, "description1", "task1", Duration.ofMinutes(30), LocalDateTime.now().minusDays(1));
        taskManager.addTask(task1);

        taskManager.getTaskByIndex(task1.getId());

        assertEquals(1, taskManager.getHistory().size());
    }

    @Test
    void differentTypesInHistory() {
        Task task1 = new Task(Status.NEW, "description1", "task1", Duration.ofMinutes(30), LocalDateTime.now().minusDays(1));
        taskManager.addTask(task1);

        taskManager.getTaskByIndex(task1.getId());


        Epic epic1 = new Epic("Epic1", "description1");
        taskManager.addEpic(epic1);

        taskManager.getEpicByIndex(epic1.getId()); // эпик в историю

        boolean isDifferentType = taskManager.getHistory().get(0).getName().equals(epic1.getName());

        assertFalse(isDifferentType);
    }

    @Test
    void shouldBeDeletedEpicInHistory() {
        Task task1 = new Task(Status.NEW, "description1", "task1", Duration.ofMinutes(30), LocalDateTime.now().minusDays(1));
        taskManager.addTask(task1);

        taskManager.getTaskByIndex(task1.getId());

        Epic epic1 = new Epic("Epic1", "description1");
        taskManager.addEpic(epic1);

        taskManager.getEpicByIndex(epic1.getId()); // эпик в историю
        taskManager.deleteEpicByIndex(epic1.getId());

        assertEquals(1, taskManager.getHistory().size());
        assertEquals(Task.class, taskManager.getHistory().get(0).getClass());
    }

    @Test
    void shouldBeDeletedSubtaskFromEpicInHistory() {
        Epic epic1 = new Epic("Новый год 2025", "Подготовка к нг");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "Выбрать и купить салют", "Салют", Duration.ZERO, LocalDateTime.of(2020,12,2,1,1), epic1);
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.NEW, "Пригласить близких", "Обзвонить близких",Duration.ofMinutes(1), LocalDateTime.now().minusMonths(14), epic1);
        taskManager.addSubtask(subtask2);

        Subtask subtask3 = new Subtask(Status.DONE, "Пригласить близких", "Обзвонить близких",Duration.ofMinutes(1), LocalDateTime.now().minusYears(2), epic1);

        subtask3.setId(subtask1.getId());
        taskManager.updateSubtask(subtask3);

        taskManager.getSubtaskByIndex(subtask3.getId()); // субтаск в историю

        Subtask subtask4 = new Subtask(Status.DONE, "Пригласить близких", "Обзвонить близких",Duration.ofMinutes(1), LocalDateTime.now(), epic1);

        subtask4.setId(subtask2.getId());
        taskManager.updateSubtask(subtask4);

        taskManager.getSubtaskByIndex(subtask1.getId());

        taskManager.getSubtaskByIndex(subtask2.getId()); // субтаск в историю

        taskManager.getEpicByIndex(epic1.getId()); // эпик в историю

        taskManager.deleteSubtaskByIndex(subtask3.getId());

        assertEquals(2, taskManager.getHistory().size());
    }

    @Test
    void shouldBeCurrStatusEpic() {
        Epic epic1 = new Epic("epic1", "descriptionEpic1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "descriptionSub1", "subtask1", Duration.ofMinutes(1), LocalDateTime.now().minusMonths(50), epic1);
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.NEW, "descriptionSub2", "subtask2",Duration.ofMinutes(2), LocalDateTime.now().minusMonths(49), epic1);
        taskManager.addSubtask(subtask2);

        taskManager.updateEpicStatus(epic1);

        Epic epic2 = new Epic("epic2", "descriptionEpic2");
        taskManager.addEpic(epic2);

        Subtask subtask3 = new Subtask(Status.DONE, "descriptionSub3", "subtask3", Duration.ofMinutes(1), LocalDateTime.now().minusMonths(48), epic2);
        taskManager.addSubtask(subtask3);

        Subtask subtask4 = new Subtask(Status.DONE, "descriptionSub4", "subtask4", Duration.ofMinutes(1), LocalDateTime.now().minusMonths(47), epic2);
        taskManager.addSubtask(subtask4);

        taskManager.updateEpicStatus(epic2);

        Epic epic3 = new Epic("epic3", "descriptionEpic3");
        taskManager.addEpic(epic3);

        Subtask subtask5 = new Subtask(Status.NEW, "descriptionSub5", "subtask5", Duration.ofMinutes(1), LocalDateTime.now().minusMonths(40), epic3);
        taskManager.addSubtask(subtask5);

        Subtask subtask6 = new Subtask(Status.DONE, "descriptionSub6", "subtask6", Duration.ofMinutes(1), LocalDateTime.now().minusMonths(39), epic3);
        taskManager.addSubtask(subtask6);

        taskManager.updateEpicStatus(epic3);

        Epic epic4 = new Epic("epic4", "descriptionEpic4");
        taskManager.addEpic(epic4);

        Subtask subtask7 = new Subtask(Status.IN_PROGRESS, "descriptionSub7", "subtask7", Duration.ofMinutes(1), LocalDateTime.now().minusMonths(36), epic4);
        taskManager.addSubtask(subtask7);

        Subtask subtask8 = new Subtask(Status.IN_PROGRESS, "descriptionSub8", "subtask8", Duration.ofMinutes(1), LocalDateTime.now().minusMonths(20), epic4);
        taskManager.addSubtask(subtask8);

        taskManager.updateEpicStatus(epic4);

        assertEquals(Status.NEW, epic1.getStatus());
        assertEquals(Status.DONE, epic2.getStatus());
        assertEquals(Status.IN_PROGRESS, epic3.getStatus());
        assertEquals(Status.IN_PROGRESS, epic4.getStatus());
    }

    @Test
    void shouldBeSubtasksEpic() {
        Epic epic1 = new Epic("epic1", "descriptionEpic1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "descriptionSub1", "subtask1", Duration.ZERO, LocalDateTime.now(), epic1);
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.NEW, "descriptionSub2", "subtask2",Duration.ofMinutes(1), LocalDateTime.now().minusWeeks(1), epic1);
        taskManager.addSubtask(subtask2);

        assertEquals(taskManager.getEpicByIndex(subtask1.getEpic().getId()), subtask1.getEpic());
    }
}