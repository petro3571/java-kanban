package HttpServerPackage;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.ManagerSaveException;
import manager.Managers;
import manager.TaskManager;
import typetask.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

class TaskHandler implements HttpHandler {
    TaskManager taskManager;
    Gson gson;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = Managers.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                handleGetTasks(exchange);
                break;
            }
            case GET_TASKS_BY_ID: {
                handleGetTaskById(exchange);
                break;
            }
            case POST_TASKS: {
                    handlePostTask(exchange);
                    break;
            }
            case DELETE_TASK : {
                handleDeleteTask(exchange);
            }
            default:
                writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        String response = taskManager.getAllTasks().stream().map(Task::toString).collect(Collectors.joining());
        writeResponse(exchange, response, 200);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи", 404);
            return;
        }
        int taskId = taskIdOpt.get();

        if (taskManager.getTaskByIndex(taskId) == null) {
            writeResponse(exchange, "Такой задачи нет", 404);
            return;
        }

        String response = taskManager.getTaskByIndex(taskId).toString();
        writeResponse(exchange, response, 200);
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if(taskIdOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи", 404);
            return;
        }
        int taskId = taskIdOpt.get();

        if (taskManager.getTaskByIndex(taskId) == null) {
            writeResponse(exchange, "Такой задачи нет", 404);
            return;
        }

        taskManager.deleteTaskByIndex(taskId);
        writeResponse(exchange, "Задача успешно удалена", 200);
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            writeResponse(exchange, "Необходимо заполнить все поля задачи", 400);
            return;
        }
        try {
            Task task = gson.fromJson(body, Task.class);
//            taskManager.isTaskCrossAnother(task);
            if (task.getId() == null) {
                taskManager.addTask(task);
                writeResponse(exchange, "Задача добавлена.", 201);
            } else {
                taskManager.updateTask(task);
                writeResponse(exchange, "Задача обновлена", 201);
            }
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Некорректный JSON", 400);
        } catch (ManagerSaveException e) {
            writeResponse(exchange, "Задача не добавлена. Ошибка: пересечение времен.", 406);
        }
    }

    private Optional<Integer> getTaskId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASKS;
            } else if( requestMethod.equals("POST")) {
                return Endpoint.POST_TASKS;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASKS_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_TASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private void writeResponse(HttpExchange exchange,
                               String responseString,
                               int responseCode) throws IOException {
        exchange.sendResponseHeaders(responseCode, 0);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseString.getBytes(StandardCharsets.UTF_8));
        }
    }

    enum Endpoint {GET_TASKS, GET_TASKS_BY_ID, POST_TASKS, DELETE_TASK, UNKNOWN}
}