package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
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
            // 입력 유효성 검사
            validateInputs();

            // 파일 존재 여부 확인
            validateFileExists();

            // 파일 읽기 권한 확인
            validateFileReadable();

            // 파일 내용을 지정된 인코딩으로 읽기
            String fileContent = Files.readString(Paths.get(filePath), encoding);

            // 파일 정보를 메타데이터로 생성
            Map<String, Object> metadata = createFileMetadata();

            // 새 메시지 생성 및 출력 파이프로 전달
            Message outputMessage = new Message(fileContent, metadata);
            emit(outputMessage);

            log.info("Successfully read file: {} with encoding: {}", filePath, encoding.name());
        } catch (InvalidPathException e) {
            handlePathError(e);
        } catch (NoSuchFileException e) {
            handleNoSuchFileError(e);
        } catch (AccessDeniedException e) {
            handleAccessDeniedError(e);
        } catch (IOException e) {
            handleIOError(e);
        }
    }

    private void validateInputs() {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        if (encoding == null) {
            throw new IllegalArgumentException("Character encoding cannot be null");
        }
    }

    private void validateFileExists() throws NoSuchFileException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new NoSuchFileException("File does not exist: " + filePath);
        }
    }

    private void validateFileReadable() throws AccessDeniedException {
        Path path = Paths.get(filePath);
        if (!Files.isReadable(path)) {
            throw new AccessDeniedException("File is not readable: " + filePath);
        }
    }

    private Map<String, Object> createFileMetadata() throws IOException {
        Path path = Paths.get(filePath);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source_file", filePath);
        metadata.put("file_size", Files.size(path));
        metadata.put("file_encoding", encoding.name());
        metadata.put("last_modified", Files.getLastModifiedTime(path).toMillis());
        return metadata;
    }

    private void handlePathError(InvalidPathException e) {
        log.error("Invalid file path: {}", filePath, e);
        handleError(new IOException("Invalid file path", e));
    }

    private void handleNoSuchFileError(NoSuchFileException e) {
        log.error("File not found: {}", filePath, e);
        handleError(new IOException("File not found", e));
    }

    private void handleAccessDeniedError(AccessDeniedException e) {
        log.error("Access denied when reading file: {}", filePath, e);
        handleError(new IOException("Access denied", e));
    }

    private void handleIOError(IOException e) {
        log.error("I/O error occurred while reading file: {}", filePath, e);
        handleError(e);
    }
}