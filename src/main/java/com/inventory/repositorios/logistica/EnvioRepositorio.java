    package com.inventory.repositorios.logistica;
    import com.inventory.modelo.entidades.logistica.Envio;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.List;

    @Repository
    public interface EnvioRepositorio extends JpaRepository<Envio, Long> {
        List<Envio> findByTransferenciaId(Long transferenciaId);
    }



