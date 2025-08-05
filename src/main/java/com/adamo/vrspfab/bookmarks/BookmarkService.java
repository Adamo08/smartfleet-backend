package com.adamo.vrspfab.bookmarks;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.reservations.ReservationMapper;
import com.adamo.vrspfab.reservations.ReservationService;
import com.adamo.vrspfab.users.UserMapper;
import com.adamo.vrspfab.users.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkMapper bookmarkMapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    @Transactional
    public BookmarkDto createBookmark(BookmarkDto bookmarkDto) {
        var user = userService.getUser(bookmarkDto.getUserId());
        var reservation = reservationService.getReservationById(bookmarkDto.getReservationId());

        Bookmark bookmark = bookmarkMapper.toEntity(bookmarkDto);
        bookmark.setUser(userMapper.toEntity(user));
        bookmark.setReservation(reservationMapper.toEntity(reservation));
        return bookmarkMapper.toDto(bookmarkRepository.save(bookmark));
    }

    @Transactional(readOnly = true)
    public BookmarkDto getBookmarkById(Long id) {
        var bookmark = bookmarkRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));
        return bookmarkMapper.toDto(bookmark);
    }

    @Transactional
    public void deleteBookmark(Long id) {
        Bookmark bookmark = bookmarkRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));
        bookmarkRepository.delete(bookmark);
    }

    @Transactional(readOnly = true)
    public List<BookmarkDto> getAllBookmarks(int page, int size, Long userId) {
        List<Bookmark> bookmarks = bookmarkRepository.findAllWithUserAndReservation();
        if (userId != null) {
            bookmarks = bookmarkRepository.findByUserId(userId);
        }
        return bookmarks.stream()
                .skip((long) page * size)
                .limit(size)
                .map(bookmarkMapper::toDto)
                .collect(Collectors.toList());
    }
}