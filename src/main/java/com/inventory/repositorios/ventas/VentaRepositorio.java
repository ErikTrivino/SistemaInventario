    package com.inventory.repositorios.ventas;
    import com.inventory.modelo.entidades.ventas.Venta;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.Date;
    import java.util.List;

    @Repository
    public interface VentaRepositorio extends JpaRepository<Venta, Long> {
        List<Venta> findByBranchId(Long branchId);
        List<Venta> findByCreatedAtBetween(Date start, Date end);
    }



