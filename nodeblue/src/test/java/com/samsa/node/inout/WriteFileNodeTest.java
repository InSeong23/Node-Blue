package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WriteFileNodeTest {

    private WriteFileNode writeFileNode;
    private final String testFilePath = "test_write_file.txt";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // Generate a valid UUID for the node ID
        UUID nodeId = UUID.randomUUID();

        // Clean up any existing test file
        Path fullPath = Paths.get(testFilePath);
        if (Files.exists(fullPath)) {
            Files.delete(fullPath);
        }
        writeFileNode = new WriteFileNode(nodeId.toString(), testFilePath);
    }

    @Test
    void testOnMessage_Success() throws IOException {
        // Mock message
        Message mockMessage = new Message("Test content", Map.of());

        // Execute
        writeFileNode.onMessage(mockMessage);

        // Verify file content
        String fileContent = Files.readString(Paths.get(testFilePath), StandardCharsets.UTF_8);
        assertEquals("Test content", fileContent);
    }

    @Test
    void testOnMessage_Append() throws IOException {
        // Write initial content to the file
        Files.writeString(Paths.get(testFilePath), "Existing content\n", StandardCharsets.UTF_8);

        // Mock message
        Message mockMessage = new Message("New content", Map.of());

        // Execute
        writeFileNode.onMessage(mockMessage);

        // Verify file content
        String fileContent = Files.readString(Paths.get(testFilePath), StandardCharsets.UTF_8);
        assertEquals("Existing content\nNew content", fileContent);
    }

    @Test
    void testOnMessage_NullPayload() throws IOException {
        // Mock message with null payload
        Message mockMessage = new Message(null, Map.of());

        // Execute
        writeFileNode.onMessage(mockMessage);

        // Verify empty file content
        String fileContent = Files.readString(Paths.get(testFilePath), StandardCharsets.UTF_8);
        assertEquals("", fileContent);
    }

    @Test
    void testOnMessage_InvalidPath() {
        // Attempt to create a node with an invalid file path
        assertThrows(IllegalArgumentException.class, () -> new WriteFileNode(UUID.randomUUID().toString(), null));
    }

    @Test
    void testOnMessage_WriteToProtectedDirectory() {
        // Attempt to write to a protected directory (should throw an exception)
        WriteFileNode protectedNode = new WriteFileNode(
                UUID.randomUUID().toString(),
                "/root/impossible_file.txt");

        Message mockMessage = new Message("Test content", Map.of());

        // Verify that an IOException is thrown when trying to write to a protected
        // directory
        assertThrows(IOException.class, () -> protectedNode.onMessage(mockMessage));
    }

    @Test
    void testOnMessage_CreateDirectoryIfNotExists() throws IOException {
        // Create a path in a non-existent directory
        Path newDir = tempDir.resolve("new_subdirectory");
        Path filePath = newDir.resolve("test_file.txt");

        // Create WriteFileNode with the new file path
        WriteFileNode directoryCreatingNode = new WriteFileNode(
                UUID.randomUUID().toString(),
                filePath.toString());

        // Mock message
        Message mockMessage = new Message("Test content in new directory", Map.of());

        // Execute
        directoryCreatingNode.onMessage(mockMessage);

        // Verify file was created
        assertTrue(Files.exists(filePath));
        assertEquals("Test content in new directory",
                Files.readString(filePath, StandardCharsets.UTF_8));
    }
}