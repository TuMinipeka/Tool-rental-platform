const documentMetadata = [
  ['Versión del documento', '0.4-draft'],
  ['Fecha de publicación', '20 de octubre de 2026'],
  ['Entrada en vigencia', '20 de octubre de 2026'],
  ['Operador', 'Tool Rental Platform'],
  ['Identificación tributaria', 'NIT 900.000.000-0'],
  ['Dirección', 'Calle 00 # 00-00, Bucaramanga, Santander'],
  ['Correo de atención', 'soporte@toolrental.local'],
  ['Teléfono de atención', '+57 300 000 0000'],
  ['Jurisdicción', 'Colombia'],
]

function TermsAndConditionsPage() {
  return (
    <main className="legal-page page-shell page-top">
      <header className="legal-hero">
        <p className="legal-eyebrow">Documento legal · Simulación académica</p>
        <h1>Términos y Condiciones</h1>
        <p>
          Este borrador regula el uso académico de Tool Rental. No está aprobado para un
          lanzamiento comercial ni reemplaza una revisión legal o tributaria colombiana.
        </p>
        <div className="legal-status" role="status">
          <strong>Borrador 0.4</strong>
          <span>No procesa dinero real ni emite facturas válidas ante la DIAN.</span>
        </div>
      </header>

      <div className="legal-layout">
        <aside className="legal-metadata" aria-label="Datos del documento">
          <h2>Datos del documento</h2>
          <dl>
            {documentMetadata.map(([label, value]) => (
              <div key={label}>
                <dt>{label}</dt>
                <dd>{value}</dd>
              </div>
            ))}
          </dl>
        </aside>

        <article className="legal-content">
          <section>
            <h2>1. Alcance y aceptación</h2>
            <p>
              Estos términos regulan el acceso y uso de Tool Rental por clientes, proveedores y
              administradores, incluyendo catálogo, reservas, alquileres, entregas, devoluciones,
              protección opcional por daños, pagos, reembolsos y facturas simuladas.
            </p>
            <p>
              El ambiente actual es una simulación universitaria. No procesa dinero real, no emite
              facturas válidas ante la DIAN, no vende seguros ni implica afiliación con PayPal,
              Visa o Mastercard. Toda aceptación debe ser explícita: el silencio, la inactividad o
              una casilla preseleccionada no constituyen aceptación.
            </p>
          </section>

          <section>
            <h2>2. Definiciones</h2>
            <ul>
              <li><strong>Plataforma:</strong> la aplicación Tool Rental.</li>
              <li><strong>Cliente:</strong> quien solicita y recibe el alquiler.</li>
              <li><strong>Proveedor:</strong> quien publica y alquila herramientas.</li>
              <li><strong>Unidad:</strong> herramienta o equipo físico individualmente identificado.</li>
              <li><strong>Reserva:</strong> solicitud para unidades y fechas determinadas.</li>
              <li><strong>Alquiler:</strong> periodo entre la entrega y la devolución aceptada.</li>
              <li>
                <strong>Plan de Protección:</strong> protección contractual opcional; no es seguro
                ni póliza.
              </li>
              <li>
                <strong>Simulación:</strong> reproducción académica de un flujo comercial sin mover
                dinero real.
              </li>
            </ul>
          </section>

          <section>
            <h2>3. Cuentas y roles</h2>
            <p>
              Los usuarios deben suministrar información correcta, proteger sus credenciales y
              actuar solo con los permisos asignados. Los proveedores responden por la exactitud,
              condición, disponibilidad legal y publicación lícita de sus herramientas.
            </p>
            <p>
              La plataforma puede suspender una cuenta para proteger usuarios, investigar usos
              indebidos, cumplir la ley o preservar la seguridad, manteniendo el canal de
              reclamación que corresponda.
            </p>
          </section>

          <section>
            <h2>4. Catálogo y disponibilidad</h2>
            <p>
              Cada oferta debe identificar características esenciales, tarifa, moneda, proveedor,
              disponibilidad, entrega y restricciones. Las imágenes no sustituyen el registro de
              condición de una unidad específica. La disponibilidad previa es informativa y se
              verifica nuevamente al confirmar la reserva.
            </p>
          </section>

          <section>
            <h2>5. Ciclo de reserva y alquiler</h2>
            <p>
              Antes del resumen final, el cliente elige herramienta, fechas, entrega y, si lo
              desea, el Plan de Protección. La entrega y la devolución registran unidad, fecha,
              condición, accesorios y evidencias disponibles.
            </p>
            <p>
              La política académica permite cancelar una solicitud pendiente sin cargo. Una
              reserva confirmada cancelada con al menos 48 horas genera reembolso completo; con
              menos anticipación aplica un cargo del 25 %. La inasistencia declarada dos horas
              después de la hora programada aplica un cargo del 50 %. Un uso comercial requiere
              revisión legal previa de estas reglas.
            </p>
          </section>

          <section>
            <h2>6. Precios, IVA y facturas</h2>
            <p>
              Los precios se muestran en pesos colombianos salvo indicación expresa. El resumen
              discrimina alquiler, entrega, protección opcional, descuentos, base gravable, IVA,
              ajustes y total. La referencia general de IVA es 19 %, pero la base aplicable al
              arrendamiento de bienes muebles debe validarse profesionalmente antes de operar.
            </p>
            <p>
              Todo documento actual debe indicar: “Simulación académica - documento no válido
              como factura ante la DIAN”.
            </p>
          </section>

          <section>
            <h2>7. Pagos simulados</h2>
            <p>
              El MVP usa una pasarela ficticia: el cliente elige aprobar o rechazar una operación
              académica por el total de la reserva. No hay cobros reales ni conexión con bancos,
              tarjetas o PayPal. El servidor valida importe, propiedad, disponibilidad y vencimiento;
              una aprobación repetida no duplica el comprobante. Los ajustes se registran aparte.
              No se solicitan números de tarjeta, CVV ni contraseñas bancarias.
            </p>
          </section>

          <section>
            <h2>8. Plan Opcional de Protección por Daños</h2>
            <p>
              El plan es opcional, no viene seleccionado y no es un seguro. Precio, deducible,
              límite, eventos cubiertos, exclusiones y vigencia deben aparecer antes de aceptarlo.
              No existe un porcentaje legal universal para esta protección.
            </p>
            <p>
              Las exclusiones informadas pueden comprender daño intencional, fraude, pérdida no
              reportada, uso prohibido, transferencia no autorizada o reparaciones no aprobadas.
              La protección no elimina el derecho a revisar y disputar una valoración.
            </p>
          </section>

          <section>
            <h2>9. Daños, pérdida total y cargos adicionales</h2>
            <p>
              Todo cargo exige registro de devolución, diferencia frente a la salida, evidencias y
              valoración discriminada. Sin protección, la reparación documentada no puede superar
              el valor documentado de reposición. Con protección, aplican el deducible, el límite y
              las exclusiones aceptadas. Una misma pérdida no puede cobrarse dos veces.
            </p>
          </section>

          <section>
            <h2>10. Cancelación, retracto, reembolsos y reversión</h2>
            <p>
              Las reglas contractuales no eliminan derechos obligatorios del consumidor. Cuando
              corresponda, el retracto se tramita según el artículo 47 de la Ley 1480 de 2011 y
              conserva una decisión humana documentada. Antes de operar se debe revisar también la
              Ley 2439 de 2024.
            </p>
            <p>
              La reversión de pagos electrónicos procede únicamente en los casos y mediante el
              procedimiento legal aplicable. Un reembolso en estado pendiente es una obligación
              operativa, no prueba de devolución al consumidor.
            </p>
          </section>

          <section>
            <h2>11. Datos personales y comunicaciones</h2>
            <p>
              El tratamiento debe seguir la política publicada y la autorización del titular. La
              aceptación de estos términos, la autorización de datos, el mercadeo opcional y el Plan
              de Protección son decisiones separadas. Antes de recopilar datos reales se deben
              definir responsable, finalidades, retención, transferencias y canales de reclamo.
            </p>
          </section>

          <section>
            <h2>12. Uso aceptable y seguridad</h2>
            <p>
              Se prohíben fraude, suplantación, publicaciones ilícitas, interferencia con la
              disponibilidad, ataques o accesos no autorizados. Las herramientas deben usarse con
              fines lícitos y conforme a instrucciones y requisitos de seguridad.
            </p>
          </section>

          <section>
            <h2>13. Propiedad intelectual y nombres de terceros</h2>
            <p>
              El software, el contenido original y la marca se rigen por sus licencias y derechos.
              PayPal, Visa y Mastercard pertenecen a sus titulares; mencionarlas en la simulación
              no implica patrocinio, certificación ni integración.
            </p>
          </section>

          <section>
            <h2>14. Disponibilidad y responsabilidad</h2>
            <p>
              El proyecto académico puede modificarse, reiniciarse o quedar indisponible y no
              ofrece un nivel de servicio de producción. Nada excluye responsabilidades o derechos
              del consumidor que la ley colombiana no permita excluir.
            </p>
          </section>

          <section>
            <h2>15. Reclamos, disputas y autoridad</h2>
            <p>
              Los canales definitivos de atención deben publicarse antes de operar. Los consumidores
              pueden acudir a la{' '}
              <a href="https://www.sic.gov.co/">Superintendencia de Industria y Comercio</a>.
            </p>
          </section>

          <section>
            <h2>16. Cambios e historial</h2>
            <p>
              Cada cambio material crea una nueva versión. Ningún cambio modifica de forma
              retroactiva precios, protección u otras condiciones ya aceptadas.
            </p>
            <div className="legal-version">
              <strong>0.4-draft</strong>
              <span>Actualización de metadatos del documento</span>
            </div>
          </section>

          <section className="legal-references">
            <h2>Referencias oficiales</h2>
            <ul>
              <li><a href="https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=44306">Ley 1480 de 2011</a></li>
              <li><a href="https://www1.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=69037">Decreto 587 de 2016</a></li>
              <li><a href="https://sedeelectronica.sic.gov.co/transparencia/normativa/ley-estatutaria-1581-de-2012">Ley 1581 de 2012</a></li>
              <li><a href="https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=4276">Ley 527 de 1999</a></li>
              <li><a href="https://normograma.dian.gov.co/dian/compilacion/docs/pdf/decreto_1625_2016.pdf">Decreto 1625 de 2016</a></li>
            </ul>
          </section>
        </article>
      </div>
    </main>
  )
}

export default TermsAndConditionsPage
