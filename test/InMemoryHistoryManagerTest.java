import manager.HistoryManager;
import manager.Managers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import typetask.Epic;
import typetask.Status;
import typetask.Subtask;
import typetask.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest extends AbstractClassTMTest {
    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefaultTaskManager();
        HistoryManager newHistoryManager = Managers.getDefaultHistoryManager();
    }

    @Test
    void shouldBeEmptyHistory() {
        List<Task> newList = taskManager.getHistory();

        assertTrue(newList.isEmpty());
    }

    @Test
    void shouldBeNotDoubleTaskInHistory() {
        Task task1 = new Task(Status.NEW, "description1", "name1", Duration.ofMinutes(1), LocalDateTime.now().minusWeeks(1));
        taskManager.addTask(task1);

        Task newTask = new Task(Status.NEW, "description1", "name1", Duration.ofMinutes(1), LocalDateTime.now().minusWeeks(2));

        newTask.setId(task1.getId());
        taskManager.updateTask(newTask);

        taskManager.getTaskByIndex(newTask.getId());

        List<Task> newList = taskManager.getHistory();

        assertEquals(1, newList.size());
    }

    @Test
    void shouldeBCurrDelete() {
        Task task1 = new Task(Status.NEW, "description1", "name1", Duration.ofMinutes(1), LocalDateTime.now().minusWeeks(1));
        taskManager.addTask(task1);

        Task newTask = new Task(Status.NEW, "description1", "name1", Duration.ofMinutes(1), LocalDateTime.now().minusWeeks(2));

        newTask.setId(task1.getId());
        taskManager.updateTask(newTask);

        taskManager.getTaskByIndex(newTask.getId());

        Epic epic1 = new Epic("epic1", "descriptionEpic1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "descriptionSub1", "subtask1",Duration.ofMinutes(1), LocalDateTime.now().minusYears(1), epic1);
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.NEW, "descriptionSub2", "subtask2",Duration.ofMinutes(2), LocalDateTime.now().minusMonths(5), epic1);
        taskManager.addSubtask(subtask2);

        taskManager.deleteSubtaskByIndex(subtask1.getId());

        assertEquals(3, taskManager.getHistory().size());
        assertEquals(2, taskManager.getHistory().get(1).getId());
    }
}