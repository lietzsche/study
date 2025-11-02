package org.example;

import java.util.Deque;
import java.util.LinkedList;

public class DocumentHistory {
    private final Document document;
    private final Deque<String> history;
    private final Deque<String> futureHistory;

    public DocumentHistory(Document document) {
        this.document = document;
        history = new LinkedList<>();
        futureHistory = new LinkedList<>();
    }

    void executeAdd(String text) {
        history.addFirst(document.getText());
        document.addText(text);
        futureHistory.clear();
    }

    void undo() {
        doIt(futureHistory, history);
    }

    void redo() {
        doIt(history, futureHistory);
    }

    private void doIt(Deque<String> add, Deque<String> sub) {
        if (sub.isEmpty()) return;
        add.addFirst(document.getText());
        String tmp = sub.removeFirst();
        document.clear();
        document.addText(tmp);
    }
}
