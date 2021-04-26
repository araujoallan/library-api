package com.allan.libraryapi.resource;

import com.allan.libraryapi.dto.BookDTO;
import com.allan.libraryapi.exception.BusinessException;
import com.allan.libraryapi.model.entity.Book;
import com.allan.libraryapi.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService bookService;

    @Test
    @DisplayName("Deve criar um livro com sucesso")
    public void createBookTest() throws Exception {

        BookDTO dto = BookDTO.builder().author("Allan").title("As aventuras").isbn("001").build();
        Book savedBook = Book.builder().id(1L).author("Allan").title("As aventuras").isbn("001").build();
        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willReturn(savedBook);
        String json = new ObjectMapper().writeValueAsString(dto);

         MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(BOOK_API)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(json);

         mvc
            .perform(request)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("id").value(1L) )
            .andExpect(jsonPath("title").value(dto.getTitle()) )
            .andExpect(jsonPath("author").value(dto.getAuthor()) )
            .andExpect(jsonPath("isbn").value(dto.getIsbn()) )
        ;
    }

    //@Test
    @DisplayName("Deve lançar erro de validação quando não tiver dados suficientes  para criar um  livro")
    public void createInvalidBookTest() throws Exception {
        BookDTO book = this.createNewBook();
        String json = new ObjectMapper().writeValueAsString(book);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(BOOK_API)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(json);

        mvc.perform(request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar isbn já utilizado por outro livro.")
    public void createBookWithDuplicateIsbn() throws Exception {
        BookDTO book = this.createNewBook();
        String json = new ObjectMapper().writeValueAsString(book);
        String mensagemErro = "Isbn já cadastrado.";
        BDDMockito.given(bookService.save(Mockito.any(Book.class)))
                    .willThrow(new BusinessException(mensagemErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(BOOK_API)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(json);

        mvc.perform(request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("errors", hasSize(1)))
            .andExpect(jsonPath("errors[0]").value(mensagemErro));
    }

    @Test
    @DisplayName("Deve obter informações de livro")
    public void getBookDetailTest() throws Exception {
        Long id = 1L;
        Book book = Book.builder()
            .id(id)
            .title(createNewBook().getTitle())
            .author(createNewBook().getAuthor())
            .isbn(createNewBook().getIsbn())
            .build();

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get(BOOK_API.concat("/" + id))
            .accept(MediaType.APPLICATION_JSON);

        mvc
            .perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").value(id) )
            .andExpect(jsonPath("title").value(createNewBook().getTitle()) )
            .andExpect(jsonPath("author").value(createNewBook().getAuthor()) )
            .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()) );
    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir")
    public void bookNotFoundTest() throws Exception {

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception {

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
            .perform(request)
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar um livro para deletar")
    public void deleteInexistentBookTest() throws Exception {

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar dados de um livro")
    public void updateBookTest() throws Exception{
        Long id = 1L;
        String json = new ObjectMapper().writeValueAsString(createNewBook());
        Book updatingbook = Book.builder().id(1L).author("Outro autor").title("Outro titulo").isbn("123").build();
        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(updatingbook));

        Book updatedBook = Book.builder().id(id).author("Allan").title("As aventuras").isbn("123").build();
        BDDMockito.given(bookService.update(updatingbook)).willReturn(updatedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id) )
                .andExpect(jsonPath("title").value(createNewBook().getTitle()) )
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()) )
                .andExpect(jsonPath("isbn").value("123") );
        ;
    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar um livro para atualizar")
    public void updateInexistentBookTest() throws Exception{
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void findBooksTest() throws Exception {
        Long id = 1L;
        Book book = Book.builder()
                    .id(id)
                    .title(createNewBook().getTitle())
                    .author(createNewBook().getAuthor())
                    .isbn(createNewBook().getIsbn())
                    .build();

        BDDMockito.given( bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

        //"api/books?"
        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(), book.getAuthor());

         MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                 .accept(MediaType.APPLICATION_JSON);

         mvc
                 .perform(request)
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("content", Matchers.hasSize(1)))
                 .andExpect(jsonPath("totalElements").value(1))
                 .andExpect(jsonPath("pageable.pageSize").value(100))
                 .andExpect(jsonPath("pageable.pageNumber").value(0));

    }




    private BookDTO createNewBook() {
        return BookDTO.builder().id(1L).author("Allan").title("As aventuras").isbn("123").build();
    }

}
