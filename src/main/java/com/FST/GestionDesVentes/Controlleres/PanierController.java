package com.FST.GestionDesVentes.Controlleres;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.FST.GestionDesVentes.Entities.Commande;
import com.FST.GestionDesVentes.Entities.Panier;
import com.FST.GestionDesVentes.Entities.Produit;
import com.FST.GestionDesVentes.Entities.ProduitPanier;
import com.FST.GestionDesVentes.Repositories.CommandeRepository;
import com.FST.GestionDesVentes.Repositories.PanierRepository;
import com.FST.GestionDesVentes.Repositories.ProduitPanierRepository;
import com.FST.GestionDesVentes.Repositories.ProduitRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/panier")
@CrossOrigin(origins = "http://localhost:4200")
public class PanierController {

	private final PanierRepository panierRepository;
	private final ProduitPanierRepository produitPanierRepo;
	private final ProduitRepository produitRepository;
	private final CommandeRepository commandeRepository;

	@Autowired
	public PanierController(PanierRepository panierRepository, ProduitRepository produitRepository,
			ProduitPanierRepository produitPanierRepository,
			CommandeRepository commandeRepository) {
		this.panierRepository = panierRepository;
		this.produitPanierRepo = produitPanierRepository;
		this.produitRepository = produitRepository;
		this.commandeRepository = commandeRepository;
	}

	@GetMapping("/list")
	public List<Panier> getAllPaniers() {
		return (List<Panier>) panierRepository.findAll();
	}

	
	@PostMapping("/add")
	public ResponseEntity<String> createPanier(@RequestBody Panier panier) {

		// Enregistrez le panier
		try {
			panierRepository.save(panier);
			return ResponseEntity.ok("Panier créé avec succès");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Erreur lors de la création du panier : " + e.getMessage());
		}
	}

	@PutMapping("/{panierId}")
	public ResponseEntity<Panier> updatePanier(@PathVariable Long panierId, @Valid @RequestBody Panier panierRequest) {
		return panierRepository.findById(panierId).map(panier -> {
			panier.setQuantite(panierRequest.getQuantite());
			panier.setStatut(panierRequest.getStatut().trim());
			Panier updatedPanier = panierRepository.save(panier);
			return ResponseEntity.ok(updatedPanier);
		}).orElseThrow(() -> new IllegalArgumentException("PanierId " + panierId + " not found"));
	}

	@Transactional
	@DeleteMapping("/{panierId}")
	public ResponseEntity<?> deletePanier(@PathVariable Long panierId) {
		return panierRepository.findById(panierId).map(panier -> {
			// Trouver les commandes associées au panier
			List<Commande> commandes = commandeRepository.findByPanierId(panierId);

			// Supprimer les commandes associées
			commandeRepository.deleteAll(commandes);

			// Supprimer le panier
			panierRepository.delete(panier);

			return ResponseEntity.ok().build();
		}).orElseThrow(() -> new IllegalArgumentException("PanierId " + panierId + " not found"));
	}

