package manager;

import typetask.Epic;
import typetask.Subtask;
import typetask.Task;
import typetask.Status;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) {
        File tempfile = null;
        try {
            tempfile = File.createTempFile("test1","csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileBackedTaskManager newTM = new FileBackedTaskManager(tempfile.toPath());

        Task task1 = new Task(Status.NEW, "Купить продукты", "Лента");
        newTM.addTask(task1);

        Task taskFirst = newTM.getTaskByIndex(task1.getId());
        taskFirst.setStatus(Status.IN_PROGRESS);
        newTM.updateTask(taskFirst);

        Task taskSecond = newTM.getTaskByIndex(taskFirst.getId());
        taskSecond.setDescription("fwdfdsfw");
        newTM.updateTask(taskSecond);

        Task task3 = newTM.getTaskByIndex(taskSecond.getId());

        Task task2 = new Task(Status.NEW, "Полный бак аи", "Крайснефть");
        newTM.addTask(task2);

        Epic epic1 = new Epic("Новый год", "Подготовка к нг");
        newTM.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "Выбрать и купить салют", "Салют", epic1);
        newTM.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.NEW, "Пригласить близких", "Обзвонить близких", epic1);
        newTM.addSubtask(subtask2);

        Epic epic2 = new Epic("Переезд", "Подготовка к переезду");
        newTM.addEpic(epic2);

        Subtask subtask3 = new Subtask(Status.NEW, "Собрать вещи", "Упаковать вещи", epic2);
        newTM.addSubtask(subtask3);

        printAllTasks(newTM);

        subtask1.setStatus(Status.DONE);
        newTM.updateSubtask(subtask1);

        subtask2.setStatus(Status.DONE);
        newTM.updateSubtask(subtask2);

        System.out.println("Обновленный эпик " + newTM.getEpicByIndex(epic1.getId()));
        System.out.println();

        newTM.deleteTaskByIndex(task2.getId());
        newTM.deleteEpicByIndex(epic2.getId());

        System.out.println("Задачи после удаления " + newTM.getAllTasks());
        System.out.println();
        System.out.println("Эпики после удаления " + newTM.getAllEpics());
        System.out.println();

        try {
            System.out.println(Files.readString(newTM.getPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileBackedTaskManager restoreFBTM = FileBackedTaskManager.loadFromFile(tempfile.toPath());
            System.out.println(restoreFBTM.getAllTasks());
            System.out.println(restoreFBTM.getAllEpics());
            System.out.println(restoreFBTM.getAllSubtasks());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Файла нет.");
        } finally {
            tempfile.deleteOnExit();
        }
//        printPrevWorkProgram();
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }

    private static void printPrevWorkProgram() {
        TaskManager taskManager =  Managers.getDefaultTaskManager();
        Task task1 = new Task(Status.NEW, "Купить продукты", "Лента");
        taskManager.addTask(task1);

        Task taskFirst = taskManager.getTaskByIndex(task1.getId());
        taskFirst.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(taskFirst);

        Task taskSecond = taskManager.getTaskByIndex(taskFirst.getId());
        taskSecond.setDescription("fwdfdsfw");
        taskManager.updateTask(taskSecond);

        Task task3 = taskManager.getTaskByIndex(taskSecond.getId());

        Task task2 = new Task(Status.NEW, "Полный бак аи", "Крайснефть");
        taskManager.addTask(task2);

        Epic epic1 = new Epic("Новый год", "Подготовка к нг");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(Status.NEW, "Выбрать и купить салют", "Салют", epic1);
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(Status.NEW, "Пригласить близких", "Обзвонить близких", epic1);
        taskManager.addSubtask(subtask2);

        Epic epic2 = new Epic("Переезд", "Подготовка к переезду");
        taskManager.addEpic(epic2);

        Subtask subtask3 = new Subtask(Status.NEW, "Собрать вещи", "Упаковать вещи", epic2);
        taskManager.addSubtask(subtask3);

        printAllTasks(taskManager);

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        System.out.println("Обновленный эпик " + taskManager.getEpicByIndex(epic1.getId()));
        System.out.println();

        taskManager.deleteTaskByIndex(task2.getId());
        taskManager.deleteEpicByIndex(epic2.getId());

        System.out.println("Задачи после удаления " + taskManager.getAllTasks());
        System.out.println();
        System.out.println("Эпики после удаления " + taskManager.getAllEpics());
        System.out.println();
    }
}