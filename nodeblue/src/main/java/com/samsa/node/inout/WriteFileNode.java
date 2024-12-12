package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
public class WriteFileNode extends InOutNode {
    private final String filePath;
    private final Charset encoding;

    public WriteFileNode(String id, String filePath) {
        this(id, filePath, StandardCharsets.UTF_8);
    }

    public WriteFileNode(String id, String filePath, String encodingName) {
        this(id, filePath, Charset.forName(encodingName));
    }

    public WriteFileNode(String id, String filePath, Charset encoding) {
        super(id);
        this.filePath = filePath;
        this.encoding = encoding;
    }

    @Override
    public void onMessage(Message message) {
        try {
            String content = message.getPayload() != null ? message.getPayload().toString() : "";

            Path path = Paths.get(filePath);

            StandardOpenOption[] options = Files.exists(path)
                    ? new StandardOpenOption[] {
                            StandardOpenOption.APPEND,
                            StandardOpenOption.WRITE
                    }
                    : new StandardOpenOption[] {
                            StandardOpenOption.CREATE_NEW,
                            StandardOpenOption.WRITE
                    };

            Files.writeString(path, content, encoding, options);

            log.info("Successfully wrote to file: {} (Mode: {})",
                    filePath,
                    options[0] == StandardOpenOption.APPEND ? "Append" : "Create");

            emit(message);
        } catch (IOException e) {
            handleError(e);
            log.error("Error writing to file: {} with encoding: {}", filePath, encoding.name(), e);
        }
    }
}