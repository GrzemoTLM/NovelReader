package org.example.novelreader.repository;

import org.example.novelreader.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByOwnerId(Long userId);
}
