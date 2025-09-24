package br.com.fiap.biblioteca.controller;

import br.com.fiap.biblioteca.model.Loan;
import br.com.fiap.biblioteca.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {
    
    @Autowired
    private LoanService loanService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> findAll() {
        return ResponseEntity.ok(loanService.findAll());
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<Loan>> findMyLoans(Authentication authentication) {
        // TODO: Implement logic to get authenticated user ID
        Long userId = 1L; // placeholder
        return ResponseEntity.ok(loanService.findByUser(userId));
    }
    
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> findOverdue() {
        return ResponseEntity.ok(loanService.findOverdueLoans());
    }
    
    @PostMapping("/create")
    public ResponseEntity<Loan> createLoan(
            @RequestParam Long bookId,
            Authentication authentication) {
        try {
            // TODO: Implement logic to get authenticated user ID
            Long userId = 1L; // placeholder
            Loan loan = loanService.createLoan(userId, bookId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/return")
    public ResponseEntity<Loan> returnBook(@PathVariable Long id) {
        try {
            Loan loan = loanService.returnBook(id);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}