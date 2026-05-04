# Wildlife Explorer — use case specifications

Actors use standard UML notation: the **End User** is the human operator of the desktop application. There is **no** domain object representing application users in the current codebase.

---

## UC-01 — Search Trails

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-01 |
| **Name** | Search Trails |
| **Overview** | The End User enters optional search text (or leaves it blank) and refreshes the trail list. The application queries persisted trails and displays matching results so a trail can be selected for further actions. |
| **Related use cases** | **Includes** UC-02 View Trail Information (search results feed opening a trail). |
| **Actors** | End User |

---

## UC-02 — View Trail Information

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-02 |
| **Name** | View Trail Information (trail, wildlife, images) |
| **Overview** | After choosing a trail from the list, the End User opens its detail view. The application loads the trail record (including linked wildlife and pictures) and displays metadata, images, and wildlife information. |
| **Related use cases** | **Included by** UC-01 Search Trails; UC-04 Rate Existing Trail; UC-05 Edit Existing Trail Information; UC-06 Add Wildlife to Trail; UC-07 Remove Wildlife from Trail (each concrete interaction assumes or leads through viewing context). |
| **Actors** | End User |

---

## UC-03 — Add New Trail

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-03 |
| **Name** | Add New Trail |
| **Overview** | The End User creates a new trail by supplying required attributes. The application assigns an identifier, stores the trail, persists data to disk, and updates the list. Optional trail pictures may be added later from the trail detail flow. |
| **Related use cases** | **Includes** UC-09 Provide Trail Name; UC-10 Provide Trail Length; UC-11 Provide Elevation Changes; UC-12 Provide Trail Pictures (optional). |
| **Actors** | End User |

---

## UC-04 — Rate Existing Trail

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-04 |
| **Name** | Rate Existing Trail (score 0–10) |
| **Overview** | From an open trail, the End User submits a numeric rating. The application updates the trail’s aggregate rating and persists the change. |
| **Related use cases** | **Includes** UC-02 View Trail Information. |
| **Actors** | End User |

---

## UC-05 — Edit Existing Trail Information

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-05 |
| **Name** | Edit Existing Trail Information |
| **Overview** | The End User opens a trail, edits fields such as name, length, or elevation, and saves. The application updates the in-memory model and writes changes to persistent storage. |
| **Related use cases** | **Includes** UC-02 View Trail Information. |
| **Actors** | End User |

---

## UC-06 — Add Wildlife to Trail

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-06 |
| **Name** | Add Wildlife to Trail (new species or from catalog) |
| **Overview** | For an open trail, the End User either attaches an existing catalog species or defines a new species with description and optional images. The application updates trail–wildlife links and the global wildlife catalog as needed, then saves. |
| **Related use cases** | **Includes** UC-02 View Trail Information; UC-13 Provide Wildlife Name; UC-14 Provide Wildlife Description; UC-15 Provide Encounter Notes; UC-16 Provide Wildlife Pictures (optional). *Note:* When attaching from the catalog, not all wildlife detail sub-use cases need separate data entry. |
| **Actors** | End User |

---

## UC-07 — Remove Wildlife from Trail

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-07 |
| **Name** | Remove Wildlife from Trail |
| **Overview** | The End User removes a species association from the current trail. The catalog entry remains available for other trails; only the link is removed and persisted. |
| **Related use cases** | **Includes** UC-02 View Trail Information. |
| **Actors** | End User |

---

## UC-08 — Delete Trail

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-08 |
| **Name** | Delete Trail |
| **Overview** | From the trail list, the End User selects a trail and confirms deletion. The application removes the trail from persistent storage and refreshes the list. |
| **Related use cases** | *(None on the diagram; operates from the same list context as search/list.)* |
| **Actors** | End User |

---

## UC-09 — Provide Trail Name

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-09 |
| **Name** | Provide Trail Name |
| **Overview** | Sub-step of adding a trail: capture the trail’s display name. |
| **Related use cases** | **Included by** UC-03 Add New Trail. |
| **Actors** | End User |

---

## UC-10 — Provide Trail Length

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-10 |
| **Name** | Provide Trail Length |
| **Overview** | Sub-step of adding a trail: capture trail length (miles). |
| **Related use cases** | **Included by** UC-03 Add New Trail. |
| **Actors** | End User |

---

## UC-11 — Provide Elevation Changes

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-11 |
| **Name** | Provide Elevation Changes |
| **Overview** | Sub-step of adding a trail: capture elevation change (feet). |
| **Related use cases** | **Included by** UC-03 Add New Trail. |
| **Actors** | End User |

---

## UC-12 — Provide Trail Pictures (optional)

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-12 |
| **Name** | Provide Trail Pictures (optional) |
| **Overview** | Sub-step of adding a trail: optionally attach one or more trail images. |
| **Related use cases** | **Included by** UC-03 Add New Trail. |
| **Actors** | End User |

---

## UC-13 — Provide Wildlife Name

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-13 |
| **Name** | Provide Wildlife Name |
| **Overview** | Sub-step when creating a new species for a trail: enter the species name. |
| **Related use cases** | **Included by** UC-06 Add Wildlife to Trail. |
| **Actors** | End User |

---

## UC-14 — Provide Wildlife Description

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-14 |
| **Name** | Provide Wildlife Description |
| **Overview** | Sub-step when creating a new species: enter descriptive text. |
| **Related use cases** | **Included by** UC-06 Add Wildlife to Trail. |
| **Actors** | End User |

---

## UC-15 — Provide Encounter Notes

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-15 |
| **Name** | Provide Encounter Notes |
| **Overview** | Sub-step when creating a new species: enter notes for the encounter / observation. |
| **Related use cases** | **Included by** UC-06 Add Wildlife to Trail. |
| **Actors** | End User |

---

## UC-16 — Provide Wildlife Pictures (optional)

| Field | Content |
| ----- | ------- |
| **UC reference** | UC-16 |
| **Name** | Provide Wildlife Pictures (optional) |
| **Overview** | Sub-step when creating a new species: optionally attach wildlife images. |
| **Related use cases** | **Included by** UC-06 Add Wildlife to Trail. |
| **Actors** | End User |
