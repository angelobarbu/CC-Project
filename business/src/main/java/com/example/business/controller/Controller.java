package com.example.business.controller;

import com.example.business.products.Product;
import com.example.business.repository.ProductRepository;
import com.example.business.task.CreateTaskInput;
import com.example.business.task.TaskItemResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v2")
public class Controller implements ErrorController {
    private final static String PATH = "/error";
    private static final String API_URL = "http://your-spring-boot-api-endpoint/resource";
    private static final String AUTH_TOKEN = "your-authentication-token";

    @Autowired
    private ProductRepository productRepository;

    RestTemplate restTemplate = new RestTemplate();




//    @GetMapping("/example")
//    public ResponseEntity<String> exampleEndpoint(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
//        // Assuming the Authorization header is in the format "Bearer <token>"
//        System.out.println(authorizationHeader);
//        String[] parts = authorizationHeader.split(" ");
//        if (parts.length == 2 && "Bearer".equals(parts[0])) {
//            String bearerToken = parts[1];
//            // Now 'bearerToken' contains the Bearer token, and you can use it as needed.
//            return ResponseEntity.ok("Bearer token: " + bearerToken);
//        } else {
//            // Handle invalid or missing Authorization header
//            return ResponseEntity.status(401).body("Invalid or missing Bearer token");
//        }
//    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
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
    public ResponseEntity<Product> getProductById(int id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
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
    public ResponseEntity<Product> postProduct(Product product, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
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
    public ResponseEntity<Product> putProduct(Product product, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
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

//    private boolean isSecurityTokenValid(String token) {
//        CreateTaskInput createTaskInput = new CreateTaskInput();
//
//        createTaskInput
//                .setTitle("My task one")
//                .setDescription("Description of my task one")
//                .setStatus("Pending")
//                .setImportant(true)
//                .setDueDate(LocalDateTime.now().plusDays(7))
//                .setUserId("632f2d1c7e05bb050cdda4cb");
//
//        HttpEntity<CreateTaskInput> request = new HttpEntity<>(createTaskInput, headers);
//        String url = generateUrl("/tasks/create");
//
//        ResponseEntity<TaskItemResponse> result = restTemplate.postForEntity(url, request, TaskItemResponse.class);
//        TaskItemResponse task = result.getBody();
//
//        assert task != null;
//
//        return ResponseEntity.ok().body(task);
//
//        return false;
//    }

    @RequestMapping(PATH)
    public String getErrorPath() {
        // TODO Auto-generated method stub
        return "No Mapping Found";
    }


//    public class RestClient {
//
//        public static void main(String[] args) {
//            RestTemplate restTemplate = new RestTemplate();
//
//            // Add an interceptor to include the authentication token in the request headers
//            restTemplate.getInterceptors().add((RequestInterceptor) (request, body, execution) -> {
//                HttpHeaders headers = request.getHeaders();
//                headers.add("Authorization", "Bearer " + AUTH_TOKEN);
//            });
//
//            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.GET, null, String.class);
//
//            System.out.println("Response: " + response.getBody());
//        }
//    }

}