	@Transactional
	@GetMapping("/{panierId}")
	public ResponseEntity<List<ProduitPanier>> getProduitsByPanierId(@PathVariable Long panierId) {
		List<ProduitPanier> produits = produitPanierRepo.findByPanierId(panierId);

		if (produits.isEmpty()) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(produits);
		}
	}


	
	@PutMapping("/increaseQuantity/{panierId}/{produitId}")
	public ResponseEntity<Panier> increaseQuantity(@PathVariable Long panierId, @PathVariable Long produitId) {
	    Optional<ProduitPanier> produitPanierOptional = produitPanierRepo.findByPanierIdAndProduitId(panierId, produitId);

	    if (produitPanierOptional.isPresent()) {
	        ProduitPanier produitPanier = produitPanierOptional.get();
	        produitPanier.setQuantite(produitPanier.getQuantite() + 1);
	        produitPanierRepo.save(produitPanier);

	        Panier panier = panierRepository.findById(panierId).orElse(null);
	        if (panier != null) {
	            // Mettre à jour le total du panier
	            panier.setTotal(panier.getTotal() + produitPanier.getProduit().getPrix());
	            panierRepository.save(panier);

	            // Retourner le panier mis à jour
	            return ResponseEntity.ok(panier);
	        }
	    }

	    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}
	
	       
	
	@PutMapping("/decreaseQuantity/{panierId}/{produitId}")
	public ResponseEntity<Panier> decreaseQuantity(@PathVariable Long panierId, @PathVariable Long produitId) {
	    Optional<ProduitPanier> produitPanierOptional = produitPanierRepo.findByPanierIdAndProduitId(panierId, produitId);

	    if (produitPanierOptional.isPresent()) {
	        ProduitPanier produitPanier = produitPanierOptional.get();
	        if (produitPanier.getQuantite() > 1) {
	            produitPanier.setQuantite(produitPanier.getQuantite() - 1);
	            produitPanierRepo.save(produitPanier);

	            Panier panier = panierRepository.findById(panierId).orElse(null);
	            panier.setTotal(panier.getTotal() - produitPanier.getProduit().getPrix());

	            return ResponseEntity.ok(panierRepository.save(panier));
	        } else {
	            return ResponseEntity.badRequest().body(null); // Ne peut pas être inférieur à 1
	        }
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	    }
	}


	
	
	
	
	@PostMapping("/addProduitToPanier/{idPanier}/{idProduit}")
	public ResponseEntity<Map<String, String>> addProduitToPanier(
			@PathVariable long idPanier,
			@PathVariable long idProduit,
			@RequestBody Map<String, Integer> requestBody) {

		Panier panier = panierRepository.findById(idPanier).orElse(null);
		Produit produit = produitRepository.findById(idProduit).orElse(null);
		int quantite = requestBody.get("quantite");

		// Vérifier si le panier et le produit existent
		if (panier == null || produit == null) {
			Map<String, String> response = new HashMap<>();
			response.put("message", "Panier ou Produit non trouvé");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (produit.getStock() < quantite) {
			Map<String, String> response = new HashMap<>();
			response.put("message", "Stock insuffisant pour le produit : " + produit.getNom());
			return ResponseEntity.badRequest().body(response);
		}

		// Mise à jour du stock du produit
		produit.setStock(produit.getStock() - quantite);
		produitRepository.save(produit);

		try {
			// Créer une nouvelle instance de ProduitPanier et l'ajouter au panier
			ProduitPanier produitPanier = new ProduitPanier();
			produitPanier.setProduit(produit);
			produitPanier.setPanier(panier);
			produitPanier.setQuantite(quantite);
			produitPanierRepo.save(produitPanier);

			panier.getProduitsPanier().add(produitPanier);

			// Recalculer le total du panier

			double total = 0.0;
			List<ProduitPanier> produitsPanier = produitPanierRepo.findByPanierId(panier.getId());
			for (ProduitPanier produitPan : produitsPanier) {
				Produit produitpn = produitPan.getProduit();
				// System.out.println(produitpn.getPrix());

				if (produitpn != null && produitpn.getPrix() != null) {
					total += produitPan.getQuantite() * produitpn.getPrix();
				} else {
					System.err.println(
							"Produit or Produit Prix is null for ProduitPanier with ID: " + produitPan.getId());
				}
			}

			panier.setTotal(total);
			// panierRepository.save(panier); // Sauvegarder le panier avec le nouveau total
			System.out.println(total);

			Map<String, String> response = new HashMap<>();

			response.put("message", "Produit ajouté au panier");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			// Gérer les autres exceptions
			Map<String, String> response = new HashMap<>();
			response.put("message", "Erreur lors de l'ajout du produit au panier");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	
	@DeleteMapping("removeProduitFromPanier/{idPanier}/{idProduit}")
	public ResponseEntity<Panier> removeProduitFromPanier(@PathVariable long idPanier, @PathVariable long idProduit) {
		Panier panier = panierRepository.findById(idPanier).orElse(null);
		Produit produit = produitRepository.findById(idProduit).orElse(null);

		if (panier == null || produit == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		panier.getProduitsPanier().removeIf(produitPanier -> produitPanier.getProduit().getId() == idProduit);

		double total = 0.0;
		List<ProduitPanier> produitsPanier = produitPanierRepo.findByPanierId(panier.getId());

		for (ProduitPanier produitPan : produitsPanier) {
			Produit produitpn = produitPan.getProduit();
			if (produitpn != null && produitpn.getPrix() != null) {
				total += produitPan.getQuantite() * produitpn.getPrix();
			} else {
				System.err.println("Produit or Produit Prix is null for ProduitPanier with ID: " + produitPan.getId());
			}
		}

		panier.setTotal(total);

		Panier updatedPanier = panierRepository.save(panier);

		return ResponseEntity.ok(updatedPanier);
	}

	
}