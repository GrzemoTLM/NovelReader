package org.example.novelreader.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class BookRequest {
    private String title;
    private String author;
    private String description;
    private MultipartFile file;
}
