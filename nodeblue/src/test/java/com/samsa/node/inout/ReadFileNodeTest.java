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

class ReadFileNodeTest {

    private ReadFileNode readFileNode;
    private final String testFilePath = "test_read_file.txt";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // Create test file
        Files.writeString(Paths.get(testFilePath), "Test content", StandardCharsets.UTF_8);

        // Use UUID format for id
        String uuid = UUID.randomUUID().toString();
        readFileNode = new ReadFileNode(uuid, testFilePath);
    }

    @Test
    void testOnMessage_Success() {
        // Mock message
        Message mockMessage = new Message("Initial", Map.of());

        // Execute and verify output message
        ReadFileNode spyNode = new ReadFileNode(
                UUID.randomUUID().toString(),
                testFilePath) {
            @Override
            public void emit(Message message) {
                try {
                    assertEquals("Test content", message.getPayload());
                    assertEquals(testFilePath, message.getMetadata().get("source_file"));

                    // Safely get file size with exception handling
                    long expectedFileSize = Files.size(Paths.get(testFilePath));
                    assertEquals(expectedFileSize, message.getMetadata().get("file_size"));

                    assertEquals(StandardCharsets.UTF_8.name(), message.getMetadata().get("file_encoding"));
                    assertNotNull(message.getMetadata().get("last_modified"));
                } catch (IOException e) {
                    fail("Unexpected IOException: " + e.getMessage());
                }
            }
        };

        spyNode.onMessage(mockMessage);
    }

    @Test
    void testOnMessage_FileNotFound() {
        // Create a non-existent file path
        String nonexistentFile = "nonexistent_file.txt";

        // Use UUID format for id
        String uuid = UUID.randomUUID().toString();

        // Create ReadFileNode with non-existent file
        ReadFileNode invalidNode = new ReadFileNode(uuid, nonexistentFile);

        // Mock message
        Message mockMessage = new Message("Initial", Map.of());

        // Verify IOException is thrown
        assertThrows(IOException.class, () -> invalidNode.onMessage(mockMessage));
    }

    @Test
    void testOnMessage_InvalidPath() {
        // Verify that null or empty path throws an exception
        assertThrows(IllegalArgumentException.class, () -> new ReadFileNode(UUID.randomUUID().toString(), null));

        assertThrows(IllegalArgumentException.class, () -> new ReadFileNode(UUID.randomUUID().toString(), ""));
    }

    @Test
    void testOnMessage_UnreadableFile() throws IOException {
        // Create a file and make it unreadable
        Path unreadableFile = tempDir.resolve("unreadable_file.txt");
        Files.writeString(unreadableFile, "Unreadable content");

        // Remove read permissions
        unreadableFile.toFile().setReadable(false);

        // Create ReadFileNode with unreadable file
        ReadFileNode unreadableNode = new ReadFileNode(
                UUID.randomUUID().toString(),
                unreadableFile.toString());

        // Mock message
        Message mockMessage = new Message("Initial", Map.of());

        // Verify IOException is thrown
        assertThrows(IOException.class, () -> unreadableNode.onMessage(mockMessage));
    }

    @Test
    void testOnMessage_LargeFile() throws IOException {
        // Create a large test file
        Path largeFile = tempDir.resolve("large_file.txt");
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 100_000; i++) {
            largeContent.append("Large file content line ").append(i).append("\n");
        }

        Files.writeString(largeFile, largeContent.toString());

        // Create ReadFileNode with large file
        ReadFileNode largeFileNode = new ReadFileNode(
                UUID.randomUUID().toString(),
                largeFile.toString());

        // Mock message
        Message mockMessage = new Message("Initial", Map.of());

        // Execute and verify
        largeFileNode.onMessage(mockMessage);
    }
}