package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ReadFileNode extends InOutNode {
    private final String filePath;
    private final Charset encoding;

    public ReadFileNode(String id, String filePath) {
        this(id, filePath, StandardCharsets.UTF_8);
    }

    public ReadFileNode(String id, String filePath, String encodingName) {
        this(id, filePath, Charset.forName(encodingName));
    }

    public ReadFileNode(String id, String filePath, Charset encoding) {
        super(id);
        this.filePath = filePath;
        this.encoding = encoding;
    }

    @Override
    public void onMessage(Message message) {
        try {
            String fileContent = Files.readString(Paths.get(filePath), encoding);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source_file", filePath);
            metadata.put("file_size", Files.size(Paths.get(filePath)));
            metadata.put("file_encoding", encoding.name());

            Message outputMessage = new Message(fileContent, metadata);

            emit(outputMessage);

            log.info("Successfully read file: {} with encoding: {}", filePath, encoding.name());
        } catch (IOException e) {
            handleError(e);
            log.error("Error reading file: {} with encoding: {}", filePath, encoding.name(), e);
        }
    }
}