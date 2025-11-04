package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EditorControllerTest {

    @Test
    void editUndoRedoAndDirty() {
        Document doc = new Document();
        DocumentHistory hist = new DocumentHistory(doc);
        FileService fs = new FileService();
        EditorController c = new EditorController(doc, hist, fs);

        assertEquals("", c.getText());
        assertFalse(c.canUndo());
        assertFalse(c.isDirty());

        c.applyUserEdit("hello");
        assertEquals("hello", c.getText());
        assertTrue(c.canUndo());
        assertTrue(c.isDirty());

        c.undo();
        assertEquals("", c.getText());
        assertTrue(c.canRedo());

        c.redo();
        assertEquals("hello", c.getText());
    }

    @Test
    void openAndSave(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("a.txt");
        Files.writeString(f, "first");

        Document doc = new Document();
        DocumentHistory hist = new DocumentHistory(doc);
        FileService fs = new FileService();
        EditorController c = new EditorController(doc, hist, fs);

        c.open(f);
        assertEquals("first", c.getText());
        assertFalse(c.isDirty());

        c.applyUserEdit("changed");
        assertTrue(c.isDirty());

        c.save();
        assertFalse(c.isDirty());
        assertEquals("changed", Files.readString(f));

        Path f2 = tmp.resolve("b.txt");
        c.applyUserEdit("again");
        c.saveAs(f2);
        assertEquals("again", Files.readString(f2));
    }
}

