import httpserverpackage.HttpTaskServer;
import com.google.gson.Gson;
import manager.TaskManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import typetask.Epic;
import typetask.Status;
import typetask.Subtask;
import typetask.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
    HttpTaskServer taskServer = new HttpTaskServer();
    TaskManager manager = taskServer.getTaskManager();
    Gson gson = taskServer.getGson();

    public HttpTaskManagerTasksTest() throws IOException, IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testGetTask() throws IOException, InterruptedException {
        Task task = new Task(Status.NEW, "Testing task 2",
                "Test 2", Duration.ofMinutes(5), LocalDateTime.now().minusYears(30));
        manager.addTask(task);
        String taskJson = gson.toJson(task);
        taskServer.start();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task(Status.NEW, "Test task", "Test", Duration.ofMinutes(5), LocalDateTime.now().minusDays(1));
        String taskJson = gson.toJson(task);
        System.out.println("Sending JSON: " + taskJson);


        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(10000))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json; charset=utf-8")
//                    .header("Accept", "application/json")
                .build();

        taskServer.start();
        Thread.sleep(500);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 406) {
            System.err.println("Сервер не принимает JSON! Ответ: " + response.body());
            fail("Сервер вернул 406 Not Acceptable. Проверьте заголовки и формат данных.");
        }

        assertEquals(201, response.statusCode(), "Ожидался статус 201 OK");

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size(), "Задача не добавилась");
    }

    @Test
    public void testAddSubTask() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1Dis", "epic1");
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask(Status.NEW, "Выбрать и купить салют", "Салют", Duration.ofMinutes(40), LocalDateTime.now().minusMonths(1), epic1);
        String taskJson = gson.toJson(subtask1);
        System.out.println("Sending JSON: " + taskJson);


        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(10000))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json; charset=utf-8")
//                    .header("Accept", "application/json")
                .build();

        taskServer.start();
        Thread.sleep(500);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 406) {
            System.err.println("Сервер не принимает JSON! Ответ: " + response.body());
            fail("Сервер вернул 406 Not Acceptable. Проверьте заголовки и формат данных.");
        }

        assertEquals(201, response.statusCode(), "Ожидался статус 201 OK");

        List<Subtask> subtasks = manager.getAllSubtasks();
        assertEquals(1, subtasks.size(), "Задача не добавилась");
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1Dis", "epic1");
        String taskJson = gson.toJson(epic1);
        System.out.println("Sending JSON: " + taskJson);

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(10000))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json; charset=utf-8")
//                    .header("Accept", "application/json")
                .build();

        taskServer.start();
        Thread.sleep(500);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 406) {
            System.err.println("Сервер не принимает JSON! Ответ: " + response.body());
            fail("Сервер вернул 406 Not Acceptable. Проверьте заголовки и формат данных.");
        }

        assertEquals(201, response.statusCode(), "Ожидался статус 201 OK");

        List<Epic> epics = manager.getAllEpics();
        assertEquals(1, epics.size(), "Задача не добавилась");
    }

    @Test
    public void testGetSubtask() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1Dis", "epic1");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "subtask1Dis", "subtask1", Duration.ofMinutes(40), LocalDateTime.now().minusMonths(1), epic1);
        manager.addSubtask(subtask1);

        String taskJson = gson.toJson(subtask1);
        taskServer.start();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> SubtasksFromManager = manager.getAllSubtasks();

        assertNotNull(SubtasksFromManager, "Задачи не возвращаются");
        assertEquals(1, SubtasksFromManager.size(), "Некорректное количество задач");
        assertEquals("subtask1", SubtasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetEpic() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1Dis", "epic1");
        manager.addEpic(epic1);

        taskServer.start();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> EpicsFromManager = manager.getAllEpics();

        assertNotNull(EpicsFromManager, "Задачи не возвращаются");
        assertEquals(1, EpicsFromManager.size(), "Некорректное количество задач");
        assertEquals("epic1", EpicsFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetTaskByIndex() throws IOException, InterruptedException {
        Task task = new Task(Status.NEW, "task1Dis",
                "task1", Duration.ofMinutes(5), LocalDateTime.now().minusYears(30));
        manager.addTask(task);
        String taskJson = gson.toJson(task);
        System.out.println("Sending JSON: " + taskJson);
        taskServer.start();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("task1", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetSubtaskByIndex() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1Dis", "epic1");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "subtask1Dis", "subtask1", Duration.ofMinutes(40), LocalDateTime.now().minusMonths(1), epic1);
        manager.addSubtask(subtask1);

        taskServer.start();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> SubtasksFromManager = manager.getAllSubtasks();

        assertNotNull(SubtasksFromManager, "Задачи не возвращаются");
        assertEquals(1, SubtasksFromManager.size(), "Некорректное количество задач");
        assertEquals("subtask1", SubtasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetEpicSubtasksIds() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1Dis", "epic1");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "subtask1Dis", "subtask1", Duration.ofMinutes(40), LocalDateTime.now().minusMonths(1), epic1);
        manager.addSubtask(subtask1);

        taskServer.start();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1Dis", "epic1");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "subtask1Dis", "subtask1", Duration.ofMinutes(40), LocalDateTime.now().minusMonths(1), epic1);
        manager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.DONE, "newSubtask1Dis", "newSubtask1", Duration.ofMinutes(30), LocalDateTime.now().minusHours(1), epic1);
        subtask2.setId(subtask1.getId());
        manager.updateSubtask(subtask2);

        taskServer.start();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> historyFromManager = taskServer.getTaskManager().getHistory();
        assertEquals(2, historyFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testGetPrioritized() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1Dis", "epic1");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "subtask1Dis", "subtask1", Duration.ofMinutes(40), LocalDateTime.now().minusMonths(1), epic1);
        manager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.DONE, "newSubtask1Dis", "newSubtask1", Duration.ofMinutes(30), LocalDateTime.now().minusHours(1), epic1);
        subtask2.setId(subtask1.getId());
        manager.updateSubtask(subtask2);

        taskServer.start();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritizedFromManager = taskServer.getTaskManager().getPrioritizedTasks();
        assertEquals(1, prioritizedFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task(Status.NEW, "Testing task 2",
                "Test 2", Duration.ofMinutes(5), LocalDateTime.now().minusYears(30));
        manager.addTask(task);
        taskServer.start();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().header("Accept", "application/json").build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertEquals(0, tasksFromManager.size(), "Задача не удалена");
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1Dis", "epic1");
        manager.addEpic(epic1);

        taskServer.start();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().header("Accept", "application/json").build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> EpicsFromManager = manager.getAllEpics();

        assertEquals(0, EpicsFromManager.size(), "Эпик не удален");
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic1Dis", "epic1");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "subtask1Dis", "subtask1", Duration.ofMinutes(40), LocalDateTime.now().minusMonths(1), epic1);
        manager.addSubtask(subtask1);

        taskServer.start();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().header("Accept", "application/json").build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> SubtasksFromManager = manager.getAllSubtasks();

        assertEquals(0, SubtasksFromManager.size(), "Подзадача не удалена");
    }
}