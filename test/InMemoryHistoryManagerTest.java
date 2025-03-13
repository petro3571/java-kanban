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
        Task task1 = new Task(Status.NEW, "description1", "name1");
        taskManager.addTask(task1);

        Task newtask = taskManager.getTaskByIndex(task1.getId());
        newtask.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task1);

        Task newTask1 = taskManager.getTaskByIndex(newtask.getId());

        List<Task> newList = taskManager.getHistory();

        assertEquals(1, newList.size());
    }

    @Test
    void shouldeBCurrDelete() {
        Task task1 = new Task(Status.NEW, "description1", "task1", Duration.ofMinutes(30), LocalDateTime.now().minusHours(1));
        taskManager.addTask(task1);

        Task taskFirst = taskManager.getTaskByIndex(task1.getId());
        taskFirst.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(taskFirst);

        Task taskSecond = taskManager.getTaskByIndex(taskFirst.getId());

        Epic epic1 = new Epic("epic1", "descriptionEpic1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "descriptionSub1", "subtask1", epic1);
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.NEW, "descriptionSub2", "subtask2", epic1);
        taskManager.addSubtask(subtask2);

        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        System.out.println("Обновленный субтаск1 " + taskManager.getSubtaskByIndex(subtask1.getId()));
        taskManager.updateSubtask(subtask1);

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);
        System.out.println("Обновленный субтаск2 " + taskManager.getSubtaskByIndex(subtask2.getId()));


        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        taskManager.deleteSubtaskByIndex(subtask1.getId());

        assertEquals(2, taskManager.getHistory().size());
        assertEquals(4, taskManager.getHistory().get(1).getId());
    }
}