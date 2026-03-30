    package com.inventory.repositorios.ventas;
    import com.inventory.modelo.entidades.ventas.DetalleVenta;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.List;

    @Repository
    public interface DetalleVentaRepositorio extends JpaRepository<DetalleVenta, Long> {
        List<DetalleVenta> findBySaleId(Long saleId);
    }



