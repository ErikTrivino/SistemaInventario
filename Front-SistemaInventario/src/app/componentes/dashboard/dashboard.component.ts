import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableroService } from '../../servicios/tablero.service';
import { AuditoriaService } from '../../servicios/auditoria.service';
import { ReporteService } from '../../servicios/reporte.service';
import { MensajeDTO } from '../../modelo/mensaje-dto';
import {
  ChartComponent,
  ApexAxisChartSeries,
  ApexChart,
  ApexXAxis,
  ApexDataLabels,
  ApexTitleSubtitle,
  ApexStroke,
  ApexGrid,
  ApexYAxis,
  ApexLegend,
  NgApexchartsModule,
  ApexPlotOptions,
  ApexFill,
  ApexTooltip
} from 'ng-apexcharts';

export type ChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  xaxis: ApexXAxis;
  yaxis: ApexYAxis;
  dataLabels: ApexDataLabels;
  grid: ApexGrid;
  stroke: ApexStroke;
  title: ApexTitleSubtitle;
  colors: string[];
  legend: ApexLegend;
  plotOptions: ApexPlotOptions;
  fill: ApexFill;
  tooltip: ApexTooltip;
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NgApexchartsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  @ViewChild('chart') chart!: ChartComponent;
  
  resumen: any = {
    totalProductos: 0,
    bajoStock: 0,
    transferenciasPendientes: 0,
    ventasHoy: 0
  };

  actividades: any[] = [];
  alertasStock: any[] = [];

  // Configuración de gráficos
  chartVentas: Partial<ChartOptions> = {};
  chartProductos: Partial<ChartOptions> = {};
  chartMovimientos: any = {}; // Usamos any para simplificar con donut (number[])

  constructor(
    private tableroService: TableroService,
    private auditoriaService: AuditoriaService,
    private reporteService: ReporteService
  ) { }

  ngOnInit(): void {
    this.initCharts();
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargarResumen();
    this.cargarActividadReciente();
    this.cargarAlertasStock();
    this.cargarDatosGraficos();
  }

  cargarResumen(): void {
    this.tableroService.getResumenDiario().subscribe({
      next: (data: MensajeDTO) => {
        if (!data.error && data.respuesta) {
          this.resumen = data.respuesta;
        }
      }
    });
  }

  cargarActividadReciente(): void {
    this.auditoriaService.getAuditLogs(0, 8).subscribe({
      next: (data: MensajeDTO) => {
        if (!data.error && data.respuesta) {
          // Si la respuesta es una página, tomamos el contenido
          this.actividades = data.respuesta.content || data.respuesta;
        }
      }
    });
  }

  cargarAlertasStock(): void {
    this.tableroService.getAlertasStock(0, 5).subscribe({
      next: (data: MensajeDTO) => {
        if (!data.error && data.respuesta) {
          this.alertasStock = data.respuesta.content || data.respuesta;
        }
      }
    });
  }

  cargarDatosGraficos(): void {
    const hoy = new Date();
    const hace7dias = new Date();
    hace7dias.setDate(hoy.getDate() - 7);

    const inicio = hace7dias.toISOString().split('T')[0];
    const fin = hoy.toISOString().split('T')[0];

    // Ventas última semana
    this.reporteService.generarReporteVentas(inicio, fin).subscribe({
      next: (data: MensajeDTO) => {
        if (!data.error && data.respuesta) {
          this.actualizarGraficoVentas(data.respuesta);
        }
      }
    });

    // Productos más vendidos (Top 5 del mes actual)
    const mesActual = hoy.getMonth() + 1;
    const anioActual = hoy.getFullYear();
    this.reporteService.generarAnalisisRotacion(mesActual, anioActual, 0, 5).subscribe({
      next: (data: MensajeDTO) => {
        if (!data.error && data.respuesta) {
          this.actualizarGraficoProductos(data.respuesta.content || data.respuesta);
        }
      }
    });

    // Movimientos de inventario (Ejemplo con datos de transferencia)
    this.tableroService.getMetricasTransferencias().subscribe({
      next: (data: MensajeDTO) => {
        if (!data.error && data.respuesta) {
          this.actualizarGraficoMovimientos(data.respuesta);
        }
      }
    });
  }

  private initCharts(): void {
    // Inicializar con esqueletos o datos vacíos
    this.chartVentas = {
      series: [{ name: "Ventas", data: [0, 0, 0, 0, 0, 0, 0] }],
      chart: { height: 350, type: "area", toolbar: { show: false }, zoom: { enabled: false } },
      dataLabels: { enabled: false },
      stroke: { curve: "smooth", width: 3 },
      xaxis: { categories: ["Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"] },
      colors: ["#3b82f6"], // Blue 500
      fill: { type: "gradient", gradient: { shadeIntensity: 1, opacityFrom: 0.7, opacityTo: 0.3, stops: [0, 90, 100] } }
    };

    this.chartProductos = {
      series: [{ name: "Ventas", data: [0, 0, 0, 0, 0] }],
      chart: { height: 350, type: "bar", toolbar: { show: false } },
      plotOptions: { bar: { borderRadius: 4, horizontal: true } },
      dataLabels: { enabled: true },
      xaxis: { categories: ["P1", "P2", "P3", "P4", "P5"] },
      colors: ["#10b981"] // Emerald 500
    };

    this.chartMovimientos = {
      series: [44, 55, 13, 33],
      chart: { height: 350, type: "donut" },
      labels: ["Completadas", "Pendientes", "En Tránsito", "Discrepancias"],
      colors: ["#10b981", "#f59e0b", "#3b82f6", "#ef4444"],
      legend: { position: "bottom" }
    };
  }

  private actualizarGraficoVentas(data: any): void {
    // Aquí procesaríamos los datos reales para el gráfico de líneas
    // Por ahora simulamos con base en totalVentas si no viene el desglose
    this.chartVentas.series = [{
      name: "Ventas ($)",
      data: [31, 40, 28, 51, 42, 109, 100] // Ejemplo, en prod usar data real
    }];
  }

  private actualizarGraficoProductos(productos: any[]): void {
    if (productos && productos.length > 0) {
      this.chartProductos.series = [{
        name: "Unidades",
        data: productos.map(p => p.totalSalidas)
      }];
      this.chartProductos.xaxis = {
        categories: productos.map(p => p.nombreProducto)
      };
    }
  }

  private actualizarGraficoMovimientos(metricas: any): void {
    if (metricas) {
      this.chartMovimientos.series = [
        metricas.completadas || 0,
        metricas.pendientes || 0,
        metricas.enTransito || 0,
        metricas.conDiscrepancias || 0
      ];
    }
  }

  getIconForActivity(tipo: string): string {
    switch (tipo?.toLowerCase()) {
      case 'venta': return 'fa-shopping-cart text-green-500';
      case 'transferencia': return 'fa-exchange-alt text-blue-500';
      case 'ajuste': return 'fa-tools text-orange-500';
      case 'login': return 'fa-user-check text-purple-500';
      default: return 'fa-info-circle text-gray-500';
    }
  }
}
