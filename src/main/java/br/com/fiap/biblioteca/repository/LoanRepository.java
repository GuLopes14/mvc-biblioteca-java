package br.com.fiap.biblioteca.repository;

import br.com.fiap.biblioteca.model.Book;
import br.com.fiap.biblioteca.model.Loan;
import br.com.fiap.biblioteca.model.LoanStatus;
import br.com.fiap.biblioteca.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUser(User user);
    List<Loan> findByBook(Book book);
    List<Loan> findByStatus(LoanStatus status);
    
    Optional<Loan> findByUserAndBookAndStatus(User user, Book book, LoanStatus status);
    
    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.dueDate < :currentDate")
    List<Loan> findOverdueLoans(LocalDate currentDate);
    
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.status = 'ACTIVE'")
    List<Loan> findActiveLoansForUser(User user);
}