package br.com.fiap.biblioteca.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    private String publisher;
    
    private Integer year;
    
    @Column(unique = true)
    private String isbn;
    
    private Integer copies;
    
    @Column(name = "available_copies")
    private Integer availableCopies;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Loan> loans;
    
    public Book() {}
    
    public Book(String title, String author, String isbn, Integer copies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.copies = copies;
        this.availableCopies = copies;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public Integer getCopies() { return copies; }
    public void setCopies(Integer copies) { this.copies = copies; }
    
    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Loan> getLoans() { return loans; }
    public void setLoans(List<Loan> loans) { this.loans = loans; }
    
    public boolean isAvailable() {
        return availableCopies != null && availableCopies > 0;
    }
    
    public Integer getTotalQuantity() { return copies; }
    public void setTotalQuantity(Integer totalQuantity) { this.copies = totalQuantity; }
    
    public Integer getAvailableQuantity() { return availableCopies; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableCopies = availableQuantity; }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}