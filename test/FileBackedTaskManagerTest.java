import manager.FileBackedTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import typetask.Epic;
import typetask.Status;
import typetask.Subtask;
import typetask.Task;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest extends AbstractClassTMTest {
        File tempFile;

        {
            try {
                tempFile = File.createTempFile("test2", "csv");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    @BeforeEach
    public void beforeEach() {
            taskManager = new FileBackedTaskManager(tempFile.toPath());
    }

        @Test
        void shouldBeCurrCountAfterAddTask() throws IOException {
            Task task1 = new Task(Status.NEW, "Купить продукты", "Лента");
            taskManager.addTask(task1);

            Task task2 = new Task(Status.NEW, "Полный бак аи", "Крайснефть");
            taskManager.addTask(task2);

            Epic epic1 = new Epic("Новый год", "Подготовка к нг");
            taskManager.addEpic(epic1);

            Subtask subtask1 = new Subtask(Status.NEW, "Выбрать и купить салют", "Салют", epic1);
            taskManager.addSubtask(subtask1);

            Subtask subtask2 = new Subtask(Status.NEW, "Пригласить близких", "Обзвонить близких", epic1);
            taskManager.addSubtask(subtask2);

            FileBackedTaskManager restoreFBTM = FileBackedTaskManager.loadFromFile(tempFile.toPath());

            Task newTask1 = new Task(Status.NEW, "new1", "newname1");
            restoreFBTM.addTask(newTask1);
            int indexOfRestoreTaskManager = restoreFBTM.getAllTasks().size() + restoreFBTM.getAllEpics().size() + restoreFBTM.getAllSubtasks().size();
            assertEquals(6, indexOfRestoreTaskManager);
        }
}