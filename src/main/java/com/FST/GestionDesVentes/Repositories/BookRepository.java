package com.FST.GestionDesVentes.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.FST.GestionDesVentes.Entities.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer>{

}
