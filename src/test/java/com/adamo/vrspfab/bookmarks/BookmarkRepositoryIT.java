package com.adamo.vrspfab.bookmarks;

import com.adamo.vrspfab.common.containers.MySqlTestBaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookmarkRepositoryIT extends MySqlTestBaseIT {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Test
    void repositoryLoads_andBasicQueriesExecuteAgainstSchema() {
        assertNotNull(bookmarkRepository);
        var page = bookmarkRepository.findAll(PageRequest.of(0, 5));
        assertNotNull(page);
        assertTrue(bookmarkRepository.findByUserIdAndReservationId(1L, 1L).isEmpty());
    }
}


