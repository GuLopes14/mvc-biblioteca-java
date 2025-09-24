package br.com.fiap.biblioteca.service;

import br.com.fiap.biblioteca.model.Book;
import br.com.fiap.biblioteca.model.Loan;
import br.com.fiap.biblioteca.model.LoanStatus;
import br.com.fiap.biblioteca.model.User;
import br.com.fiap.biblioteca.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private UserService userService;
    
    public List<Loan> findAll() {
        return loanRepository.findAll();
    }
    
    public List<Loan> findByUser(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return loanRepository.findByUser(user);
    }
    
    public List<Loan> findActiveLoansForUser(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return loanRepository.findActiveLoansForUser(user);
    }
    
    public List<Loan> findOverdueLoans() {
        return loanRepository.findOverdueLoans(LocalDate.now());
    }
    
    @Transactional
    public Loan createLoan(Long userId, Long bookId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Book book = bookService.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available for loan");
        }
        
        Optional<Loan> existingLoan = loanRepository
                .findByUserAndBookAndStatus(user, book, LoanStatus.ACTIVE);
        if (existingLoan.isPresent()) {
            throw new RuntimeException("User already has this book on loan");
        }
        
        Loan loan = new Loan(user, book);
        
        if (!bookService.decreaseAvailableQuantity(bookId)) {
            throw new RuntimeException("Error processing loan");
        }
        
        return loanRepository.save(loan);
    }
    
    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new RuntimeException("This loan has already been completed");
        }
        
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now());
        
        bookService.increaseAvailableQuantity(loan.getBook().getId());
        
        return loanRepository.save(loan);
    }
    
    public void updateOverdueLoans() {
        List<Loan> overdueLoans = findOverdueLoans();
        for (Loan loan : overdueLoans) {
            if (loan.getStatus() == LoanStatus.ACTIVE) {
                loan.setStatus(LoanStatus.LATE);
                loanRepository.save(loan);
            }
        }
    }
    
    public Optional<Loan> findById(Long id) {
        return loanRepository.findById(id);
    }
}