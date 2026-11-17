package com.library.model;

import java.time.LocalDate;

public record IssuedBook(long issueId, String studentId, String studentName, long bookId,
                         String bookTitle, LocalDate issuedOn, LocalDate dueOn, String status,
                         LocalDate returnedOn) {
}
