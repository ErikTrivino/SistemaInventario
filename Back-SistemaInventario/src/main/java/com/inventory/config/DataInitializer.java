package com.inventory.config;

import com.inventory.modelo.entidades.auditoria.RegistroAuditoria;
import com.inventory.modelo.entidades.compras.DetalleCompra;
import com.inventory.modelo.entidades.compras.OrdenCompra;
import com.inventory.modelo.entidades.eventos.NotificacionEvento;
import com.inventory.modelo.entidades.inventario.*;
import com.inventory.modelo.entidades.logistica.*;
import com.inventory.modelo.entidades.nucleo.*;
import com.inventory.modelo.entidades.proveedores.*;
import com.inventory.modelo.entidades.seguridad.Usuario;
import com.inventory.modelo.entidades.transferencias.*;
import com.inventory.modelo.entidades.ventas.*;
import com.inventory.modelo.enums.*;
import com.inventory.repositorios.auditoria.*;
import com.inventory.repositorios.compras.*;
import com.inventory.repositorios.eventos.*;
import com.inventory.repositorios.inventario.*;
import com.inventory.repositorios.logistica.*;
import com.inventory.repositorios.nucleo.*;
import com.inventory.repositorios.proveedores.*;
import com.inventory.repositorios.seguridad.*;
import com.inventory.repositorios.transferencias.*;
import com.inventory.repositorios.ventas.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Inicializador de datos que se ejecuta al arrancar la aplicación.
 * Asegura la existencia de datos coherentes para todas las entidades.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepositorio userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolEntidadRepositorio rolEntidadRepository;
    private final SucursalRepositorio sucursalRepository;
    private final UnidadMedidaRepositorio unidadMedidaRepository;
    private final ProveedorRepositorio proveedorRepository;
    private final ProductoRepositorio productoRepository;
    private final InventarioRepositorio inventarioRepository;
    private final ProductoProveedorRepositorio productoProveedorRepository;
    private final OrdenCompraRepositorio ordenCompraRepository;
    private final DetalleCompraRepositorio detalleCompraRepository;
    private final VentaRepositorio ventaRepository;
    private final DetalleVentaRepositorio detalleVentaRepository;
    private final MovimientoInventarioRepositorio movimientoRepository;
    private final TransportistaRepositorio transportistaRepository;
    private final RutaRepositorio rutaRepository;
    private final EnvioRepositorio envioRepository;
    private final TransferenciaRepositorio transferenciaRepository;
    private final DetalleTransferenciaRepositorio detalleTransferenciaRepository;
    private final NotificacionEventoRepositorio notificacionRepository;
    private final RegistroAuditoriaRepositorio auditoriaRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Iniciando sembrado de datos iniciales...");

        // 1. Datos base del sistema (Roles, Sucursales, Unidades)
        Map<String, List<?>> coreData = seedCoreData();
        @SuppressWarnings("unchecked")
        List<Sucursal> sucursales = (List<Sucursal>) coreData.get("sucursales");

        // 2. Catálogo de negocio (Proveedores, Productos)
        Map<String, List<?>> catalogData = seedCatalogData();
        @SuppressWarnings("unchecked")
        List<Proveedor> proveedores = (List<Proveedor>) catalogData.get("proveedores");
        @SuppressWarnings("unchecked")
        List<Producto> productos = (List<Producto>) catalogData.get("productos");

        // 3. Seguridad (Usuarios con sucursal asignada)
        List<Usuario> usuarios = seedSecurityData(sucursales);

        // 4. Operaciones y Transacciones
        seedOperationData(sucursales, productos, proveedores, usuarios);

        // 5. Logística y Envios
        seedLogisticData(sucursales, usuarios);

        // 6. Auditoría y Eventos
        seedAuditData(usuarios);

        log.info("✅ Sembrado de datos completado exitosamente.");
    }

    private Map<String, List<?>> seedCoreData() {
        log.info("  - Sembrando datos Core...");
        seedRolEntidades();
        List<Sucursal> sucursales = seedSucursales();
        seedUnidadesMedida();
        
        Map<String, List<?>> data = new HashMap<>();
        data.put("sucursales", sucursales);
        return data;
    }

    private Map<String, List<?>> seedCatalogData() {
        log.info("  - Sembrando datos de Catálogo...");
        List<Proveedor> proveedores = seedProveedores();
        List<Producto> productos = seedProductos();
        seedProductoProveedores(productos, proveedores);

        Map<String, List<?>> data = new HashMap<>();
        data.put("proveedores", proveedores);
        data.put("productos", productos);
        return data;
    }

    private List<Usuario> seedSecurityData(List<Sucursal> sucursales) {
        log.info("  - Sembrando datos de Seguridad...");
        return seedUsuarios(sucursales);
    }

    private void seedOperationData(List<Sucursal> sucs, List<Producto> prods, List<Proveedor> provs, List<Usuario> usus) {
        log.info("  - Sembrando datos de Operaciones...");
        seedInventario(sucs, prods);
        
        List<OrdenCompra> compras = seedOrdenesCompra(sucs, provs, usus);
        seedDetalleCompras(compras, prods);

        List<Venta> ventas = seedVentas(sucs, usus);
        seedDetalleVentas(ventas, prods);

        seedMovimientos(prods, sucs);

        List<Transferencia> transferencias = seedTransferencias(sucs, usus);
        seedDetalleTransferencias(transferencias, prods);
    }

    private void seedLogisticData(List<Sucursal> sucs, List<Usuario> usus) {
        log.info("  - Sembrando datos de Logística...");
        List<Transportista> transportistas = seedTransportistas();
        List<Ruta> rutas = seedRutas(sucs);
        
        // Necesitamos las transferencias para los envíos, las recuperamos del repo
        List<Transferencia> transferencias = transferenciaRepository.findAll();
        seedEnvios(transferencias, transportistas, rutas);
    }

    private void seedAuditData(List<Usuario> usus) {
        log.info("  - Sembrando datos de Auditoría...");
        seedNotificaciones(usus);
        seedAuditoria();
    }

    private void seedRolEntidades() {
        if (rolEntidadRepository.count() == 0) {
            String[] roles = { "ADMINISTRADOR", "VENDEDOR", "ALMACENERO", "GERENTE", "COORDINADOR" };
            for (String r : roles) {
                rolEntidadRepository.save(RolEntidad.builder().nombreRol(r).build());
            }
            log.info("  - Roles inicializados.");
        }
    }

    private List<Sucursal> seedSucursales() {
        if (sucursalRepository.count() == 0) {
            List<Sucursal> list = Arrays.asList(
                    Sucursal.builder().nombre("Sede Principal").direccion("Calle 100 #15-20").ciudad("Bogotá")
                            .activo(true).build(),
                    Sucursal.builder().nombre("Sucursal Norte").direccion("Av 19 #145-30").ciudad("Bogotá").activo(true)
                            .build(),
                    Sucursal.builder().nombre("Sucursal Medellín").direccion("Carrera 80 #45-10").ciudad("Medellín")
                            .activo(true).build(),
                    Sucursal.builder().nombre("Sucursal Cali").direccion("Calle 5 #20-10").ciudad("Cali").activo(true)
                            .build(),
                    Sucursal.builder().nombre("Sucursal Barranquilla").direccion("Vía 40 #70-80").ciudad("Barranquilla")
                            .activo(true).build());
            return sucursalRepository.saveAll(list);
        }
        return sucursalRepository.findAll();
    }

    private void seedUnidadesMedida() {
        if (unidadMedidaRepository.count() == 0) {
            unidadMedidaRepository.saveAll(Arrays.asList(
                    UnidadMedida.builder().nombre("Unidad").abreviatura("UND").esUnidadBase(true)
                            .factorConversion(BigDecimal.ONE).build(),
                    UnidadMedida.builder().nombre("Kilogramo").abreviatura("KG").esUnidadBase(true)
                            .factorConversion(BigDecimal.ONE).build(),
                    UnidadMedida.builder().nombre("Caja x 12").abreviatura("CJ12").esUnidadBase(false)
                            .factorConversion(new BigDecimal("12")).build(),
                    UnidadMedida.builder().nombre("Litro").abreviatura("LT").esUnidadBase(true)
                            .factorConversion(BigDecimal.ONE).build(),
                    UnidadMedida.builder().nombre("Paquete").abreviatura("PQ").esUnidadBase(true)
                            .factorConversion(BigDecimal.ONE).build()));
            log.info("  - Unidades de medida inicializadas.");
        }
    }

    private List<Proveedor> seedProveedores() {
        if (proveedorRepository.count() == 0) {
            List<Proveedor> list = Arrays.asList(
                    Proveedor.builder().nitRut("900.123.456-1").razonSocial("Tech Global Solutions")
                            .contacto("Carlos Pérez").email("ventas@techglobal.com").activo(true).build(),
                    Proveedor.builder().nitRut("900.234.567-2").razonSocial("Distribuidora Oficina Express")
                            .contacto("Ana Martínez").email("ana.m@oficinaex.com").activo(true).build(),
                    Proveedor.builder().nitRut("900.345.678-3").razonSocial("Logística & suministros S.A.")
                            .contacto("Juan David").email("juan.d@logsum.com").activo(true).build(),
                    Proveedor.builder().nitRut("900.456.789-4").razonSocial("Importaciones Mundo Digital")
                            .contacto("Elena Gómez").email("elena@mundodigital.com").activo(true).build(),
                    Proveedor.builder().nitRut("900.567.890-5").razonSocial("Cómputo Veloz Ltda")
                            .contacto("Roberto Díaz").email("rdiaz@computoveloz.com").activo(true).build());
            return proveedorRepository.saveAll(list);
        }
        return proveedorRepository.findAll();
    }

    private List<Usuario> seedUsuarios(List<Sucursal> sucursales) {
        if (userRepository.count() <= 1) { // 1 es el admin creado anteriormente
            List<Usuario> list = new ArrayList<>();
            String pass = passwordEncoder.encode("password123");

            // Asignamos sucursales de forma secuencial para pruebas consistentes
            Sucursal sedePrincipal = sucursales.get(0);
            Sucursal sucursalNorte = sucursales.size() > 1 ? sucursales.get(1) : sedePrincipal;
            Sucursal sucursalMedellin = sucursales.size() > 2 ? sucursales.get(2) : sedePrincipal;

            list.add(Usuario.builder().nombre("Juan").apellido("Vendedor").correo("juan.vendedor@inventario.com")
                    .contrasena(pass).rol(Rol.MANAGER).sucursalAsignadaId(sedePrincipal.getId()).activo(true).build());
            list.add(Usuario.builder().nombre("María").apellido("Almacén").correo("maria.almacen@inventario.com")
                    .contrasena(pass).rol(Rol.OPERATOR).sucursalAsignadaId(sucursalNorte.getId()).activo(true).build());
            list.add(Usuario.builder().nombre("Andrés").apellido("Admin").correo("andres.admin@inventario.com")
                    .contrasena(pass).rol(Rol.ADMIN).sucursalAsignadaId(sedePrincipal.getId()).activo(true).build());
            list.add(Usuario.builder().nombre("Sofía").apellido("Comercial").correo("sofia.vendedor@inventario.com")
                    .contrasena(pass).rol(Rol.MANAGER).sucursalAsignadaId(sucursalMedellin.getId()).activo(true).build());
            list.add(Usuario.builder().nombre("Pedro").apellido("Logística").correo("pedro.almacen@inventario.com")
                    .contrasena(pass).rol(Rol.OPERATOR).sucursalAsignadaId(sucursalNorte.getId()).activo(true).build());

            return userRepository.saveAll(list);
        }
        return userRepository.findAll();
    }

    private List<Producto> seedProductos() {
        if (productoRepository.count() == 0) {
            List<Producto> list = Arrays.asList(
                    Producto.builder().nombre("Laptop Dell Latitude 5420").sku("LAP-DELL-5420")
                            .descripcion("Intel i7-1185G7, 16GB RAM, 512GB SSD, 14\" FHD").unidadMedidaBase("UND")
                            .precioCostoPromedio(new BigDecimal("3200000")).activo(true).build(),
                    Producto.builder().nombre("Monitor Samsung Odyssey G5").sku("MON-SAM-G5-27")
                            .descripcion("27\" QHD, 144Hz, curvo 1000R").unidadMedidaBase("UND")
                            .precioCostoPromedio(new BigDecimal("1200000")).activo(true).build(),
                    Producto.builder().nombre("Teclado Logitech G Pro X").sku("TEC-LOG-GPROX")
                            .descripcion("Mecánico, Switches GX Blue Clicky, RGB").unidadMedidaBase("UND")
                            .precioCostoPromedio(new BigDecimal("450000")).activo(true).build(),
                    Producto.builder().nombre("Impresora HP LaserJet Pro M404dw").sku("IMP-HP-M404DW")
                            .descripcion("Láser Monocromática, Duplex, WiFi").unidadMedidaBase("UND")
                            .precioCostoPromedio(new BigDecimal("950000")).activo(true).build(),
                    Producto.builder().nombre("SSD Externo Samsung T7 1TB").sku("SSD-SAM-T7-1TB")
                            .descripcion("Hasta 1050MB/s, USB 3.2 Gen 2, Titan Gray").unidadMedidaBase("UND")
                            .precioCostoPromedio(new BigDecimal("550000")).activo(true).build(),
                    Producto.builder().nombre("Mouse Razer DeathAdder V2").sku("MOU-RAZ-DV2")
                            .descripcion("Ergonómico, 20K DPI Optical Sensor").unidadMedidaBase("UND")
                            .precioCostoPromedio(new BigDecimal("280000")).activo(false).build()); // Inactivo globalmente
            return productoRepository.saveAll(list);
        }
        return productoRepository.findAll();
    }

    private void seedInventario(List<Sucursal> sucursales, List<Producto> productos) {
        if (inventarioRepository.count() == 0 && !sucursales.isEmpty() && !productos.isEmpty()) {
            Random rand = new Random();
            for (Sucursal s : sucursales) {
                for (Producto p : productos) {
                    // Lógica de coherencia:
                    // 1. Si el producto ya está inactivo globalmente, queda inactivo en la sucursal.
                    // 2. De lo contrario, activamos el producto en la sucursal, 
                    //    EXCEPTO en un ~20% de casos aleatorios para pruebas de filtrado.
                    boolean estaActivoEnSucursal = p.getActivo() && (rand.nextInt(10) > 1);

                    inventarioRepository.save(Inventario.builder()
                            .sucursal(s)
                            .producto(p)
                            .stock(new BigDecimal(rand.nextInt(100) + 10))
                            .stockMinimo(new BigDecimal(5))
                            .activo(estaActivoEnSucursal)
                            .build());
                }
            }
            log.info("  - Inventario por sucursal inicializado (con varianza de estado activo/inactivo).");
        }
    }

    private void seedProductoProveedores(List<Producto> productos, List<Proveedor> proveedores) {
        if (productoProveedorRepository.count() == 0 && !productos.isEmpty() && !proveedores.isEmpty()) {
            Random rand = new Random();
            for (Producto p : productos) {
                // Cada producto lo surten al menos 2 proveedores
                for (int i = 0; i < 2; i++) {
                    Proveedor prov = proveedores.get(rand.nextInt(proveedores.size()));
                    productoProveedorRepository.save(ProductoProveedor.builder()
                            .productoId(p.getId())
                            .proveedorId(prov.getId())
                            .precioCompra(p.getPrecioCostoPromedio().multiply(new BigDecimal("0.95")))
                            .leadTimeDias(rand.nextInt(5) + 1)
                            .fechaVigenciaDesde(LocalDate.now())
                            .build());
                }
            }
        }
    }

    private List<OrdenCompra> seedOrdenesCompra(List<Sucursal> sucs, List<Proveedor> provs, List<Usuario> usus) {
        if (ordenCompraRepository.count() == 0 && !provs.isEmpty()) {
            List<OrdenCompra> list = new ArrayList<>();
            Random rand = new Random();
            for (int i = 1; i <= 5; i++) {
                list.add(OrdenCompra.builder()
                        .sucursalDestinoId(sucs.get(rand.nextInt(sucs.size())).getId())
                        .proveedorId(provs.get(rand.nextInt(provs.size())).getId())
                        .usuarioResponsableId(usus.get(rand.nextInt(usus.size())).getId())
                        .fechaCompra(LocalDateTime.now().minusDays(i))
                        .total(BigDecimal.ZERO)
                        .estado("COMPLETADA")
                        .plazoPagoDias(30)
                        .build());
            }
            return ordenCompraRepository.saveAll(list);
        }
        return ordenCompraRepository.findAll();
    }

    private void seedDetalleCompras(List<OrdenCompra> compras, List<Producto> prods) {
        if (detalleCompraRepository.count() == 0 && !compras.isEmpty()) {
            for (OrdenCompra c : compras) {
                BigDecimal total = BigDecimal.ZERO;
                for (int i = 0; i < 2; i++) {
                    Producto p = prods.get(i);
                    BigDecimal cant = new BigDecimal("10");
                    BigDecimal precio = p.getPrecioCostoPromedio();
                    detalleCompraRepository.save(DetalleCompra.builder()
                            .ordenCompraId(c.getId())
                            .productoId(p.getId())
                            .cantidadSolicitada(cant)
                            .precioUnitario(precio)
                            .build());
                    total = total.add(cant.multiply(precio));
                }
                c.setTotal(total);
                ordenCompraRepository.save(c);
            }
        }
    }

    private List<Venta> seedVentas(List<Sucursal> sucs, List<Usuario> usus) {
        if (ventaRepository.count() == 0 && !sucs.isEmpty()) {
            List<Venta> list = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                list.add(Venta.builder()
                        .sucursalId(sucs.get(i % sucs.size()).getId())
                        .vendedorId(usus.get(i % usus.size()).getId())
                        .fechaVenta(new Date())
                        .total(BigDecimal.ZERO)
                        .comprobanteOriginal("FAC-00" + i)
                        .build());
            }
            return ventaRepository.saveAll(list);
        }
        return ventaRepository.findAll();
    }

    private void seedDetalleVentas(List<Venta> ventas, List<Producto> prods) {
        if (detalleVentaRepository.count() == 0 && !ventas.isEmpty()) {
            for (Venta v : ventas) {
                BigDecimal total = BigDecimal.ZERO;
                for (int i = 0; i < 2; i++) {
                    Producto p = prods.get(i);
                    BigDecimal cant = new BigDecimal("1");
                    BigDecimal precio = p.getPrecioCostoPromedio().multiply(new BigDecimal("1.3"));
                    detalleVentaRepository.save(DetalleVenta.builder()
                            .ventaId(v.getId())
                            .productoId(p.getId())
                            .cantidad(cant)
                            .precioUnitario(precio)
                            .descuentoAplicado(BigDecimal.ZERO)
                            .build());
                    total = total.add(cant.multiply(precio));
                }
                v.setTotal(total);
                ventaRepository.save(v);
            }
        }
    }

    private void seedMovimientos(List<Producto> prods, List<Sucursal> sucursales) {
        if (movimientoRepository.count() == 0 && !prods.isEmpty()) {
            for (int i = 0; i < prods.size(); i++) {
                movimientoRepository.save(MovimientoInventario.builder()
                        .productoId(prods.get(i).getId())
                        .sucursalId(sucursales.get(0).getId())
                        .tipo(TipoMovimiento.ENTRADA_COMPRA)
                        .cantidad(new BigDecimal("100"))
                        .fechaMovimiento(LocalDateTime.now().minusDays(10))
                        .motivo("Carga inicial de inventario")
                        .build());
            }
        }
    }

    private List<Transportista> seedTransportistas() {
        if (transportistaRepository.count() == 0) {
            return transportistaRepository.saveAll(Arrays.asList(
                    Transportista.builder().nombre("Servientrega").nit("860.000.123-1").contacto("Oficina Central")
                            .activo(true).build(),
                    Transportista.builder().nombre("Coordinadora").nit("860.000.456-2").contacto("Soporte Logístico")
                            .activo(true).build(),
                    Transportista.builder().nombre("Envía").nit("860.000.789-3").contacto("Atención Cliente")
                            .activo(true).build(),
                    Transportista.builder().nombre("FedEx Nacional").nit("860.000.321-4").contacto("Import/Export")
                            .activo(true).build(),
                    Transportista.builder().nombre("DHT S.A.").nit("860.000.654-5").contacto("Transporte Pesado")
                            .activo(true).build()));
        }
        return transportistaRepository.findAll();
    }

    private List<Ruta> seedRutas(List<Sucursal> sucs) {
        if (rutaRepository.count() == 0 && sucs.size() >= 2) {
            return rutaRepository.saveAll(Arrays.asList(
                    Ruta.builder().sucursalOrigenId(sucs.get(0).getId()).sucursalDestinoId(sucs.get(1).getId())
                            .tipoClasificacion(ClasificacionRuta.TIEMPO).leadTimeEstandar(4).build(),
                    Ruta.builder().sucursalOrigenId(sucs.get(0).getId()).sucursalDestinoId(sucs.get(2).getId())
                            .tipoClasificacion(ClasificacionRuta.COSTO).leadTimeEstandar(12).build(),
                    Ruta.builder().sucursalOrigenId(sucs.get(1).getId()).sucursalDestinoId(sucs.get(0).getId())
                            .tipoClasificacion(ClasificacionRuta.PRIORIDAD).leadTimeEstandar(2).build(),
                    Ruta.builder().sucursalOrigenId(sucs.get(2).getId()).sucursalDestinoId(sucs.get(3).getId())
                            .tipoClasificacion(ClasificacionRuta.TIEMPO).leadTimeEstandar(8).build(),
                    Ruta.builder().sucursalOrigenId(sucs.get(0).getId()).sucursalDestinoId(sucs.get(4).getId())
                            .tipoClasificacion(ClasificacionRuta.COSTO).leadTimeEstandar(24).build()));
        }
        return rutaRepository.findAll();
    }

    private void seedEnvios(List<Transferencia> transferencias, List<Transportista> trans, List<Ruta> ruts) {
        if (envioRepository.count() == 0 && !transferencias.isEmpty() && !trans.isEmpty() && !ruts.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                envioRepository.save(Envio.builder()
                        .transferencia(transferencias.get(i))
                        .transportistaId(trans.get(i).getId())
                        .rutaId(ruts.get(i).getId())
                        .estado(EstadoLogistico.EN_TRANSITO)
                        .fechaDespacho(LocalDateTime.now())
                        .build());
            }
        }
    }

    private List<Transferencia> seedTransferencias(List<Sucursal> sucs, List<Usuario> usus) {
        if (transferenciaRepository.count() == 0 && sucs.size() >= 2) {
            List<Transferencia> list = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                list.add(Transferencia.builder()
                        .sucursalOrigenId(sucs.get(0).getId())
                        .sucursalDestinoId(sucs.get(1).getId())
                        .usuarioSolicitaId(usus.get(0).getId())
                        .fechaSolicitud(LocalDateTime.now())
                        .estado(EstadoLogistico.EN_TRANSITO.name())
                        .build());
            }
            return transferenciaRepository.saveAll(list);
        }
        return transferenciaRepository.findAll();
    }

    private void seedDetalleTransferencias(List<Transferencia> transfs, List<Producto> prods) {
        if (detalleTransferenciaRepository.count() == 0 && !transfs.isEmpty() && !prods.isEmpty()) {
            for (Transferencia t : transfs) {
                // Transferimos los primeros 2 productos
                for (int i = 0; i < Math.min(2, prods.size()); i++) {
                    detalleTransferenciaRepository.save(DetalleTransferencia.builder()
                            .transferencia(t)
                            .productoId(prods.get(i).getId())
                            .cantidadSolicitada(new BigDecimal(5 + i))
                            .build());
                }
            }
        }
    }

    private void seedNotificaciones(List<Usuario> usus) {
        if (notificacionRepository.count() == 0 && !usus.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                notificacionRepository.save(NotificacionEvento.builder()
                        .usuarioResponsable(usus.get(0).getNombre())
                        .mensaje("Stock bajo para producto ID: " + (i + 1))
                        .tipoEvento("ALERTA_STOCK")
                        .fechaRegistro(LocalDateTime.now())
                        .build());
            }
        }
    }

    private void seedAuditoria() {
        if (auditoriaRepository.count() == 0) {
            for (int i = 0; i < 5; i++) {
                RegistroAuditoria reg = RegistroAuditoria.builder()
                        .usuario("admin")
                        .entidad("Producto")
                        .accion("CREATE")
                        .detalles("Creación inicial de productos")
                        .fechaHora(LocalDateTime.now())
                        .build();
                auditoriaRepository.save(reg);
            }
        }
    }
}
