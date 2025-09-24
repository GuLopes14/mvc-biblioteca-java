package br.com.fiap.biblioteca.controller;

import br.com.fiap.biblioteca.auth.AuthUtils;
import br.com.fiap.biblioteca.model.Book;
import br.com.fiap.biblioteca.model.Loan;
import br.com.fiap.biblioteca.service.BookService;
import br.com.fiap.biblioteca.service.LoanService;
import br.com.fiap.biblioteca.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private LoanService loanService;

    @GetMapping("/")
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !authentication.getName().equals("anonymousUser")) {
            model.addAttribute("authenticated", true);

            // Usuário OAuth2 do Google
            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                model.addAttribute("name", oauth2User.getAttribute("name"));
            }
        } else {
            model.addAttribute("authenticated", false);
        }
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                String email = oauth2User.getAttribute("email");
                model.addAttribute("name", oauth2User.getAttribute("name"));
                model.addAttribute("email", email);
                model.addAttribute("picture", oauth2User.getAttribute("picture"));
                model.addAttribute("provider", "Google");

                // Carregar empréstimos do usuário para exibir no dashboard
                Long userId = userService.findByEmail(email).map(u -> u.getId())
                        .orElse(null);
                if (userId != null) {
                    List<Loan> loans = loanService.findByUser(userId);
                    model.addAttribute("loans", loans);
                    model.addAttribute("hasLoans", loans != null && !loans.isEmpty());
                } else {
                    model.addAttribute("hasLoans", false);
                }
            }
        }

        return "dashboard";
    }

    @GetMapping("/view-available-books")  // Caminho único para evitar qualquer conflito
    public String availableBooks(Model model, @RequestParam(required = false) String search) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Adiciona informações do usuário autenticado ao modelo
        boolean isAdmin = AuthUtils.isAdmin(authentication);
        model.addAttribute("isAdmin", isAdmin);
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                model.addAttribute("name", oauth2User.getAttribute("name"));
                model.addAttribute("email", oauth2User.getAttribute("email"));
            }
        }

        // Se houver termo de pesquisa, use o método de busca
        if (search != null && !search.isEmpty()) {
            model.addAttribute("books", bookService.search(search));
            model.addAttribute("searchTerm", search);
        } else {
            // Caso contrário, mostre apenas livros disponíveis
            model.addAttribute("books", bookService.findAvailable());
        }

        return "books";
    }

    @PostMapping("/loans/create")
    public String createLoanFromWeb(@RequestParam("bookId") Long bookId,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
                redirectAttributes.addFlashAttribute("error", "Usuário não autenticado");
                return "redirect:/view-available-books";
            }
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            if (email == null) {
                redirectAttributes.addFlashAttribute("error", "Não foi possível identificar o email do usuário");
                return "redirect:/view-available-books";
            }

            Long userId = userService.findByEmail(email)
                    .map(u -> u.getId())
                    .orElseGet(() -> {
                        // Como fallback, registra o usuário e retorna o ID
                        var u = userService.register(oauth2User);
                        return u != null ? u.getId() : null;
                    });

            if (userId == null) {
                redirectAttributes.addFlashAttribute("error", "Usuário não encontrado/registrado no sistema");
                return "redirect:/view-available-books";
            }

            loanService.createLoan(userId, bookId);
            redirectAttributes.addFlashAttribute("message", "Empréstimo criado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() != null ? e.getMessage() : "Erro ao criar empréstimo");
        }
        return "redirect:/view-available-books";
    }

    @GetMapping("/loans/create")
    public String loansCreateGetFallback(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Operação inválida via GET. Use o botão Emprestar.");
        return "redirect:/view-available-books";
    }

    // Admin: Editar livro (formulário)
    @GetMapping("/books/edit/{id}")
    public String editBookForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return bookService.findById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    return "book-edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Livro não encontrado");
                    return "redirect:/view-available-books";
                });
    }

    // Admin: Salvar edição
    @PostMapping("/books/edit")
    public String editBookSubmit(@RequestParam Long id,
                                 @RequestParam String title,
                                 @RequestParam String author,
                                 @RequestParam(required = false) String publisher,
                                 @RequestParam(required = false) Integer year,
                                 @RequestParam(required = false) String isbn,
                                 @RequestParam Integer copies,
                                 RedirectAttributes redirectAttributes) {
        try {
            Book updated = new Book();
            updated.setTitle(title);
            updated.setAuthor(author);
            updated.setPublisher(publisher);
            updated.setYear(year);
            updated.setIsbn(isbn);
            updated.setCopies(copies);

            bookService.updateBook(id, updated);
            redirectAttributes.addFlashAttribute("message", "Livro atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() != null ? e.getMessage() : "Erro ao atualizar livro");
        }
        return "redirect:/view-available-books";
    }

    // Admin: Excluir livro
    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("message", "Livro excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() != null ? e.getMessage() : "Erro ao excluir livro");
        }
        return "redirect:/view-available-books";
    }

    @GetMapping("/my-loans")
    public String myLoans(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
                redirectAttributes.addFlashAttribute("error", "Faça login para ver seus empréstimos");
                return "redirect:/login";
            }
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            model.addAttribute("name", oauth2User.getAttribute("name"));
            model.addAttribute("email", email);

            Long userId = userService.findByEmail(email)
                    .map(u -> u.getId())
                    .orElseGet(() -> {
                        var u = userService.register(oauth2User);
                        return u != null ? u.getId() : null;
                    });

            if (userId == null) {
                redirectAttributes.addFlashAttribute("error", "Não foi possível identificar o usuário");
                return "redirect:/";
            }

            model.addAttribute("loans", loanService.findByUser(userId));
            return "my-loans";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() != null ? e.getMessage() : "Erro ao carregar seus empréstimos");
            return "redirect:/";
        }
    }

    @PostMapping("/loans/{id}/return")
    public String returnLoanFromWeb(@PathVariable("id") Long loanId,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
                redirectAttributes.addFlashAttribute("error", "Faça login para devolver um livro");
                return "redirect:/login";
            }
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            Long userId = userService.findByEmail(email)
                    .map(u -> u.getId())
                    .orElse(null);
            if (userId == null) {
                redirectAttributes.addFlashAttribute("error", "Usuário não encontrado");
                return "redirect:/my-loans";
            }

            var loanOpt = loanService.findById(loanId);
            if (loanOpt.isEmpty() || loanOpt.get().getUser() == null || !userId.equals(loanOpt.get().getUser().getId())) {
                redirectAttributes.addFlashAttribute("error", "Você não tem permissão para devolver este empréstimo");
                return "redirect:/my-loans";
            }

            loanService.returnBook(loanId);
            redirectAttributes.addFlashAttribute("message", "Livro devolvido com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage() != null ? e.getMessage() : "Erro ao devolver livro");
        }
        return "redirect:/my-loans";
    }
}
