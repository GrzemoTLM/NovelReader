package org.example.novelreader.service;


import lombok.RequiredArgsConstructor;
import org.example.novelreader.dto.ChapterDto;
import org.example.novelreader.dto.EpubDto;
import org.example.novelreader.dto.MetadataDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class EpubServiceImpl implements EpubService {


    @Override
    public EpubDto parseEpub(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            return parseEpubFromStream(in);
        }
    }


    @Override
    public EpubDto parseEpubFromFilePath(String filePath) throws IOException {
        try (InputStream in = new FileInputStream(filePath)) {
            return parseEpubFromStream(in);
        }
    }


    private EpubDto parseEpubFromStream(InputStream in) throws IOException {
        EpubReader reader = new EpubReader();
        nl.siegmann.epublib.domain.Book epub = reader.readEpub(in);

        String title = Optional.ofNullable(epub.getMetadata().getTitles()).filter(l -> !l.isEmpty()).map(l -> l.get(0)).orElse(null);
        String author = epub.getMetadata().getAuthors().stream().findFirst().map(a -> a.getFirstname() + " " + a.getLastname()).orElse(null);
        String language = epub.getMetadata().getLanguage();
        String identifier = epub.getMetadata().getIdentifiers().stream().findFirst().map(id -> id.getValue()).orElse(null);
        String description = epub.getMetadata().getDescriptions().stream().findFirst().orElse(null);


        MetadataDto meta = MetadataDto.builder()
                .title(title)
                .author(author)
                .language(language)
                .identifier(identifier)
                .description(description)
                .build();


        List<ChapterDto> chapters = new ArrayList<>();


        List<SpineReference> spine = epub.getSpine().getSpineReferences();
        int idx = 0;
        for (SpineReference ref : spine) {
            Resource res = ref.getResource();
            String href = res.getHref();
            String raw = null;
            try (InputStream ris = res.getInputStream()) {
                raw = new String(ris.readAllBytes());
            }

            Document doc = Jsoup.parse(raw);
            String text = doc.text();
            String chapterTitle = Optional.ofNullable(doc.selectFirst("title")).map(org.jsoup.nodes.Element::text).orElse("Chapter " + (idx + 1));


            ChapterDto chapter = ChapterDto.builder()
                    .index(idx)
                    .title(chapterTitle)
                    .html(raw)
                    .text(text)
                    .build();


            chapters.add(chapter);
            idx++;
        }


        return EpubDto.builder()
                .metadata(meta)
                .chapters(chapters)
                .build();
    }


    @Override
    public String generatePreview(EpubDto epubDto, int maxChars) {
        if (epubDto == null || epubDto.getChapters().isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (ChapterDto c : epubDto.getChapters()) {
            if (sb.length() >= maxChars) break;
            String chapterText = c.getText();
            if (chapterText == null) continue;
            int remaining = Math.max(0, maxChars - sb.length());
            sb.append(chapterText, 0, Math.min(remaining, chapterText.length()));
            sb.append('\n');
        }
        String preview = sb.toString();
        if (preview.length() > maxChars) preview = preview.substring(0, maxChars);
        return preview;
    }
}