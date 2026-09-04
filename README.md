# Banco XYZ - Backend for Frontend (Semana 4)

Continuidad del Banco XYZ (Spring Batch de las semanas 1-3). Esta entrega implementa
el patron **Backend for Frontend (BFF)** para tres canales: **web**, **movil** y
**cajero automatico**.

El Batch de la semana 3 sigue en este mismo repositorio (puerto 8081). La nota de
esta semana evalua el BFF. Detalle del Batch: [README-BATCH.md](README-BATCH.md).

## Propuesta tecnica: estrategia elegida

La guia de aprendizaje lista 3 estrategias. Se eligio la **1: backends independientes
por cada tipo de cliente**, con un toque de la **3** (el BFF Web combina cuenta +
movimientos + resumen).

| Estrategia | Por que si / no |
| --- | --- |
| **1. Backends independientes** (elegida) | La actividad pide "cada cliente debera tener su propio Backend". Es la de la clase (RutaExpress: `bff-web` y `bff-movil`). Auth distinta por canal. Codigo organizado por proyecto (criterio 4). |
| 2. Endpoints `/web` y `/mobile` en un solo servicio | Se parece al monolito del material extra (`if` segun el cliente). No cumple "su propio backend". |
| 3. BFF que combina microservicios | Hoy la fuente son los CSV de `bank_legacy_data`, no varios microservicios. El BFF Web si orquesta dos llamadas al core (cuenta + movimientos). |

No se implementa API Gateway: el profesor lo nombro solo como analogia (en Cloud Native se evaluaba el Gateway; aqui se evalua el BFF). El frontend llama a su BFF y el BFF llama al backend interno.

```
App Web     -->  bff-web    :8091  -->  ms-cuentas :8090
App Movil   -->  bff-movil  :8092  -->  ms-cuentas :8090
Cajero      -->  bff-cajero :8093  -->  ms-cuentas :8090
```

`ms-cuentas` es el backend funcional que el profesor pidio para que el BFF tenga de
donde sacar datos. Persiste en **Oracle Autonomous** (no H2). Expone el dato
**completo e igual** para todos. Los BFF recortan.

## Estructura

```
Exp2_S4_lisbeth_bilbao_Grupo16/
├── ms-cuentas/     backend interno (CSV semana_3, Oracle Autonomous)
├── bff-web/        datos completos + resumen
├── bff-movil/      saldo, nombre, 3 ultimos movimientos
├── bff-cajero/     saldo y retiro
└── (raiz)          Spring Batch semana 3, puerto 8081
```

## Personalizacion por canal

| Canal | Token (`X-Canal-Token`) | Que ve |
| --- | --- | --- |
| Web | `token-web-xyz` | cuentaId, nombre, saldo, edad, tipo, historial completo, totales, transacciones diarias |
| Movil | `token-movil-xyz` | id, nombre, saldo y 3 movimientos (sin descripcion ni edad) |
| Cajero | `token-cajero-xyz` | solo saldo, 2 movimientos basicos y POST retiro |

## Como ejecutar

JDK 17+ y Maven. **Cuatro terminales**, en este orden:

```powershell
cd ms-cuentas
mvn spring-boot:run
```

```powershell
cd bff-web
mvn spring-boot:run
```

```powershell
cd bff-movil
mvn spring-boot:run
```

```powershell
cd bff-cajero
mvn spring-boot:run
```

### Web (datos ricos)

```powershell
$h = @{ "X-Canal-Token" = "token-web-xyz" }
Invoke-RestMethod -Headers $h -Uri http://localhost:8091/bff/web/cuentas
Invoke-RestMethod -Headers $h -Uri http://localhost:8091/bff/web/cuentas/101
Invoke-RestMethod -Headers $h -Uri http://localhost:8091/bff/web/transacciones
```

### Movil (liviano)

```powershell
$h = @{ "X-Canal-Token" = "token-movil-xyz" }
Invoke-RestMethod -Headers $h -Uri http://localhost:8092/bff/movil/cuentas
Invoke-RestMethod -Headers $h -Uri http://localhost:8092/bff/movil/cuentas/101
```

### Cajero (saldo y retiro)

```powershell
$h = @{ "X-Canal-Token" = "token-cajero-xyz" }
Invoke-RestMethod -Headers $h -Uri http://localhost:8093/bff/cajero/saldo/101
Invoke-RestMethod -Headers $h -Uri http://localhost:8093/bff/cajero/cuentas/101/movimientos
Invoke-RestMethod -Method POST -Headers $h -ContentType "application/json" `
  -Uri http://localhost:8093/bff/cajero/cuentas/101/retiro `
  -Body '{"monto":100}'
```

### Auth (401 si el token es de otro canal)

```powershell
Invoke-RestMethod -Headers @{ "X-Canal-Token" = "token-web-xyz" } `
  -Uri http://localhost:8093/bff/cajero/saldo/101
```

## Datos

CSV oficiales de [bank_legacy_data](https://github.com/KariVillagran/bank_legacy_data)
`data/semana_3`. El core los persiste en **Oracle Autonomous** (misma wallet de la
semana 3) y descarta filas sucias (tipos invalidos, montos vacios, edades fuera
de rango) para entregar datos consistentes a los tres BFF. No se usa H2.

## Version

- Java 17, Spring Boot 3.3.5
- Oracle Autonomous (wallet `Wallet_miQuintaBD`)
- Entrega individual
