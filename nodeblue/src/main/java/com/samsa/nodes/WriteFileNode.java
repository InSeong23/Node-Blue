package com.samsa.nodes;

import com.samsa.core.InOutNode;
import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
public class WriteFileNode extends InOutNode {
    private final String filePath;
    private final boolean appendMode;
    private String encoding = "utf-8";

    public WriteFileNode(String id, String filePath) {
        this(id, filePath, false);
    }

    public WriteFileNode(String id, String filePath, boolean appendMode) {
        super(id);
        this.filePath = filePath;
        this.appendMode = appendMode;
    }

    public WriteFileNode(String id, String filePath, boolean appendMode, String encoding) {
        this(id, filePath, appendMode);
        this.encoding = encoding;
    }

    @Override
    public void onMessage(Message message) {
        try {
            // Convert payload to string, handling different payload types
            String content = message.getPayload() != null ? message.getPayload().toString() : "";

            // Determine write option based on append mode
            StandardOpenOption[] options = appendMode
                    ? new StandardOpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND,
                            StandardOpenOption.WRITE }
                    : new StandardOpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE };

            // Write content to file
            Files.write(Paths.get(filePath), content.getBytes(), options);

            log.info("Successfully wrote to file: {} (Append Mode: {})", filePath, appendMode);

            // Optionally, pass the message along to next nodes if needed
            emit(message);
        } catch (IOException e) {
            // Handle any file writing errors
            handleError(e);
            log.error("Error writing to file: {}", filePath, e);
        }
    }
}