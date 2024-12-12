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

/**
 * 파일을 읽어 메시지로 변환하는 노드 클래스
 * 지정된 파일 경로에서 파일을 읽고 내용을 메시지 페이로드로 변환
 */
@Slf4j
public class ReadFileNode extends InOutNode {
    /** 읽을 파일의 경로 */
    private final String filePath;

    /** 파일 읽기에 사용할 문자 인코딩 */
    private final Charset encoding;

    /**
     * 기본 UTF-8 인코딩으로 ReadFileNode 생성자
     * 
     * @param id       노드의 고유 식별자
     * @param filePath 읽을 파일의 경로
     */
    public ReadFileNode(String id, String filePath) {
        this(id, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 문자열 인코딩 이름으로 ReadFileNode 생성자
     * 
     * @param id           노드의 고유 식별자
     * @param filePath     읽을 파일의 경로
     * @param encodingName 파일 읽기에 사용할 문자 인코딩 이름
     */
    public ReadFileNode(String id, String filePath, String encodingName) {
        this(id, filePath, Charset.forName(encodingName));
    }

    /**
     * Charset 객체로 ReadFileNode 생성자
     * 
     * @param id       노드의 고유 식별자
     * @param filePath 읽을 파일의 경로
     * @param encoding 파일 읽기에 사용할 문자 인코딩
     */
    public ReadFileNode(String id, String filePath, Charset encoding) {
        super(id);
        this.filePath = filePath;
        this.encoding = encoding;
    }

    /**
     * 입력 메시지에 대응하여 파일을 읽고 새 메시지 생성
     * 파일 내용을 페이로드로, 파일 정보를 메타데이터로 포함
     * 
     * @param message 입력 메시지 (사용되지 않음)
     */
    @Override
    public void onMessage(Message message) {
        try {
            // 파일 내용을 지정된 인코딩으로 읽기
            String fileContent = Files.readString(Paths.get(filePath), encoding);

            // 파일 정보를 메타데이터로 생성
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source_f`ile", filePath);
            metadata.put("file_size", Files.size(Paths.get(filePath)));
            metadata.put("file_encoding", encoding.name());

            // 새 메시지 생성 및 출력 파이프로 전달
            Message outputMessage = new Message(fileContent, metadata);
            emit(outputMessage);

            log.info("Successfully read file: {} with encoding: {}", filePath, encoding.name());
        } catch (IOException e) {
            // 파일 읽기 중 오류 처리
            handleError(e);
            log.error("Error reading file: {} with encoding: {}", filePath, encoding.name(), e);
        }
    }
}