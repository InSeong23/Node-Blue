package com.samsa.nodes;

import com.samsa.core.InOutNode;
import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ReadFileNode extends InOutNode {
    private final String filePath;
    private String encoding = "utf-8";

    public ReadFileNode(String id, String filePath) {
        super(id);
        this.filePath = filePath;
    }

    public ReadFileNode(String id, String filePath, String encoding) {
        this(id, filePath);
        this.encoding = encoding;
    }

    @Override
    public void onMessage(Message message) {
        try {
            // Read the entire file content
            String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));

            // Create metadata to include file information
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source_file", filePath);
            metadata.put("file_size", Files.size(Paths.get(filePath)));

            // Create a new message with file content
            Message outputMessage = new Message(fileContent, metadata);

            // Emit the message to output pipes
            emit(outputMessage);

            log.info("Successfully read file: {}", filePath);
        } catch (IOException e) {
            // Handle any file reading errors
            handleError(e);
            log.error("Error reading file: {}", filePath, e);
        }
    }
}