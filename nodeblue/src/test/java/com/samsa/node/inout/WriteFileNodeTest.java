package com.samsa.node.inout;

import com.samsa.core.InPort;
import com.samsa.core.Message;
import com.samsa.core.Node;
import com.samsa.core.OutPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WriteFileNodeTest {

    @Mock
    private InPort inPort;

    @Mock
    private OutPort outPort;

    @Mock
    private Node node;

    private Path tempFile;
    private WriteFileNode writeFileNode;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        // 임시 파일 생성
        tempFile = Files.createTempFile("test", ".txt");
    }

    @Test
    void testConstructorWithValidParameters() {
        assertDoesNotThrow(() -> new WriteFileNode(inPort, outPort, tempFile.toString()));
    }

    @Test
    void testConstructorWithNullFilePath() {
        assertThrows(IllegalArgumentException.class,
                () -> new WriteFileNode(inPort, outPort, null));
    }

    @Test
    void testConstructorWithEmptyFilePath() {
        assertThrows(IllegalArgumentException.class,
                () -> new WriteFileNode(inPort, outPort, ""));
    }

    @Test
    void testOnMessageWritesMessageToFile() throws IOException {
        writeFileNode = new WriteFileNode(inPort, outPort, tempFile.toString());

        // Mockito를 사용해 메시지 방출을 모킹
        doNothing().when(outPort).propagate(any(Message.class));

        Message testMessage = new Message("Test Content");
        writeFileNode.onMessage(testMessage);

        // 파일에 내용 쓰였는지 검증
        List<String> fileContent = Files.readAllLines(tempFile, StandardCharsets.UTF_8);
        assertEquals(1, fileContent.size());
        assertEquals("Test Content", fileContent.get(0));
    }

    @Test
    void testOnMessageWithAppendMode() throws IOException {
        writeFileNode = new WriteFileNode(inPort, outPort, tempFile.toString(),
                StandardCharsets.UTF_8, true);

        doNothing().when(outPort).propagate(any(Message.class));

        Message message1 = new Message("First Line");
        Message message2 = new Message("Second Line");

        writeFileNode.onMessage(message1);
        writeFileNode.onMessage(message2);

        // 두 줄이 파일에 추가되었는지 검증
        List<String> fileContent = Files.readAllLines(tempFile, StandardCharsets.UTF_8);
        assertEquals(2, fileContent.size());
        assertEquals("First Line", fileContent.get(0));
        assertEquals("Second Line", fileContent.get(1));
    }

    @Test
    void testOnMessageWithNullMessage() {
        writeFileNode = new WriteFileNode(inPort, outPort, tempFile.toString());

        assertThrows(NullPointerException.class, () -> writeFileNode.onMessage(null));
    }
}