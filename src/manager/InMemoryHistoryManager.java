package manager;

import model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private Node head;
    private Node tail;
    private final Map<Integer, Node> nodeMap;

    private static class Node {
        private final Task data;
        private Node next;
        private Node previous;

        public Node(Node previous, Task data, Node next) {
            this.previous = previous;
            this.data = data;
            this.next = next;
        }

        public Task getData() {
            return data;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public Node getPrevious() {
            return previous;
        }

        public void setPrevious(Node previous) {
            this.previous = previous;
        }
    }

    public InMemoryHistoryManager() {
        this.nodeMap = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(getTasks());
    }

    private void linkLast(Task task) {
        if (task == null) return;

        int id = task.getId();

        if (nodeMap.containsKey(id)) {
            removeNode(nodeMap.get(id));
        }

        Node oldTail = tail;
        Node newTail = new Node(oldTail, task, null);
        tail = newTail;

        if (oldTail == null) {
            head = newTail;
        } else {
            oldTail.setNext(newTail);
        }

        nodeMap.put(id, newTail);
    }

    private void removeNode(Node node) {
        if (node == null || node.getData() == null) return;

        Node prev = node.getPrevious();
        Node next = node.getNext();

        if (prev != null) {
            prev.setNext(next);
        } else {
            head = next;
        }

        if (next != null) {
            next.setPrevious(prev);
        } else {
            tail = prev;
        }

        node.setPrevious(null);
        node.setNext(null);
    }

    private List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        Node current = head;

        while (current != null) {
            Task task = current.getData();

            if (task != null) {
                result.add(task);
            }

            current = current.getNext();
        }
        return result;
    }

    public boolean contains(int id) {
        return nodeMap.containsKey(id);
    }
}