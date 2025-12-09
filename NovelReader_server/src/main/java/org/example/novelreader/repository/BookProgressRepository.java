package org.example.novelreader.repository;

import org.example.novelreader.entity.BookProgress;
import org.example.novelreader.entity.Book;
import org.example.novelreader.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookProgressRepository extends JpaRepository<BookProgress, Long> {

    Optional<BookProgress> findByUserAndBook(User user, Book book);

}
