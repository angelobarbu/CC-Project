package com.example.business.controller;

import com.example.business.products.Product;
import com.example.business.repository.ProductRepository;
import com.example.business.tokenvalidation.TokenValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v2")
public class Controller implements ErrorController {
    private final static String PATH = "/error";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TokenValidationService tokenValidationService;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts(HttpServletRequest request) {
        boolean isValidToken = tokenValidationService.validateToken(request.getHeader("Authorization"));
        System.out.println("IS VALID? " + isValidToken);
        if (!isValidToken)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        try {
            List<Product> products = new ArrayList<>(productRepository.findAll());

            if (products.isEmpty())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);

            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("EXCEPTION");
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(int id, HttpServletRequest request) {
        boolean isValidToken = tokenValidationService.validateToken(request.getHeader("Authorization"));
        if (!isValidToken)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        try {
            Optional<Product> product = productRepository.findById(id);

            return product.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/products/{id}")
    public ResponseEntity<Product> postProduct(Product product, HttpServletRequest request) {
        boolean isValidToken = tokenValidationService.validateToken(request.getHeader("Authorization"));
        if (!isValidToken)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        try {
            System.out.println("POST");
            System.out.println(product.getId() + "   " + product.getPrice() + "   " + product.getAmount());
            Product prod = new Product(product.getId(), product.getPrice(), product.getAmount());

            productRepository.saveAndFlush(prod);

            return new ResponseEntity<>(prod, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> putProduct(Product product, HttpServletRequest request) {
        boolean isValidToken = tokenValidationService.validateToken(request.getHeader("Authorization"));
        if (!isValidToken)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        try {
            System.out.println("PUT");
            System.out.println(product.getId() + "   " + product.getPrice() + "  " + product.getAmount());

            Optional<Product> prod = productRepository.findById(product.getId());
            if (prod.isEmpty())
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            productRepository.deleteById(prod.get().getId());
            productRepository.saveAndFlush(new Product(product.getId(), product.getPrice(), product.getAmount()));
            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(PATH)
    public String getErrorPath() {
        // TODO Auto-generated method stub
        return "No Mapping Found";
    }

}
