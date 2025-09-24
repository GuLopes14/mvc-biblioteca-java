package br.com.fiap.biblioteca.service;

import br.com.fiap.biblioteca.model.Book;
import br.com.fiap.biblioteca.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    public List<Book> findAll() {
        return bookRepository.findAll();
    }
    
    public List<Book> findAvailable() {
        return bookRepository.findAvailableBooks();
    }
    
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }
    
    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
    
    public List<Book> findByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
    
    public List<Book> findByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }
    
    public List<Book> search(String term) {
        return bookRepository.searchByTitleOrAuthor(term);
    }
    
    public Book createBook(Book book) {
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getCopies());
        }
        return bookRepository.save(book);
    }
    
    public Book updateBook(Long id, Book updatedBook) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        if (updatedBook.getTitle() != null) book.setTitle(updatedBook.getTitle());
        if (updatedBook.getAuthor() != null) book.setAuthor(updatedBook.getAuthor());
        if (updatedBook.getIsbn() != null) book.setIsbn(updatedBook.getIsbn());
        if (updatedBook.getPublisher() != null) book.setPublisher(updatedBook.getPublisher());
        if (updatedBook.getYear() != null) book.setYear(updatedBook.getYear());
        
        // Ajustar availableCopies se copies mudou
        if (updatedBook.getCopies() != null && !updatedBook.getCopies().equals(book.getCopies())) {
            int oldCopies = book.getCopies() == null ? 0 : book.getCopies();
            int diff = updatedBook.getCopies() - oldCopies;
            book.setCopies(updatedBook.getCopies());
            int newAvailable = (book.getAvailableCopies() == null ? 0 : book.getAvailableCopies()) + diff;
            book.setAvailableCopies(Math.max(newAvailable, 0));
        }
        
        return bookRepository.save(book);
    }
    
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found");
        }
        bookRepository.deleteById(id);
    }
    
    public boolean decreaseAvailableQuantity(Long bookId) {
        Book book = findById(bookId).orElse(null);
        if (book != null && book.getAvailableCopies() != null && book.getAvailableCopies() > 0) {
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            bookRepository.save(book);
            return true;
        }
        return false;
    }
    
    public void increaseAvailableQuantity(Long bookId) {
        Book book = findById(bookId).orElse(null);
        if (book != null) {
            int current = book.getAvailableCopies() == null ? 0 : book.getAvailableCopies();
            book.setAvailableCopies(current + 1);
            bookRepository.save(book);
        }
    }
}