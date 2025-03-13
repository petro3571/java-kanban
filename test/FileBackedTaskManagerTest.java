import exceptions.ManagerSaveException;
import manager.FileBackedTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import typetask.Epic;
import typetask.Status;
import typetask.Subtask;
import typetask.Task;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileBackedTaskManagerTest {
        File tempFile;

        {
            try {
                tempFile = File.createTempFile("test2", "csv");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
            taskManager = new FileBackedTaskManager(tempFile.toPath());
    }

        @Test
        void shouldBeCurrCountAfterAddTask() throws IOException {
            Task task1 = new Task(Status.NEW, "description1", "task1", Duration.ofMinutes(10), LocalDateTime.now().minusHours(1));
            taskManager.addTask(task1);

            Task task2 = new Task(Status.NEW, "description2", "task2", Duration.ofMinutes(1), LocalDateTime.now().minusHours(3));
            taskManager.addTask(task2);

            Epic epic1 = new Epic("epic1", "descriptionEpic1");
            taskManager.addEpic(epic1);

            Subtask subtask1 = new Subtask(Status.NEW, "descriptionSub1", "subtask1",Duration.ofMinutes(20), LocalDateTime.now().minusHours(2),epic1);
            taskManager.addSubtask(subtask1);

            Subtask subtask2 = new Subtask(Status.NEW, "descriptionSub2", "subtask2",Duration.ofMinutes(40), LocalDateTime.now().minusHours(3), epic1);
            taskManager.addSubtask(subtask2);

            FileBackedTaskManager restoreFBTM = FileBackedTaskManager.loadFromFile(tempFile.toPath());

            Task newTask1 = new Task(Status.NEW, "new1", "newname1", Duration.ofMinutes(10), LocalDateTime.now().minusHours(4));
            restoreFBTM.addTask(newTask1);
            int indexOfRestoreTaskManager = restoreFBTM.getAllTasks().size() + restoreFBTM.getAllEpics().size() + restoreFBTM.getAllSubtasks().size();
            assertEquals(6, indexOfRestoreTaskManager);
        }

        @Test
        void shouldBeExceptionWithAddTaskWithSameTime() {
            Task task1 = new Task(Status.NEW, "description1", "task1", Duration.ofMinutes(30), LocalDateTime.now().minusHours(1));
            taskManager.addTask(task1);

            Task task2 = new Task(Status.NEW, "description2", "task2", Duration.ofMinutes(30), LocalDateTime.now().minusHours(1));

            assertThrows(ManagerSaveException.class, () -> taskManager.addTask(task2), "Пересечение времен");
        }
}