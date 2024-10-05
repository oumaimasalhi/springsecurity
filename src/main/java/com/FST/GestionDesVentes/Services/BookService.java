package com.FST.GestionDesVentes.Services;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.FST.GestionDesVentes.Entities.Book;
import com.FST.GestionDesVentes.Entities.BookRequest;
import com.FST.GestionDesVentes.Repositories.BookRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

	
	  @Autowired
	    private BookRepository repository;

	    public void save(BookRequest request) {
	        var book = new Book(request.getAuthor(), request.getIsbn());
	        repository.save(book); 
	    }

	    public List<Book> findAll() {
	        return (List<Book>) repository.findAll(); 
	    }
}
