package cl.duoc.bancoxyz.bff.web.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.bff.web.clients.MsCuentasClient;
import cl.duoc.bancoxyz.bff.web.dtos.CuentaWebDetalleDTO;
import cl.duoc.bancoxyz.bff.web.dtos.CuentaWebResumenDTO;
import cl.duoc.bancoxyz.bff.web.dtos.MovimientoWebDTO;
import cl.duoc.bancoxyz.bff.web.dtos.ResumenMovimientosDTO;
import cl.duoc.bancoxyz.bff.web.dtos.core.CuentaCoreDTO;
import cl.duoc.bancoxyz.bff.web.dtos.core.MovimientoCoreDTO;
import cl.duoc.bancoxyz.bff.web.dtos.core.TransaccionCoreDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BffWebService {

    private final MsCuentasClient msCuentasClient;

    public List<CuentaWebResumenDTO> listarCuentas() {
        return msCuentasClient.obtenerCuentas().stream()
                .map(c -> new CuentaWebResumenDTO(c.cuentaId(), c.nombre(), c.saldo(), c.edad(), c.tipo()))
                .toList();
    }

    public CuentaWebDetalleDTO obtenerDetalle(Long cuentaId) {
        CuentaCoreDTO cuenta = msCuentasClient.obtenerCuenta(cuentaId);
        List<MovimientoCoreDTO> movimientos = msCuentasClient.obtenerMovimientos(cuentaId);
        List<MovimientoWebDTO> movimientosWeb = movimientos.stream()
                .map(m -> new MovimientoWebDTO(m.fecha(), m.tipo(), m.monto(), m.descripcion()))
                .toList();
        return new CuentaWebDetalleDTO(
                cuenta.cuentaId(), cuenta.nombre(), cuenta.saldo(), cuenta.edad(), cuenta.tipo(),
                movimientosWeb, resumen(movimientos));
    }

    public List<TransaccionCoreDTO> listarTransacciones() {
        return msCuentasClient.obtenerTransacciones();
    }

    private ResumenMovimientosDTO resumen(List<MovimientoCoreDTO> movimientos) {
        return new ResumenMovimientosDTO(
                sumar(movimientos, "deposito"),
                sumar(movimientos, "retiro"),
                sumar(movimientos, "compra"),
                movimientos.size());
    }

    private BigDecimal sumar(List<MovimientoCoreDTO> movimientos, String tipo) {
        return movimientos.stream()
                .filter(m -> tipo.equals(m.tipo()))
                .map(MovimientoCoreDTO::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
