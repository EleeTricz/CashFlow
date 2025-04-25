package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    List<Empresa> findByUsuario(Usuario usuario);

}
