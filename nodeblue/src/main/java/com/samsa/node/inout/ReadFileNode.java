package com.samsa.node.inout;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.samsa.core.InOutNode;
import com.samsa.core.InPort;
import com.samsa.core.Message;
import com.samsa.core.OutPort;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일에서 내용을 읽어 메시지로 변환하는 노드입니다.
 * 지정된 파일 경로, 인코딩을 사용하여 파일을 읽고 각 라인을 별도의 메시지로 전송합니다.
 * 
 * @author samsa
 * @since 1.0
 */
@Slf4j
public class ReadFileNode extends InOutNode {

    /** 읽을 파일의 경로 */
    private final String filePath;

    /** 파일 읽기에 사용할 문자 인코딩 */
    private final Charset charset;

    /**
     * 기본 생성자. UTF-8 인코딩을 사용하여 파일을 읽습니다.
     * 
     * @param inPort  입력 포트
     * @param outPort 출력 포트
     * @param filePath 읽을 파일의 경로
     * @throws IllegalArgumentException 파일 경로가 null이거나 비어있는 경우
     */
    public ReadFileNode(InPort inPort, OutPort outPort, String filePath) {
        this(inPort, outPort, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 파일 경로와 인코딩을 지정하는 생성자.
     * 
     * @param inPort  입력 포트
     * @param outPort 출력 포트
     * @param filePath 읽을 파일의 경로
     * @param charset 파일 읽기에 사용할 문자 인코딩
     * @throws IllegalArgumentException 파일 경로가 null이거나 비어있는 경우, 인코딩이 null인 경우
     */
    public ReadFileNode(InPort inPort, OutPort outPort, String filePath, Charset charset) {
        super(inPort, outPort);
        
        if (filePath == null || filePath.trim().isEmpty()) {
            log.error("파일 경로가 유효하지 않습니다");
            throw new IllegalArgumentException("파일 경로는 비어있을 수 없습니다");
        }
        
        if (charset == null) {
            log.error("문자 인코딩이 null입니다");
            throw new IllegalArgumentException("문자 인코딩은 null일 수 없습니다");
        }
        
        this.filePath = filePath;
        this.charset = charset;
    }

    /**
     * 노드의 메시지 처리 로직을 구현합니다.
     * 입력된 메시지에 관계없이 파일을 읽어 각 라인을 별도의 메시지로 전송합니다.
     * 
     * @param message 입력 메시지 (실제로는 사용되지 않음)
     */
    @Override
    public void onMessage(Message message) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), charset))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // 각 라인을 별도의 메시지로 전송
                Message lineMessage = new Message(line);
                emit(lineMessage);
            }
        } catch (IOException e) {
            log.error("파일 읽기 중 오류 발생. 파일: {}", filePath, e);
            handleError(e);
        }
    }
}