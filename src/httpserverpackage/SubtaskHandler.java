package httpserverpackage;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import exceptions.ManagerSaveException;
import manager.Managers;
import manager.TaskManager;
import typetask.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = Managers.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                handleGetSubTasks(exchange);
                break;
            }
            case GET_TASK_BY_ID: {
                handleGetSubTaskById(exchange);
                break;
            }
            case POST_TASK: {
                handlePostSubTask(exchange);
                break;
            }
            case DELETE_TASK: {
                handleDeleteSubtask(exchange);
            }
            default:
                writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handleGetSubTasks(HttpExchange exchange) throws IOException {
        String response = taskManager.getAllSubtasks().stream().map(Subtask::toString).collect(Collectors.joining());
        writeResponse(exchange, response, 200);
    }

    private void handleGetSubTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор подзадачи", 404);
            return;
        }
        int subtaskId = taskIdOpt.get();

        if (taskManager.getSubtaskByIndex(subtaskId) == null) {
            writeResponse(exchange, "Такой подзадачи нет", 404);
            return;
        }

        String response = taskManager.getSubtaskByIndex(subtaskId).toString();
        writeResponse(exchange, response, 200);
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи", 404);
            return;
        }
        int subtaskId = taskIdOpt.get();

        if (taskManager.getSubtaskByIndex(subtaskId) == null) {
            writeResponse(exchange, "Такой задачи нет", 404);
            return;
        }

        taskManager.deleteSubtaskByIndex(subtaskId);
        writeResponse(exchange, "Задача успешно удалена", 200);
    }

    private void handlePostSubTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            writeResponse(exchange, "Необходимо заполнить все поля задачи", 400);
            return;
        }
        try {
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (subtask.getId() == null) {
                taskManager.addSubtask(subtask);
                writeResponse(exchange, "ПодЗадача добавлена.", 201);
            } else {
                taskManager.updateSubtask(subtask);
                writeResponse(exchange, "ПодЗадача обновлена", 201);
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

        if (pathParts.length == 2 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals(METHOD_GET)) {
                return Endpoint.GET_TASKS;
            } else if (requestMethod.equals(METHOD_POST)) {
                return Endpoint.POST_TASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals(METHOD_GET)) {
                return Endpoint.GET_TASK_BY_ID;
            }
            if (requestMethod.equals(METHOD_DELETE)) {
                return Endpoint.DELETE_TASK;
            }
        }
        return Endpoint.UNKNOWN;
    }
}