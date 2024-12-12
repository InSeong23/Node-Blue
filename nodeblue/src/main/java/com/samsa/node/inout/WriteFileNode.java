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

/**
 * 메시지를 파일에 작성하는 노드 클래스
 * 입력된 메시지의 내용을 지정된 파일에 작성
 */
@Slf4j
public class WriteFileNode extends InOutNode {
    /** 파일을 작성할 경로 */
    private final String filePath;

    /** 파일 작성에 사용할 문자 인코딩 */
    private final Charset encoding;

    /**
     * 기본 UTF-8 인코딩으로 WriteFileNode 생성자
     * 
     * @param id       노드의 고유 식별자
     * @param filePath 파일을 작성할 경로
     */
    public WriteFileNode(String id, String filePath) {
        this(id, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 문자열 인코딩 이름으로 WriteFileNode 생성자
     * 
     * @param id           노드의 고유 식별자
     * @param filePath     파일을 작성할 경로
     * @param encodingName 파일 작성에 사용할 문자 인코딩 이름
     */
    public WriteFileNode(String id, String filePath, String encodingName) {
        this(id, filePath, Charset.forName(encodingName));
    }

    /**
     * Charset 객체로 WriteFileNode 생성자
     * 
     * @param id       노드의 고유 식별자
     * @param filePath 파일을 작성할 경로
     * @param encoding 파일 작성에 사용할 문자 인코딩
     */
    public WriteFileNode(String id, String filePath, Charset encoding) {
        super(id);
        this.filePath = filePath;
        this.encoding = encoding;
    }

    /**
     * 입력 메시지의 페이로드를 파일에 작성
     * 파일이 존재하면 추가, 존재하지 않으면 새로 생성
     * 
     * @param message 파일에 작성할 내용을 포함하는 메시지
     */
    @Override
    public void onMessage(Message message) {
        try {
            // 메시지 페이로드를 문자열로 변환
            String content = message.getPayload() != null
                    ? message.getPayload().toString()
                    : "";

            // 파일 경로 생성
            Path path = Paths.get(filePath);

            // 파일 존재 여부에 따라 쓰기 옵션 결정
            StandardOpenOption[] options = Files.exists(path)
                    ? new StandardOpenOption[] {
                            StandardOpenOption.APPEND,
                            StandardOpenOption.WRITE
                    }
                    : new StandardOpenOption[] {
                            StandardOpenOption.CREATE_NEW,
                            StandardOpenOption.WRITE
                    };

            // 파일에 내용 작성
            Files.writeString(path, content, encoding, options);

            // 로깅
            log.info("Successfully wrote to file: {} (Mode: {})",
                    filePath,
                    options[0] == StandardOpenOption.APPEND ? "Append" : "Create");

            // 메시지를 다음 노드로 전달
            emit(message);
        } catch (IOException e) {
            // 파일 작성 중 오류 처리
            handleError(e);
            log.error("Error writing to file: {} with encoding: {}", filePath, encoding.name(), e);
        }
    }
}