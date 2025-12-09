package org.example.novelreader.entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "book_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"book_id","user_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookProgress {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    private Integer chapterIndex; // 0-based
    private Integer offsetInChapter; // optional character offset
}