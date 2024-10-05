package com.FST.GestionDesVentes.Controlleres;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.FST.GestionDesVentes.Entities.Book;
import com.FST.GestionDesVentes.Entities.BookRequest;
import com.FST.GestionDesVentes.Services.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

	
     @Autowired	
	 private  BookService service;

     
	    @PostMapping
	    public ResponseEntity<?> save(@RequestBody BookRequest request) {
	        service.save(request);
	        return ResponseEntity.accepted().build();
	    }


	    @GetMapping
	    public ResponseEntity<List<Book>> findAllBooks() {
	        return ResponseEntity.ok(service.findAll());
	    }
	    
	 
	   
}