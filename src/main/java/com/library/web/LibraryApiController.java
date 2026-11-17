package com.library.web;

import com.library.model.Book;
import com.library.model.Student;
import com.library.service.BookService;
import com.library.service.FineService;
import com.library.service.IssueService;
import com.library.service.ReportService;
import com.library.service.ReturnService;
import com.library.service.SettingsService;
import com.library.service.StudentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LibraryApiController {
    private final BookService books;
    private final StudentService students;
    private final IssueService issues;
    private final ReturnService returns;
    private final FineService fines;
    private final SettingsService settings;
    private final ReportService reports;

    public LibraryApiController(BookService books, StudentService students, IssueService issues,
                                ReturnService returns, FineService fines, SettingsService settings,
                                ReportService reports) {
        this.books = books; this.students = students; this.issues = issues; this.returns = returns;
        this.fines = fines; this.settings = settings; this.reports = reports;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() throws Exception {
        return Map.of("totalBooks", books.totalCount(), "availableCopies", books.availableCount(),
                "issuedBooks", issues.activeCount(), "students", students.count(),
                "overdueBooks", issues.overdueCount(), "finesCollected", fines.totalCollected());
    }

    @GetMapping("/books")
    public Object books(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "title") String sort) throws Exception {
        return books.search(q, sort);
    }

    @PostMapping("/books")
    public Map<String, String> addBook(@RequestBody BookRequest body) throws Exception {
        books.save(body.toBook(0)); return Map.of("status", "created");
    }

    @PutMapping("/books/{id}")
    public Map<String, String> updateBook(@PathVariable long id, @RequestBody BookRequest body) throws Exception {
        books.save(body.toBook(id)); return Map.of("status", "updated");
    }

    @DeleteMapping("/books/{id}")
    public Map<String, String> deleteBook(@PathVariable long id) throws Exception {
        books.delete(id); return Map.of("status", "deleted");
    }

    @GetMapping("/students")
    public Object students(@RequestParam(defaultValue = "") String q) throws Exception { return students.search(q); }

    @PostMapping("/students")
    public Map<String, String> addStudent(@RequestBody Student body) throws Exception {
        students.add(body); return Map.of("status", "created");
    }

    @PutMapping("/students/{id}")
    public Map<String, String> updateStudent(@PathVariable String id, @RequestBody Student body) throws Exception {
        if (!id.equals(body.id())) throw new IllegalArgumentException("Student ID cannot be changed.");
        students.update(body); return Map.of("status", "updated");
    }

    @DeleteMapping("/students/{id}")
    public Map<String, String> deleteStudent(@PathVariable String id) throws Exception {
        students.delete(id); return Map.of("status", "deleted");
    }

    @GetMapping("/loans")
    public Object activeLoans(@RequestParam(defaultValue = "") String q,
                              @RequestParam(defaultValue = "false") boolean overdueOnly) throws Exception {
        return issues.active(q, overdueOnly);
    }

    @GetMapping("/loans/history")
    public Object loanHistory(@RequestParam(defaultValue = "") String q) throws Exception { return issues.history(q); }

    @PostMapping("/loans")
    public Map<String, Object> issue(@RequestBody IssueRequest body) throws Exception {
        return Map.of("issueId", issues.issue(body.studentId(), body.bookId()));
    }

    @GetMapping("/returns")
    public Object returnHistory(@RequestParam(defaultValue = "") String q) throws Exception { return returns.history(q); }

    @PostMapping("/returns")
    public Object returnBook(@RequestBody ReturnRequest body) throws Exception { return returns.process(body.issueId()); }

    @GetMapping("/fines")
    public Object fines(@RequestParam(defaultValue = "") String q,
                        @RequestParam(defaultValue = "All") String status) throws Exception { return fines.search(q, status); }

    @PostMapping("/fines/{id}/payments")
    public Map<String, String> payFine(@PathVariable long id, @RequestBody PaymentRequest body) throws Exception {
        fines.pay(id, body.amount()); return Map.of("status", "payment recorded");
    }

    @GetMapping("/payments")
    public Object payments() throws Exception { return fines.paymentHistory(); }

    @GetMapping("/reports")
    public Object report(@RequestParam String type) throws Exception { return reports.generate(type); }

    @GetMapping("/settings")
    public SettingsResponse settings() throws Exception { return new SettingsResponse(settings.finePerDay(), settings.loanPeriodDays()); }

    @PutMapping("/settings")
    public Map<String, String> updateSettings(@RequestBody SettingsRequest body) throws Exception {
        settings.update(body.finePerDay(), body.loanPeriodDays()); return Map.of("status", "saved");
    }

    public record BookRequest(Long id, String isbn, String title, String author, String publisher,
                              String category, BigDecimal price, int quantity) {
        Book toBook(long pathId) {
            return new Book(pathId, isbn, title, author, publisher, category, price, quantity, 0);
        }
    }
    public record IssueRequest(String studentId, long bookId) { }
    public record ReturnRequest(long issueId) { }
    public record PaymentRequest(String amount) { }
    public record SettingsRequest(String finePerDay, String loanPeriodDays) { }
    public record SettingsResponse(BigDecimal finePerDay, int loanPeriodDays) { }
}
