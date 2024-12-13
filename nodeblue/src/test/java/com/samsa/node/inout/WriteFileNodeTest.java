package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WriteFileNodeTest {

    private WriteFileNode writeFileNode;
    private final String testFilePath = "test_write_file.txt";

    @BeforeEach
    void setUp() throws IOException {
        // 테스트 환경 초기화
        Files.deleteIfExists(Path.of(testFilePath));
        writeFileNode = new WriteFileNode("writeNode1", testFilePath);
    }

    @Test
    void testOnMessage_Success() throws IOException {
        // Mock 메시지
        Message mockMessage = new Message("Test content", Map.of());

        // 실행
        writeFileNode.onMessage(mockMessage);

        // 파일 내용 검증
        String fileContent = Files.readString(Path.of(testFilePath), StandardCharsets.UTF_8);
        assertEquals("Test content", fileContent);
    }

    @Test
    void testOnMessage_Append() throws IOException {
        // 기존 파일에 내용 작성
        Files.writeString(Path.of(testFilePath), "Existing content\n", StandardCharsets.UTF_8);

        // Mock 메시지
        Message mockMessage = new Message("New content", Map.of());

        // 실행
        writeFileNode.onMessage(mockMessage);

        // 파일 내용 검증
        String fileContent = Files.readString(Path.of(testFilePath), StandardCharsets.UTF_8);
        assertEquals("Existing content\nNew content", fileContent);
    }

    @Test
    void testOnMessage_ErrorHandling() {
        // Mock 메시지
        Message mockMessage = new Message("Test content", Map.of());

        // 쓰기 불가능한 경로로 WriteFileNode 생성
        WriteFileNode invalidNode = new WriteFileNode("writeNode2", "/invalid_path/test.txt");

        // 실행 및 예외 검증
        assertThrows(Exception.class, () -> invalidNode.onMessage(mockMessage));
    }
}
