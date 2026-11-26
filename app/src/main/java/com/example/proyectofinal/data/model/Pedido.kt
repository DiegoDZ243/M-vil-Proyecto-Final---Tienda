package com.example.proyectofinal.data.model

/**
 * Pedido guardado en Firebase (nodo "pedidos").
 * La fecha se guarda como timestamp en milisegundos (Long) porque Realtime Database
 * no serializa bien java.util.Date.
 */
data class Pedido(
    val id: Int = 0,
    val fecha: Long = System.currentTimeMillis(),
    val estado: String = EstadoPedido.PENDIENTE,
    val usuarioId: String = "",
    val total: Double = 0.0,
    val direccion: String = "",
    val metodoPago: String = "",
    val items: List<ProductoCarrito> = emptyList()
)

object EstadoPedido {
    const val PENDIENTE = "Pendiente"
    const val FINALIZADO = "Finalizado"
    const val CANCELADO = "Cancelado"
}

object MetodoPago {
    const val TARJETA = "Tarjeta de crédito/débito"
    const val EFECTIVO = "Efectivo al recibir"
    const val TRANSFERENCIA = "Transferencia bancaria"
    val opciones = listOf(TARJETA, EFECTIVO, TRANSFERENCIA)
}

/** RF-14 / RF-15: ventana de cancelación de 6 horas. */
const val HORAS_LIMITE_CANCELACION = 6
private const val MILIS_POR_HORA = 60L * 60L * 1000L

fun Pedido.estaDentroDeVentanaCancelacion(ahora: Long = System.currentTimeMillis()): Boolean =
    ahora - fecha <= HORAS_LIMITE_CANCELACION * MILIS_POR_HORA

fun Pedido.puedeCancelarse(ahora: Long = System.currentTimeMillis()): Boolean =
    estado == EstadoPedido.PENDIENTE && estaDentroDeVentanaCancelacion(ahora)
