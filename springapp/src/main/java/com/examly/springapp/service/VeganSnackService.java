package com.examly.springapp.service;
import java.util.List;
import org.springframework.stereotype.Service;
import com.examly.springapp.model.VeganSnack;
import com.examly.springapp.repository.VeganSnackRepo;
import com.examly.springapp.exception.InvalidExpiryException;
@Service
public class VeganSnackService {
 private final VeganSnackRepo repo;
 public VeganSnackService(VeganSnackRepo repo){this.repo=repo;}
 public VeganSnack addVeganSnack(VeganSnack snack){if(snack.getExpiryInMonths()<0) throw new InvalidExpiryException("Expiry in months should not be a negative value."); return repo.save(snack);}
 public List<VeganSnack> getAllVeganSnacks(){return repo.findAll();}
}