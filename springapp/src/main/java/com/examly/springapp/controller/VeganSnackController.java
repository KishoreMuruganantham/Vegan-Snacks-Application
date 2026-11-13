package com.examly.springapp.controller;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.examly.springapp.model.VeganSnack;
import com.examly.springapp.service.VeganSnackService;
import com.examly.springapp.exception.InvalidExpiryException;
@RestController
public class VeganSnackController {
 private final VeganSnackService service;
 public VeganSnackController(VeganSnackService service){this.service=service;}
 @PostMapping("/addVeganSnack")
 public ResponseEntity<?> addVeganSnack(@RequestBody VeganSnack snack){
  if(snack.getSnackName()==null||snack.getSnackName().trim().isEmpty()||snack.getSnackType()==null||snack.getSnackType().trim().isEmpty()||snack.getQuantity()==null||snack.getQuantity().trim().isEmpty()||snack.getPrice()<=0) return ResponseEntity.badRequest().body("Invalid snack details.");
  try{return ResponseEntity.status(201).body(service.addVeganSnack(snack));}catch(InvalidExpiryException e){return ResponseEntity.badRequest().body(e.getMessage());}
 }
 @GetMapping("/getAllVeganSnacks") public ResponseEntity<List<VeganSnack>> getAllVeganSnacks(){return ResponseEntity.ok(service.getAllVeganSnacks());}
 @ExceptionHandler(InvalidExpiryException.class) public ResponseEntity<String> handleInvalidExpiry(InvalidExpiryException e){return ResponseEntity.badRequest().body(e.getMessage());}
}