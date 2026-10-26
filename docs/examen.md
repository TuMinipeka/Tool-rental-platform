## **Contexto**

Los clientes quieren llevar control de los pagos que han realizado dentro de la plataforma, pero actualmente no existe un módulo de historial.

## **Descripción detallada**

Se debe implementar un módulo donde cada cliente pueda ver todos sus pagos pasados con detalles básicos.

### **Instrucciones**

- Crear un endpoint en el backend (/pagos/cliente/{id}) que devuelva todos los pagos realizados por un cliente, incluyendo:

- Fecha del pago
- Monto pagado
- Método de pago (si aplica)
- Estado (Pagado, Pendiente, Fallido)

- En el frontend, crear una sección “Historial de Pagos” en el panel del cliente.

- Mostrar los resultados en una tabla con columnas: Fecha | Monto | Estado.

- Validar en el backend que solo el cliente logueado pueda acceder a sus propios pagos.

