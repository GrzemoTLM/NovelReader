package org.example.novelreader.repository;

import org.example.novelreader.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByUserIdAndBookIdOrderByChapterIndexAscCharacterOffsetAsc(Long userId, Long bookId);

    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Bookmark> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndBookIdAndChapterIndexAndCharacterOffset(
            Long userId, Long bookId, Integer chapterIndex, Integer characterOffset);

    void deleteByUserIdAndBookId(Long userId, Long bookId);

    long countByUserIdAndBookId(Long userId, Long bookId);

    @Query("SELECT b FROM Bookmark b WHERE b.user.id = :userId AND b.book.id = :bookId " +
            "AND b.chapterIndex = :chapterIndex " +
            "ORDER BY ABS(b.characterOffset - :offset) ASC")
    List<Bookmark> findNearestInChapter(
            @Param("userId") Long userId,
            @Param("bookId") Long bookId,
            @Param("chapterIndex") Integer chapterIndex,
            @Param("offset") Integer offset);
}

