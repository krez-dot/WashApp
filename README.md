# WashApp

Laundry Service Management System with Queue Optimization and Scheduling — an
Android app (Kotlin + Firebase) for small to medium-scale laundry shops. See
`chap1`/`chap2`/`chap3` documentation for the full project background, related
literature, and technical specification this scaffold follows.

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- A Firebase project (Blaze or Spark plan) with Authentication, Firestore,
  Realtime Database, and Cloud Messaging enabled

## First-time setup

1. In the [Firebase console](https://console.firebase.google.com/), create a
   project and register an Android app with package name `com.washapp`.
2. Download the generated `google-services.json` and place it at
   `app/google-services.json` (this file is gitignored — each developer/
   environment needs their own).
3. Enable **Email/Password** sign-in under Authentication.
4. Create Firestore (production mode) and publish `firestore.rules` from this
   repo. Create the following collections (seed at least one document in each
   so the admin screens have something to show):
   - `users` — `{ uid, name, email, role: "CUSTOMER" | "ADMINISTRATOR" }`
   - `orders` — see `Order` in `data/model/Order.kt`
   - `machines` — see `Machine` in `data/model/Machine.kt`
   - `pricing` — one document per service type, id = `COLORED` / `NON_COLORED`,
     see `PricingConfig` in `data/model/PricingConfig.kt`
5. Create a **Realtime Database** instance (locked mode) and publish these
   rules — this backs the live stage/queue-position sync on the customer's
   Track Order screen:
   ```json
   {
     "rules": {
       "orderStatus": {
         ".read": "auth != null",
         "$orderId": { ".write": "auth != null" }
       }
     }
   }
   ```
   After creating it, re-download `google-services.json` (it needs to include
   the database URL) and replace `app/google-services.json`.
6. Open the project root in Android Studio and let it sync (it will offer to
   generate the Gradle wrapper jar/scripts if missing).

## Project structure

- `auth/` — login & registration, routes to the customer or admin activity
  based on the signed-in user's `role`
- `customer/` — customer-facing nav graph (`nav_customer.xml`): order
  submission, order tracking
- `admin/` — administrator nav graph (`nav_admin.xml`): order management,
  queue management, machine scheduling, service configuration
- `data/model/` — Firestore-backed data classes
- `data/repository/` — Firestore/Auth access layer
- `data/scheduling/` — rule-based queue optimization, machine scheduling, and
  time estimation logic (`QueueOptimizer`, `MachineScheduler`, `TimeEstimator`)
- `notifications/` — FCM messaging service (stage-transition push alerts)

## Current state

Register, login, order submission, order tracking, and all four admin screens
(orders with stage advancement, queue reordering, machine management, service
pricing config) are wired up end-to-end against Firestore and verified working.
The customer's Track Order screen live-updates via a Realtime Database
listener on `orderStatus/{orderId}` — writes to that path from anywhere
(the admin app, another client, the Firebase console) reflect instantly
without a manual refresh.

Advancing an order to "Washing" auto-assigns it the first available machine
(`MachineScheduler.findAvailableMachine`), marks that machine unavailable, and
releases it back to the pool when the order is marked "Complete" — this is
the "Machines (schedule per machine) – per-machine scheduling and load
assignment" feature from the Laundry Shop Module spec.

Note: `Machine.isAvailable` is annotated with `@get:PropertyName("isAvailable")`
— without it, Firestore's Kotlin mapper silently strips the `is` prefix and
serializes the field as `available`, which doesn't match the literal
`"isAvailable"` key our raw map-based partial updates use, so writes and
reads silently diverge onto two different fields. If you add other boolean
properties named `isXxx` to any model, apply the same annotation.

Not yet built:
- Actual push notification sending (the FCM service in `notifications/` is
  still a stub — no Cloud Function triggers a send on stage transitions;
  skipped for now since it needs the Blaze plan)
- Admin-side live listeners (admin screens still use one-shot fetches plus
  optimistic local updates on the admin's own actions; they won't see another
  admin's changes without navigating away and back)
