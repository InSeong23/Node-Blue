package com.samsa.node.inout;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.samsa.core.InPort;
import com.samsa.core.Message;
import com.samsa.core.OutPort;
import com.samsa.core.InOutNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WriteFileNode extends InOutNode {
    private final Path filePath;
    private final Charset charset;
    private final boolean append;

    public WriteFileNode(InPort inPort, OutPort outPort, String filePath) {
        this(inPort, outPort, filePath, StandardCharsets.UTF_8, false);
    }

    public WriteFileNode(InPort inPort, OutPort outPort, String filePath, Charset charset, boolean append) {
        super(inPort, outPort);
        
        validateParameters(filePath, charset);
        
        this.filePath = Paths.get(filePath).toAbsolutePath().normalize();
        this.charset = charset;
        this.append = append;
    }

    private void validateParameters(String filePath, Charset charset) {
        if (filePath == null || filePath.trim().isEmpty()) {
            log.error("Invalid file path: null or empty");
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        if (charset == null) {
            log.error("Charset cannot be null");
            throw new IllegalArgumentException("Charset cannot be null");
        }
        
        Path path = Paths.get(filePath);
        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException e) {
            log.error("Cannot create parent directories for file: {}", path, e);
            throw new IllegalArgumentException("Cannot create parent directories", e);
        }
    }

    @Override
    public void onMessage(Message message) {
        Objects.requireNonNull(message, "Input message cannot be null");
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath.toFile(), append), charset))) {
            
            String payload = String.valueOf(message.getPayload());
            writer.write(payload);
            writer.newLine();
            writer.flush();  // 명시적으로 버퍼 flush

            emit(message);
        } catch (IOException e) {
            log.error("Error writing to file: {}", filePath, e);
            handleError(e);
        }
    }
}