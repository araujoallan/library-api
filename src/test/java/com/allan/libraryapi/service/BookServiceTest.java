package com.allan.libraryapi.service;

import com.allan.libraryapi.exception.BusinessException;
import com.allan.libraryapi.model.entity.Book;
import com.allan.libraryapi.model.repository.BookRepository;
import com.allan.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        this.bookService = new BookServiceImpl(bookRepository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {
        //cenario
        Book book = createValidBook();
        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(false);

        Mockito.when( bookRepository.save(book))
                .thenReturn(Book.builder()
                        .id(1L)
                        .isbn("123")
                        .author("Fulano")
                        .title("Titulo teste")
                        .build());

        //execucao
        Book savedBook = bookService.save(book);

        //verificacao
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("Titulo teste");
        assertThat(savedBook.getAuthor()).isEqualTo("Fulano");

    }

    @Test
    @DisplayName("Deve lançar erro de negocio ao tentar salvar um livro com isbn duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN() {
        Book book = createValidBook();
        //default da inteface de retorno boolean é false
        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> bookService.save(book));
        assertThat(exception)
               .isInstanceOfAny(BusinessException.class)
               .hasMessage("Isbn já cadastrado.");

        Mockito.verify(bookRepository, Mockito.never()).save(book);

    }

    public Book createValidBook() {

        return Book.builder().isbn("123").author("Fulano").title("Titulo teste").build();
    }

    @Test
    @DisplayName("Deve obter um livro por ID")
    public void getByIdTest() {
        Long id = 1L;
        Book book = createValidBook();
        book.setId(id);

        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        //execucao
        Optional<Book> foundBook = bookService.getById(id);

        //verificacao
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por ID quando ele não existir na base")
    public void bookNotFoundByIdTest() {
        Long id = 1L;
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.empty());

        //execucao
        Optional<Book> book = bookService.getById(id);

        //verificacao
        assertThat(book.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro.")
    public void deleteBookTest() {
        Book book = Book.builder().id(1L).build();

        //execucao
        org.junit.jupiter.api.Assertions.assertDoesNotThrow( () -> bookService.delete(book));

        //validacao
        Mockito
            .verify(bookRepository, Mockito.times(1))
            .delete(book);
    }


    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um livro.")
    public void deleteInvalidBookTest() {
        Book book = new Book();

        //execucao
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.delete(book));

        //validacao
        Mockito.verify(bookRepository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro.")
    public void updateInvalidBookTest() {
        Book book = new Book();

        //execucao
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.update(book));

        //validacao
        Mockito.verify(bookRepository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() {
        //cenario
        Long id = 1l;

        //livro para atualizar
        Book updatingBook = Book.builder().id(id).build();

        //simulando atualizacao
        Book updatedBook = this.createValidBook();
        updatedBook.setId(id);

        Mockito.when(bookRepository.save(updatingBook)).thenReturn(updatedBook);

        //execucao
        Book book = bookService.update(updatingBook);

        //verificacao
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
    }
}
