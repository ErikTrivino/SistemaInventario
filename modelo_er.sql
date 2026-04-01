-- ==========================================================
-- SCRIPT DE MODELO ENTIDAD-RELACIÓN - SISTEMA DE INVENTARIO
-- Motor: MySQL 8.x
-- Versión: 3.0 (Módulo de Gestión de Usuarios y Logística)
-- ==========================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema optiplant_db
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `optiplant_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `optiplant_db`;

-- -----------------------------------------------------
-- Table `sucursales`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sucursales` (
  `id_sucursal` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  `direccion` VARCHAR(255) NULL,
  `ciudad` VARCHAR(100) NULL,
  `estado_activo` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_sucursal`)
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `roles`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `roles` (
  `id_rol` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_rol` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id_rol`)
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `usuarios`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `usuarios` (
  `id_usuario` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(255) NOT NULL,
  `apellido` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `roles_id_rol` BIGINT UNSIGNED NOT NULL,
  `id_sucursal_asignada` BIGINT UNSIGNED NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT 1,
  `motivo_inactivacion` TEXT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE,
  CONSTRAINT `fk_usuarios_sucursal`
    FOREIGN KEY (`id_sucursal_asignada`)
    REFERENCES `sucursales` (`id_sucursal`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_usuarios_rol`
    FOREIGN KEY (`roles_id_rol`)
    REFERENCES `roles` (`id_rol`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `productos`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `productos` (
  `id_producto` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(150) NOT NULL,
  `descripcion` TEXT NULL,
  `sku` VARCHAR(50) NULL,
  `unidad_medida_base` VARCHAR(20) NOT NULL,
  `precio_costo_promedio` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `activo` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_producto`),
  UNIQUE INDEX `sku_UNIQUE` (`sku` ASC) VISIBLE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `unidades_medida`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `unidades_medida` (
  `id_unidad_medida` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  `abreviatura` VARCHAR(20) NOT NULL,
  `id_producto` BIGINT UNSIGNED NULL,
  `es_unidad_base` TINYINT(1) NOT NULL DEFAULT 0,
  `factor_conversion` DECIMAL(10,4) NOT NULL DEFAULT 1.0000,
  PRIMARY KEY (`id_unidad_medida`),
  CONSTRAINT `fk_unidades_producto`
    FOREIGN KEY (`id_producto`)
    REFERENCES `productos` (`id_producto`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `inventario_sucursal`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `inventario_sucursal` (
  `id_producto` BIGINT UNSIGNED NOT NULL,
  `id_sucursal` BIGINT UNSIGNED NOT NULL,
  `stock_actual` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `stock_minimo` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`id_producto`, `id_sucursal`),
  CONSTRAINT `fk_inventario_producto`
    FOREIGN KEY (`id_producto`)
    REFERENCES `productos` (`id_producto`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_inventario_sucursal`
    FOREIGN KEY (`id_sucursal`)
    REFERENCES `sucursales` (`id_sucursal`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `proveedores`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `proveedores` (
  `id_proveedor` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nit_rut` VARCHAR(20) NOT NULL,
  `razon_social` VARCHAR(150) NOT NULL,
  `contacto` VARCHAR(100) NULL,
  `email` VARCHAR(120) NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_proveedor`),
  UNIQUE INDEX `nit_rut_UNIQUE` (`nit_rut` ASC) VISIBLE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `productos_proveedor`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `productos_proveedor` (
  `id_producto_proveedor` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_proveedor` BIGINT UNSIGNED NOT NULL,
  `id_producto` BIGINT UNSIGNED NOT NULL,
  `precio_compra` DECIMAL(15,2) NOT NULL,
  `descuento_proveedor` DECIMAL(5,2) NULL DEFAULT 0.00,
  `lead_time_dias` INT NOT NULL,
  `fecha_vigencia_desde` DATE NULL,
  PRIMARY KEY (`id_producto_proveedor`),
  CONSTRAINT `fk_prodprov_proveedor`
    FOREIGN KEY (`id_proveedor`)
    REFERENCES `proveedores` (`id_proveedor`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_prodprov_producto`
    FOREIGN KEY (`id_producto`)
    REFERENCES `productos` (`id_producto`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `ordenes_compra`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ordenes_compra` (
  `id_compra` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_sucursal_destino` BIGINT UNSIGNED NULL,
  `id_proveedor` BIGINT UNSIGNED NOT NULL,
  `id_usuario_responsable` BIGINT UNSIGNED NULL,
  `fecha_compra` DATETIME NOT NULL,
  `total` DECIMAL(15,2) NULL,
  `estado` VARCHAR(20) NOT NULL,
  `plazo_pago_dias` INT NULL,
  PRIMARY KEY (`id_compra`),
  CONSTRAINT `fk_compra_sucursal`
    FOREIGN KEY (`id_sucursal_destino`)
    REFERENCES `sucursales` (`id_sucursal`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_compra_proveedor`
    FOREIGN KEY (`id_proveedor`)
    REFERENCES `proveedores` (`id_proveedor`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_compra_usuario`
    FOREIGN KEY (`id_usuario_responsable`)
    REFERENCES `usuarios` (`id_usuario`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `detalles_compra`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `detalles_compra` (
  `id_detalle` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_orden_compra` BIGINT UNSIGNED NOT NULL,
  `id_producto` BIGINT UNSIGNED NOT NULL,
  `cantidad_solicitada` DECIMAL(12,2) NOT NULL,
  `cantidad_recibida` DECIMAL(12,2) NULL,
  `precio_unitario` DECIMAL(15,2) NOT NULL,
  `descuento_aplicado` DECIMAL(5,2) NULL DEFAULT 0.00,
  PRIMARY KEY (`id_detalle`),
  CONSTRAINT `fk_detcompra_orden`
    FOREIGN KEY (`id_orden_compra`)
    REFERENCES `ordenes_compra` (`id_compra`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_detcompra_producto`
    FOREIGN KEY (`id_producto`)
    REFERENCES `productos` (`id_producto`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `ventas`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ventas` (
  `id_venta` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_sucursal` BIGINT UNSIGNED NOT NULL,
  `id_usuario_vendedor` BIGINT UNSIGNED NULL,
  `fecha_venta` DATETIME NOT NULL,
  `total_venta` DECIMAL(15,2) NULL,
  `comprobante_original` VARCHAR(255) NULL,
  PRIMARY KEY (`id_venta`),
  UNIQUE INDEX `comprobante_UNIQUE` (`comprobante_original` ASC) VISIBLE,
  CONSTRAINT `fk_venta_sucursal`
    FOREIGN KEY (`id_sucursal`)
    REFERENCES `sucursales` (`id_sucursal`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_venta_usuario`
    FOREIGN KEY (`id_usuario_vendedor`)
    REFERENCES `usuarios` (`id_usuario`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `detalles_venta`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `detalles_venta` (
  `id_detalle` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_venta` BIGINT UNSIGNED NOT NULL,
  `id_producto` BIGINT UNSIGNED NOT NULL,
  `cantidad` DECIMAL(12,2) NOT NULL,
  `precio_unitario` DECIMAL(15,2) NOT NULL,
  `descuento_aplicado` DECIMAL(5,2) NULL DEFAULT 0.00,
  `lista_precio_usada` VARCHAR(50) NULL,
  PRIMARY KEY (`id_detalle`),
  CONSTRAINT `fk_detventa_venta`
    FOREIGN KEY (`id_venta`)
    REFERENCES `ventas` (`id_venta`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_detventa_producto`
    FOREIGN KEY (`id_producto`)
    REFERENCES `productos` (`id_producto`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `transferencias`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `transferencias` (
  `id_transferencia` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_sucursal_origen` BIGINT UNSIGNED NOT NULL,
  `id_sucursal_destino` BIGINT UNSIGNED NOT NULL,
  `id_usuario_solicita` BIGINT UNSIGNED NULL,
  `id_gerente_aprueba` BIGINT UNSIGNED NULL,
  `estado` VARCHAR(30) NOT NULL,
  `fecha_solicitud` DATETIME NOT NULL,
  PRIMARY KEY (`id_transferencia`),
  CONSTRAINT `fk_transf_sucursal_orig`
    FOREIGN KEY (`id_sucursal_origen`)
    REFERENCES `sucursales` (`id_sucursal`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transf_sucursal_dest`
    FOREIGN KEY (`id_sucursal_destino`)
    REFERENCES `sucursales` (`id_sucursal`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transf_usuario_sol`
    FOREIGN KEY (`id_usuario_solicita`)
    REFERENCES `usuarios` (`id_usuario`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transf_usuario_apr`
    FOREIGN KEY (`id_gerente_aprueba`)
    REFERENCES `usuarios` (`id_usuario`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `detalles_transferencia`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `detalles_transferencia` (
  `id_detalle` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_transferencia` BIGINT UNSIGNED NOT NULL,
  `id_producto` BIGINT UNSIGNED NOT NULL,
  `cantidad_solicitada` DECIMAL(12,2) NOT NULL,
  `cantidad_confirmada` DECIMAL(12,2) NULL,
  `cantidad_recibida` DECIMAL(12,2) NULL,
  `motivo_diferencia` TEXT NULL,
  PRIMARY KEY (`id_detalle`),
  CONSTRAINT `fk_dettransf_transferencia`
    FOREIGN KEY (`id_transferencia`)
    REFERENCES `transferencias` (`id_transferencia`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_dettransf_producto`
    FOREIGN KEY (`id_producto`)
    REFERENCES `productos` (`id_producto`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `rutas`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `rutas` (
  `id_ruta` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_sucursal_origen` BIGINT UNSIGNED NOT NULL,
  `id_sucursal_destino` BIGINT UNSIGNED NOT NULL,
  `tipo_clasificacion` VARCHAR(30) NOT NULL,
  `lead_time_estandar` INT NOT NULL,
  PRIMARY KEY (`id_ruta`),
  CONSTRAINT `fk_ruta_sucursal_orig`
    FOREIGN KEY (`id_sucursal_origen`)
    REFERENCES `sucursales` (`id_sucursal`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_ruta_sucursal_dest`
    FOREIGN KEY (`id_sucursal_destino`)
    REFERENCES `sucursales` (`id_sucursal`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `transportistas`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `transportistas` (
  `id_transportista` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  `contacto` VARCHAR(150) NULL,
  `nit` VARCHAR(20) NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_transportista`),
  UNIQUE INDEX `nit_UNIQUE` (`nit` ASC) VISIBLE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `envios`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `envios` (
  `id_envio` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_transferencia` BIGINT UNSIGNED NOT NULL,
  `id_ruta` BIGINT UNSIGNED NULL,
  `id_transportista` BIGINT UNSIGNED NULL,
  `fecha_despacho` DATETIME NULL,
  `tiempo_estimado_entrega` INT NULL,
  `fecha_recepcion_real` DATETIME NULL,
  `estado_logistico` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id_envio`),
  UNIQUE INDEX `id_transferencia_UNIQUE` (`id_transferencia` ASC) VISIBLE,
  CONSTRAINT `fk_envio_transferencia`
    FOREIGN KEY (`id_transferencia`)
    REFERENCES `transferencias` (`id_transferencia`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_envio_ruta`
    FOREIGN KEY (`id_ruta`)
    REFERENCES `rutas` (`id_ruta`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_envio_transportista`
    FOREIGN KEY (`id_transportista`)
    REFERENCES `transportistas` (`id_transportista`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `historial_movimientos`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `historial_movimientos` (
  `id_movimiento` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tipo_movimiento` VARCHAR(20) NOT NULL,
  `cantidad` DECIMAL(12,2) NOT NULL,
  `fecha_movimiento` DATETIME NOT NULL,
  `id_usuario_responsable` BIGINT UNSIGNED NULL,
  `id_sucursal` BIGINT UNSIGNED NOT NULL,
  `id_producto` BIGINT UNSIGNED NOT NULL,
  `referencia_id` BIGINT UNSIGNED NULL,
  `motivo` TEXT NULL,
  PRIMARY KEY (`id_movimiento`),
  CONSTRAINT `fk_mov_sucursal`
    FOREIGN KEY (`id_sucursal`)
    REFERENCES `sucursales` (`id_sucursal`),
  CONSTRAINT `fk_mov_producto`
    FOREIGN KEY (`id_producto`)
    REFERENCES `productos` (`id_producto`),
  CONSTRAINT `fk_mov_usuario`
    FOREIGN KEY (`id_usuario_responsable`)
    REFERENCES `usuarios` (`id_usuario`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `registros_auditoria`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `notificaciones_eventos` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tipo_evento` VARCHAR(50) NOT NULL,
  `sucursal_id` BIGINT UNSIGNED NULL,
  `entidad_id` BIGINT UNSIGNED NULL,
  `mensaje` TEXT NOT NULL,
  `usuario_responsable` VARCHAR(100) NOT NULL,
  `fecha_registro` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_notif_tipo` (`tipo_evento` ASC),
  INDEX `idx_notif_usuario` (`usuario_responsable` ASC),
  INDEX `idx_notif_fecha` (`fecha_registro` ASC)
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `registros_auditoria`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `registros_auditoria` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_usuario` VARCHAR(255) NOT NULL,
  `accion` VARCHAR(50) NOT NULL,
  `entidad` VARCHAR(100) NOT NULL,
  `fecha_hora` DATETIME NOT NULL,
  `detalles` TEXT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- DATA INICIAL
-- -----------------------------------------------------
INSERT INTO `roles` (`nombre_rol`) VALUES ('ADMIN'), ('MANAGER'), ('OPERATOR');

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
