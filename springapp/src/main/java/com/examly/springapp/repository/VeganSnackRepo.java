package com.examly.springapp.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.examly.springapp.model.VeganSnack;
public interface VeganSnackRepo extends JpaRepository<VeganSnack,Integer> {}