package httpserverpackage;

import com.sun.net.httpserver.HttpExchange;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;

class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = Managers.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_PRIORITIZED: {
                handleGetPrioritized(exchange);
                break;
            }
            default:
                writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        String response = taskManager.getPrioritizedTasks().toString();
        writeResponse(exchange, response, 200);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("prioritized")) {
            if (requestMethod.equals(METHOD_GET)) {
                return Endpoint.GET_PRIORITIZED;
            }
        }
        return Endpoint.UNKNOWN;
    }
}