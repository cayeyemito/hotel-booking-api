# API de reservas

Una API REST para reservar habitaciones de hotel, hecha con Java 21 y Spring Boot. Es un
dominio que conocí trabajando en EY, ahora como proyecto propio: consulta qué habitaciones
están libres entre dos fechas, crea reservas y las cancela.

Lo más interesante está en la base de datos. Dos reservas confirmadas de la misma habitación
no pueden solaparse, y eso no lo comprueba el código sino PostgreSQL, con una restricción de
exclusión sobre el rango de fechas (`V1__create_rooms_and_bookings.sql`):

```sql
ALTER TABLE bookings
ADD CONSTRAINT bookings_no_overlap
EXCLUDE USING GIST (
    room_id WITH =,
    daterange(check_in, check_out, '[)') WITH &&
)
WHERE (status = 'CONFIRMED');
```

Así, aunque dos peticiones lleguen a la vez, solo una de las dos reservas entra. El rango
`[)` incluye el día de entrada y no el de salida, de modo que una reserva puede empezar el
mismo día en que acaba la anterior.

## Endpoints

| Método | Ruta | Qué hace |
|---|---|---|
| `GET` | `/rooms` | Habitaciones activas |
| `GET` | `/rooms/available?checkIn=2026-12-01&checkOut=2026-12-04` | Habitaciones libres entre dos fechas |
| `POST` | `/bookings` | Crea una reserva |
| `GET` | `/bookings/{id}` | Consulta una reserva |
| `POST` | `/bookings/{id}/cancel` | Cancela una reserva |

Para crear una reserva:

```json
{
  "roomId": 1,
  "guestName": "Ana García",
  "guestEmail": "ana@example.com",
  "checkIn": "2026-12-01",
  "checkOut": "2026-12-04",
  "guests": 2
}
```

Los errores salen en formato `ProblemDetail` (RFC 9457), con el detalle de cada campo cuando
falla la validación.

## Cómo arrancarla

Hace falta Java 21 y Docker. Spring Boot levanta el PostgreSQL de `compose.yaml` al arrancar
y Flyway crea las tablas y las habitaciones de ejemplo:

```bash
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw verify
```

Los tests de repositorio corren contra un PostgreSQL real con Testcontainers, así que la
restricción de solapes se prueba de verdad, no contra una base de datos en memoria. Los de
servicio usan Mockito y los de controlador, `@WebMvcTest`. El workflow de GitHub Actions
(`.github/workflows/ci.yml`) los ejecuta en cada push y en cada pull request.

## Qué falta

- Seguridad con JWT.
- Imagen Docker de la aplicación y su publicación desde el pipeline.
- Despliegue en Azure Container Apps con Terraform.
