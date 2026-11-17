package com.library.web;

import com.library.service.AuthService;
import com.library.service.BookService;
import com.library.service.FineService;
import com.library.service.IssueService;
import com.library.service.ReportService;
import com.library.service.ReturnService;
import com.library.service.SettingsService;
import com.library.service.StudentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LibraryWebConfiguration {
    @Bean AuthService authService() { return new AuthService(); }
    @Bean BookService bookService() { return new BookService(); }
    @Bean StudentService studentService() { return new StudentService(); }
    @Bean IssueService issueService() { return new IssueService(); }
    @Bean ReturnService returnService() { return new ReturnService(); }
    @Bean FineService fineService() { return new FineService(); }
    @Bean SettingsService settingsService() { return new SettingsService(); }
    @Bean ReportService reportService() { return new ReportService(); }
}
