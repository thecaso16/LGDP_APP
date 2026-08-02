# Plan de Corrección: Estabilización del Modo Oscuro y Aligeramiento de Color

He analizado las capturas de pantalla y el código actual. El problema es que muchas pantallas aún utilizan colores fijos (blanco, gris claro y azul oscuro) que no cambian al activar el modo oscuro. Esto causa que el texto sea invisible o que la app se vea "rota" visualmente. Además, el exceso de azul en modo oscuro puede resultar pesado para la vista.

## User Review Required

> [!IMPORTANT]
> **Sobre el exceso de azul:** En modo oscuro, la **barra inferior (navegación)** dejará de ser azul y pasará a ser un tono gris oscuro/negro estándar. Mantendré la **cabecera superior** azul para conservar tu identidad de marca, pero esto liberará mucho peso visual.

## Proposed Changes

### Ajustes de Tema y Contenedores

#### [MODIFY] [MainContainerScreen.kt](file:///C:/Users/carlo/StudioProjects/LGDP_APP/app/src/main/java/com/lasgalletasdepau/lgdp_app/ui/main/MainContainerScreen.kt) y [AdminContainerScreen.kt](file:///C:/Users/carlo/StudioProjects/LGDP_APP/app/src/main/java/com/lasgalletasdepau/lgdp_app/ui/main/AdminContainerScreen.kt)
*   Asegurar que la `NavigationBar` use un fondo neutro en Modo Oscuro.
*   Mantener la `TopAppBar` azul (`NavyBrand`) para identidad visual.

### Refactorización de Pantallas (Limpieza de Colores Fijos)

Realizaré una limpieza profunda en los archivos de UI para reemplazar colores "hardcoded" por referencias dinámicas del `MaterialTheme`:

#### [MODIFY] [GestionCatalogoScreen.kt](file:///C:/Users/carlo/StudioProjects/LGDP_APP/app/src/main/java/com/lasgalletasdepau/lgdp_app/ui/admin/GestionCatalogoScreen.kt)
*   Eliminar fondos fijos `Color(0xFFF8FAFC)`.
*   Ajustar tarjetas de productos y chips de filtrado.

#### [MODIFY] [AdminHistorialScreen.kt](file:///C:/Users/carlo/StudioProjects/LGDP_APP/app/src/main/java/com/lasgalletasdepau/lgdp_app/ui/admin/AdminHistorialScreen.kt)
*   Corregir colores del `Scaffold` y `TabRow`.
*   Ajustar etiquetas de estado (En Cocina, Cobrada) para que tengan contraste en fondo oscuro.

#### [MODIFY] [GestionInventarioScreen.kt](file:///C:/Users/carlo/StudioProjects/LGDP_APP/app/src/main/java/com/lasgalletasdepau/lgdp_app/ui/admin/GestionInventarioScreen.kt)
*   Reemplazar el azul oscuro fijo de los botones e indicadores por `MaterialTheme.colorScheme.primary`.

#### [MODIFY] [ReportesTrabajadoresScreen.kt](file:///C:/Users/carlo/StudioProjects/LGDP_APP/app/src/main/java/com/lasgalletasdepau/lgdp_app/ui/pedidos/ReportesTrabajadoresScreen.kt)
*   Asegurar que los diálogos de selección de fecha sean legibles.

## Verification Plan

### Manual Verification
1.  **Legibilidad:** Verificar en cada pantalla que el texto sea blanco/gris claro sobre los fondos oscuros.
2.  **Formularios:** Asegurar que los campos de texto (`OutlinedTextField`) sean visibles y el texto que se escribe no sea negro sobre fondo oscuro.
3.  **Peso Visual:** Confirmar que al cambiar la barra inferior a negro/gris, la app se sienta menos saturada de azul en modo oscuro.
