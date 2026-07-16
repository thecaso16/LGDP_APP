# Plan de Documentación: Especificaciones de Casos de Uso (Versión Final)

Este plan detalla la reestructuración de los Casos de Uso (CU) alineados con la implementación real del proyecto y los objetivos estratégicos proporcionados.

## Objetivos del Proyecto
- **Objetivo 1 (O1):** Reducir en 40% el tiempo de toma de pedidos y despacho (Digitalización vs. Papel).
- **Objetivo 2 (O2):** Reducir a 0% el descuadre financiero (Control de pagos y balances automáticos).

## 1. Matriz de Trazabilidad de Casos de Uso

| Código | Caso de Uso | Relación / Tipo | Objetivo Vinculado | Justificación en el Código |
| :--- | :--- | :--- | :--- | :--- |
| **CU01** | Iniciar Sesión | Principal | - | Implementado en `LoginViewModel` con Firebase Auth. |
| **CU02** | Gestionar Pedido / Comanda | Principal | **O1** | `PedidoViewModel` y `PedidoScreen` reemplazan el papel y lápiz. |
| **CU03** | Consultar Estado de Mesa | Principal | **O1** | `SalonScreen` permite ver mesas Libres/Ocupadas en tiempo real. |
| **CU04** | Gestionar flujo de Caja (Apertura/Cierre) | Principal | **O2** | `CajaViewModel` controla los montos de entrada y salida. |
| **CU05** | Registrar Pago de Pedido | Principal | **O2** | Maneja Efectivo, Yape/Plin e Izipay para evitar vacíos (O2). |
| **CU06** | Sincronizar Datos (Online/Offline) | Transversal | **O1 / O2** | `SyncManager` asegura que la información no se pierda ni se duplique. |
| **CU07** | Gestionar Catálogo e Inventario | Principal | **O1** | Administración de productos e insumos para disponibilidad inmediata. |
| **CU08** | Visualizar Reportes de Desempeño | Principal | **O1 / O2** | Análisis de tiempos de atención y cuadres históricos. |
| **CU09** | Validar Credenciales | `<<include>>` de CU01 | - | Proceso interno de Firebase Auth. |
| **CU10** | Validar Stock Disponible | `<<include>>` de CU02 | **O1** | El sistema impide pedir productos sin stock (`PedidoViewModel`). |
| **CU11** | Seleccionar Método de Pago | `<<include>>` de CU05 | **O2** | Obliga a clasificar el ingreso para el balance final. |
| **CU12** | Justificar Descuadre de Caja | `<<extend>>` de CU04 | **O2** | Se activa si la diferencia en `finalizarCierre` no es cero. |

## 2. Detalle de Actores vs. Casos de Uso

| Actor | Responsabilidades Clave | Casos de Uso Asociados |
| :--- | :--- | :--- |
| **Trabajador** | Atención al cliente y rapidez en el servicio. | CU01, CU02, CU03, CU06, CU10 |
| **Cajero** | Integridad financiera y flujo de efectivo. | Todos los del Trabajador + CU04, CU05, CU11, CU12 |
| **Administrador** | Control total, gestión de recursos y auditoría. | Todos los anteriores + CU07, CU08 |

## 3. Propuesta de Diagrama (Lógica Mermaid)
Se presentará un diagrama que visualice:
- El **Trabajador** enfocado en el "Círculo de Velocidad" (O1).
- El **Cajero** enfocado en el "Círculo de Precisión" (O2).
- El **Administrador** supervisando ambos.

## Próximos Pasos
1. Generar la tabla de especificaciones detallada (Actor, Pre-condición, Flujo).
2. Proporcionar el código Mermaid para el diagrama.
