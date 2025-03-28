package httpserverpackage;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import manager.Managers;
import manager.TaskManager;
import typetask.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = Managers.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                handleGetEpics(exchange);
                break;
            }
            case GET_TASK_BY_ID: {
                handleGetEpicById(exchange);
                break;
            }
            case POST_TASK: {
                handlePostEpic(exchange);
                break;
            }
            case DELETE_TASK: {
                handleDeleteEpic(exchange);
            }
            case GET_EPIC_SUBTASKSIDS: {
                handleGetEpicsSubtasksIds(exchange);
            }
            default:
                writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        String response = taskManager.getAllEpics().stream().map(Epic::toString).collect(Collectors.joining());
        writeResponse(exchange, response, 200);
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор подзадачи", 404);
            return;
        }
        int epicId = taskIdOpt.get();

        if (taskManager.getEpicByIndex(epicId) == null) {
            writeResponse(exchange, "Такого эпика нет", 404);
            return;
        }

        String response = taskManager.getEpicByIndex(epicId).toString();
        writeResponse(exchange, response, 200);
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи", 404);
            return;
        }
        int epicId = taskIdOpt.get();

        if (taskManager.getEpicByIndex(epicId) == null) {
            writeResponse(exchange, "Такого эпика нет", 404);
            return;
        }

        taskManager.deleteEpicByIndex(epicId);
        writeResponse(exchange, "Задача успешно удалена", 200);
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            writeResponse(exchange, "Необходимо заполнить все поля задачи", 400);
            return;
        }
        try {
            Epic epic = gson.fromJson(body, Epic.class);
            taskManager.addEpic(epic);
            writeResponse(exchange, "Эпик добавлен.", 201);

        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Некорректный JSON", 400);
        }
    }

    private void handleGetEpicsSubtasksIds(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи", 404);
            return;
        }
        int epicId = taskIdOpt.get();

        if (taskManager.getEpicByIndex(epicId) == null) {
            writeResponse(exchange, "Такого эпика нет", 404);
            return;
        }

        Epic epic = taskManager.getEpicByIndex(epicId);
        String response = epic.getSubtasksIds().toString();
        writeResponse(exchange, "Задача выполнена " + response, 200);
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

        if (pathParts.length == 2 && pathParts[1].equals("epics")) {
            if (requestMethod.equals(METHOD_GET)) {
                return Endpoint.GET_TASKS;
            } else if (requestMethod.equals(METHOD_POST)) {
                return Endpoint.POST_TASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("epics")) {
            if (requestMethod.equals(METHOD_GET)) {
                return Endpoint.GET_TASK_BY_ID;
            }
            if (requestMethod.equals(METHOD_DELETE)) {
                return Endpoint.DELETE_TASK;
            }
        }
        if (pathParts.length == 4 && pathParts[1].equals("epics") && pathParts[3].equals("subtasks")) {
            if (requestMethod.equals(METHOD_GET)) {
                return Endpoint.GET_EPIC_SUBTASKSIDS;
            }
        }
        return Endpoint.UNKNOWN;
    }
}