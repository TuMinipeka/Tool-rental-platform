# Conceptual entity–relationship diagram

This drawing represents the **18-entity baseline** in [conceptual-model.md](../conceptual-model.md). Protection, evidence, disputes, refunds, legal-document versions and consent extensions are intentionally outside this first diagram.

## Files

- [Editable Excalidraw scene](conceptual-er-model.excalidraw): native grouped shapes, attributes, relationships and connectors.
- [Vector preview](conceptual-er-model.svg): zoom without losing text quality.
- [PNG preview](conceptual-er-model.png): convenient image preview.
- [Verification record](conceptual-er-model.audit.json): counts, source fingerprint and MCP creation checkpoint.

![Conceptual ER diagram](conceptual-er-model.svg)

## Notation

Rectangles represent business entities, with their attributes listed inside. Yellow diamonds represent relationships. Identifiers use `[ID]`; alternate identifiers use `[unique]`; `?` marks an optional attribute. This is a conceptual ER presentation with attribute lists, not strict Chen notation with one ellipse per attribute and not a physical SQL schema. No SQL column types or foreign keys are included.

Multiplicities are written beside entity endpoints: `1`, `0..1`, `0..N` or `1..N`. Each endpoint counts how many instances of that entity can be associated with **one instance at the opposite endpoint**. For example, each Tool has one Supplier, and one Supplier can offer zero or many Tools.

UserRole and ReservationItem remain associative entities. Customer and Supplier are optional overlapping profiles: one User can have both. A delivery is identified by its ReservationItem, and a return by its delivery; these express the same composite identity as reservation plus equipment unit in the source model. The diagram includes all 19 source relationships, including the separate link from Rental to UnitDelivery.

The lower legend records additional rules: one supplier per reservation, inclusive dates, independent tool images and equipment units, and frozen rates and invoice values. These conditions cannot all be inferred from multiplicity alone. Derived availability and totals are not new stored attributes.

## Editing and MCP provenance

The official Excalidraw MCP server was connected directly and its `read_me` and `create_view` tools were used. Endpoint: `https://mcp.excalidraw.com/mcp`. This does not imply a permanent Codex MCP installation or a public sharing link. The export that uploads the scene to generate a shared excalidraw.com link was not executed because automatic approval review requires explicit authorization for that additional publication.

To edit locally, open Excalidraw and use **Open / Load from file** to select `conceptual-er-model.excalidraw`. The scene includes editable grouped entities and connectors. Keep the native file as the editable source and regenerate the preview after edits.

Validation: 18 entities, 19 relationship diamonds and 38 entity–relationship connectors. The entity names and relationship pairs were compared automatically with the Mermaid diagram in the source document. Layout and text were also inspected in three image crops. No backend source or configuration files were modified.
