package org.example;

import java.util.Deque;
import java.util.LinkedList;

public class DocumentHistory {
    private static final int DEFAULT_LIMIT = 1000;

    private final Document document;
    private final Deque<String> history;
    private final Deque<String> futureHistory;
    private final int limit;

    public DocumentHistory(Document document) {
        this(document, DEFAULT_LIMIT);
    }

    public DocumentHistory(Document document, int limit) {
        this.document = document;
        this.limit = Math.max(1, limit);
        history = new LinkedList<>();
        futureHistory = new LinkedList<>();
    }

    void executeAdd(String text) {
        pushHistory();
        document.addText(text);
        futureHistory.clear();
    }

    void executeSet(String newText) {
        pushHistory();
        document.setText(newText);
        futureHistory.clear();
    }

    void undo() {
        doIt(futureHistory, history);
    }

    void redo() {
        doIt(history, futureHistory);
    }

    boolean canUndo() {
        return !history.isEmpty();
    }

    boolean canRedo() {
        return !futureHistory.isEmpty();
    }

    private void pushHistory() {
        history.addFirst(document.getText());
        while (history.size() > limit) {
            history.removeLast();
        }
    }

    private void doIt(Deque<String> add, Deque<String> sub) {
        if (sub.isEmpty()) return;
        add.addFirst(document.getText());
        String tmp = sub.removeFirst();
        document.setText(tmp);
    }
}
