package org.example.novelreader.service;


import org.example.novelreader.dto.EpubDto;
import org.example.novelreader.dto.ChapterDto;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;


public interface EpubService {
    EpubDto parseEpub(MultipartFile file) throws IOException;
    EpubDto parseEpubFromFilePath(String filePath) throws IOException;
    String generatePreview(EpubDto epubDto, int maxChars);
}