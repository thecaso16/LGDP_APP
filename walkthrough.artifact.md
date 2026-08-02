# Refinamiento de Interfaz: Categoría Predeterminada y Corrección de Contrastes

He realizado una serie de ajustes finales para asegurar que la pantalla de registro de pedidos sea totalmente dinámica y funcional en ambos modos, además de priorizar la categoría de productos más utilizada.

## Cambios realizados

### 1. Categoría Predeterminada
*   **Selección Inteligente:** Ahora, al entrar a la pantalla de "Registrar Pedido", el sistema busca automáticamente la categoría **"Sánguches"** (con o sin tilde) y la selecciona por defecto. Esto agiliza la toma de pedidos al mostrar lo más buscado de inmediato.

### 2. Corrección de Visibilidad en Modo Oscuro
*   **Elementos de la Lista:** He refactorizado la `FilaProductoCatalogo` para usar componentes nativos de Material 3 (`Surface`). Esto elimina definitivamente el fondo blanco que se veía en las capturas, asegurando que cada producto tenga un fondo oscuro y letras claras.
*   **Cabecera de Información:** Se ajustó la tarjeta de "Atendido por..." para que el texto sea perfectamente legible, usando colores que contrastan correctamente tanto en fondos claros como oscuros.
*   **Controles de Cantidad:** Los botones de sumar (+) y restar (-) ahora usan colores del esquema secundario y variantes de borde para resaltar mejor sobre el fondo.

### 3. Aligeramiento Visual
*   Se refinó el esquema de colores oscuros para que no se sienta tan saturado de azul fuerte, prefiriendo tonos de gris-azulados profundos para las superficies de las tarjetas.

## Verificación realizada

1.  **Navegación:** Se confirmó que "Sánguches" es ahora la pestaña activa al abrir el menú.
2.  **Modo Oscuro:** Se verificó visualmente que ya no existen bloques blancos fijos en la lista de productos.
3.  **Accesibilidad:** Se comprobó que las etiquetas de stock y descripciones sean legibles en ambos modos.

> [!TIP]
> Si en el futuro cambias el nombre de la categoría "Sánguches", el sistema detectará el cambio automáticamente mientras mantenga una palabra similar, o seleccionará la primera categoría disponible como respaldo.
