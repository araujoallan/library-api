package com.allan.libraryapi.resource;

import com.allan.libraryapi.dto.LoanDTO;
import com.allan.libraryapi.model.entity.Book;
import com.allan.libraryapi.model.entity.Loan;
import com.allan.libraryapi.service.BookService;
import com.allan.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.jni.Local;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto) {

        Book book = bookService.getBookByIsbn(dto.getIsbn())
                .orElseThrow( () ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for ISBN"));

        Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = loanService.save(entity);
        return entity.getId();
    }
}
