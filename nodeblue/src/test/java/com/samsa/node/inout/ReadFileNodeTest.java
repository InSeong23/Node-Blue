package com.samsa.node.inout;

import com.samsa.core.InPort;
import com.samsa.core.Message;
import com.samsa.core.Node;
import com.samsa.core.OutPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReadFileNodeTest {

    @Mock
    private InPort inPort;

    @Mock
    private OutPort outPort;

    @Mock
    private Node node;

    private Path tempFile;
    private ReadFileNode readFileNode;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        // 임시 파일 생성
        tempFile = Files.createTempFile("test", ".txt");
        Files.writeString(tempFile, "Line 1\nLine 2\nLine 3", StandardCharsets.UTF_8);

        // ReadFileNode를 spy 객체로 만들어 handleError 메서드 모의 처리 가능
        readFileNode = spy(new ReadFileNode(inPort, outPort, tempFile.toString()));
    }

    @Test
    void testConstructorWithValidParameters() {
        assertDoesNotThrow(() -> new ReadFileNode(inPort, outPort, tempFile.toString()));
    }

    @Test
    void testConstructorWithNullFilePath() {
        assertThrows(IllegalArgumentException.class,
                () -> new ReadFileNode(inPort, outPort, null));
    }

    @Test
    void testConstructorWithEmptyFilePath() {
        assertThrows(IllegalArgumentException.class,
                () -> new ReadFileNode(inPort, outPort, ""));
    }

    @Test
    void testOnMessageReadsFileLines() throws IOException {
        readFileNode = new ReadFileNode(inPort, outPort, tempFile.toString());

        // Mockito를 사용해 메시지 방출을 검증
        doNothing().when(outPort).propagate(any(Message.class));

        // 테스트용 더미 메시지 생성
        Message dummyMessage = new Message("Test");

        readFileNode.onMessage(dummyMessage);

        // 출력 포트에 3번 메시지 방출되었는지 검증
        verify(outPort, times(3)).propagate(any(Message.class));
    }

    @Test
    void testOnMessageWithNonExistentFile() {
        ReadFileNode readFileNode = new ReadFileNode(inPort, outPort, "/non/existent/path.txt");
        
        Message dummyMessage = new Message("Test");
        
        // 노드의 상태 변화를 검증
        assertDoesNotThrow(() -> readFileNode.onMessage(dummyMessage));
        
        // 출력 포트에 메시지가 전파되지 않았는지 검증
        verify(outPort, never()).propagate(any(Message.class));
    }
}