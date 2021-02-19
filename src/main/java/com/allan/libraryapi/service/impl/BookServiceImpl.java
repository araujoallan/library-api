package com.allan.libraryapi.service.impl;

import com.allan.libraryapi.exception.BusinessException;
import com.allan.libraryapi.model.entity.Book;
import com.allan.libraryapi.model.repository.BookRepository;
import com.allan.libraryapi.service.BookService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book save(Book book) {
        if(bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("Isbn já cadastrado.");
        }

        return bookRepository.save(book);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return this.bookRepository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if(book == null || book.getId() == null) {
            throw new IllegalArgumentException("Book ID should be different of null");
        }

        this.bookRepository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if(book == null || book.getId() == null) {
            throw new IllegalArgumentException("Book ID should be different of null");
        }

        //se ja existir o livro, o save se comporta como update
        //caso contrario, cria novo livro
        return  this.bookRepository.save(book);
    }
}
