package manager;

import typetask.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node<Task>> newHashMap = new HashMap<>();

    private Node<Task> head;

    private Node<Task> tail;

    @Override
    public void addTask(Task task) {
        if (task != null) {
            remove(task.getId());
            linkLast(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        removeNode(newHashMap.get(id));
    }

    public void linkLast(Task task) {
        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<Task>(oldTail, task, null);
        tail = newNode;
        newHashMap.put(task.getId(), newNode);
        if (oldTail == null)
            head = newNode;
        else
            oldTail.next = newNode;
    }

    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> firstNode = head;
        while (firstNode != null) {
            tasks.add(firstNode.data);
            firstNode = firstNode.next;
        }
        return tasks;
    }

    public void removeNode(Node<Task> node) {
        if (node != null) {
            Node<Task> next = node.next;
            Node<Task> prev = node.prev;

            if (head == node && tail == node) {
                head = null;
                tail = null;
            } else if (head == node && !(tail == node)) {
                head = next;
                head.prev = null;
            } else if (!(head == node) && tail == node) {
                tail = prev;
                tail.next = null;
            } else {
                prev.next = next;
                next.prev = prev;
            }
        }
    }
}