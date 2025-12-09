package org.example.novelreader.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.novelreader.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    private String title;
    private String author;
    private String description;

    private String filePath; // absolute path on disk

    private LocalDateTime uploadedAt;
}
