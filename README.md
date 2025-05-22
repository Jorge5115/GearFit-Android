# Aplicación de Seguimiento Nutricional y Actividad Física

**GearFit** es una aplicación móvil desarrollada en Android Studio que permite a los usuarios registrar y analizar su alimentación diaria y su actividad física. Utiliza sensores nativos y tecnologías modernas para proporcionar una experiencia precisa y personalizada sin depender de servicios externos.

---

## 🚀 Características Principales

- **📋 Registro de comidas**
  - Añade alimentos desde una lista personalizada o escaneando su código de barras.
  - Registra calorías, grasas, carbohidratos y proteínas por cada 100g.
  
- **📆 Historial semanal**
  - Visualización de comidas y calorías de los últimos 7 días.
  - Opción para editar o eliminar registros.

- **🚶 Conteo de pasos**
  - Integración con los sensores nativos de Android a través de `SensorManager`.
  - Registro automático del número de pasos diarios, sin uso de librerías externas.

- **📷 Escaneo de código de barras**
  - Uso de `CameraX` para manejo de cámara en tiempo real.
  - Integración con `Google ML Kit Barcode Scanning` para detección rápida de productos.

- **🗄️ Base de datos relacional local**
  - Implementada con `SQLite`, diseñada manualmente mediante clases Java.
  - Manejo de entidades como alimentos, comidas, usuarios y actividad física.

- **📊 Visualización de datos**
  - Gráficas generadas con `MPAndroidChart` para mostrar el progreso semanal de calorías y actividad.

---

## 🛠️ Tecnologías Utilizadas

| Componente            | Tecnología                              |
|----------------------|------------------------------------------|
| Lenguaje             | Java                                     |
| Entorno              | Android Studio                           |
| Base de datos local  | SQLite (implementada manualmente)        |
| Interfaz de usuario  | Android Jetpack (ViewPager2, Material Components) |
| Cámara               | CameraX (v1.3.0)                          |
| Escaneo de códigos   | ML Kit Barcode Scanning (v17.2 / v17.3.0)|
| Sensores             | SensorManager (nativo de Android)        |
| Gráficas             | MPAndroidChart                           |
