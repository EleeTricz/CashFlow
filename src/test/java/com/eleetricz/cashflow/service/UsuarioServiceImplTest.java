package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Usuario;
import com.eleetricz.cashflow.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsuarioServiceImplTest {
    private UsuarioRepository usuarioRepository;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        usuarioService = new UsuarioServiceImpl(usuarioRepository);
    }

    @Test
    void deveListarTodosUsuarios() {
        Usuario u1 = new Usuario();
        u1.setId(1L);
        u1.setNome("João");

        Usuario u2 = new Usuario();
        u2.setId(2L);
        u2.setNome("Maria");

        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<Usuario> usuarios = usuarioService.listarTodos();

        assertEquals(2, usuarios.size());
        assertEquals("João", usuarios.get(0).getNome());
    }

    @Test
    void deveBuscarUsuarioPorIdExistente() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Carlos");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        Usuario encontrado = usuarioService.buscarPorId(1L);

        assertNotNull(encontrado);
        assertEquals("Carlos", encontrado.getNome());
    }

    @Test
    void deveRetornarNullSeUsuarioNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        Usuario resultado = usuarioService.buscarPorId(99L);

        assertNull(resultado);
    }

}
