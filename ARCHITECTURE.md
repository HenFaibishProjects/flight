# Flight Booking Architecture

The system allows users to search flights, view available seats, create bookings, and receive booking confirmation.

The main goal is to:
- search flights quickly
- show the correct seat map
- prevent two users from booking the same seat
- create reliable bookings
- keep the system simple and scalable

## Spring Boot

Handles:
- flight search API
- seat availability API
- booking API
- reservation logic
- booking logic

Why:
- good fit for backend REST APIs
- easy to structure business logic
- easy to scale horizontally

## PostgreSQL

Main database and source of truth.

Stores:
- flights
- aircraft
- bookings
- passengers
- seat reservations

Why:
- bookings require strong consistency
- good support for transactions
- good for relationships between flights, aircraft and bookings

Simple explanation:

"PostgreSQL is the source of truth because booking and seat reservation must be consistent."

## Redis

Used mainly for flight-search cache.

Why:
- flight searches can repeat many times
- very fast reads
- reduces load on PostgreSQL
- supports TTL so cached searches can expire

Simple explanation:

"I use Redis to cache common flight searches and reduce database load."

## Aircraft and Seat Map

Each flight is assigned to an aircraft.

The aircraft defines the seat map.

Example:

Aircraft A320:
- 1A
- 1B
- 1C
- 2A
- 2B
- 2C

The seat itself does not permanently know if it is booked.

Availability depends on the specific flight.

Simple explanation:

"The aircraft defines which seats exist. The flight reservations define which seats are available."

## Seat Reservation

Before creating the final booking, the selected seat should be reserved for a short time.

Why:
- prevents two users from booking the same seat
- gives the customer time to complete the booking

Possible solution:
- reservation with expiration time
- database locking or atomic update
- Redis can also be used for short reservation locks if needed

Simple explanation:

"When a user selects a seat, I temporarily reserve it so another user cannot take it."

## Booking

After reservation succeeds, the system creates a booking.

Booking contains:
- flight
- passenger
- seat
- booking status

Why:
- booking is the final business record
- it must be stored reliably in PostgreSQL

Simple explanation:

"The booking is created only after the seat reservation succeeds."

## Confirmation

After a booking is successfully created, the system sends confirmation.

For the first version this can be synchronous or a simple service call.

For larger scale, it can be asynchronous using a message queue.

Simple explanation:

"Booking is the important transaction. Confirmation can happen after the booking is already stored."

## Why Not Elasticsearch?

For this system, normal flight search is:

- origin
- destination
- date

PostgreSQL with Redis cache is enough.

Elasticsearch would make sense only if search becomes much more complex, for example:

- flexible dates
- many filters
- price ranges
- full-text search
- advanced ranking

Simple explanation:

"I would not add Elasticsearch unless the search requirements justify it."

## Main Search Flow

Client
→ Spring Boot
→ Redis

Cache hit:
→ return flights

Cache miss:
→ PostgreSQL
→ save result in Redis
→ return flights

## Seat Flow

Client
→ Spring Boot
→ Flight
→ Aircraft seat map
→ Existing reservations/bookings
→ Available seats

## Booking Flow

Client
→ Select seat
→ Reserve seat
→ Create booking in PostgreSQL
→ Send confirmation

## Important Concurrency Problem

Two users may try to book the same seat at the same time.

The system must make sure only one booking succeeds.

Possible solution:
- database transaction
- unique constraint
- row locking / atomic update

Simple explanation:

"The main concurrency problem is two users trying to book the same seat. The database must guarantee that only one succeeds."