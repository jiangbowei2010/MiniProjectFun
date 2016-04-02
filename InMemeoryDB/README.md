# In memory DB
The idea is from Thumbtack OA. This is a small and simple in-memory DataBase. 
It can be run on command line and support below command:

SET: add a key-value pair to data base
GET: return the value by given key
UNSET: go back to previous value before SET command
NUMEQUALSTO: return number of keys that have the value

example:
SET a 10 	
SET b 10
NUMEQUALSTO 10
// 2
GET a
// 10
SET a 20
GET a
// 20
GET b
// 10
NUMEQUALSTO 10
// 1
NUMEQUALSTO 20
// 1
UNSET a
GET a
// 10

It also support transactions

BEGIN: start a new transaction block
ROLLBACK: drop the change and go back to the status of previous BEGIN
	  return NO TRANSACTION if not in transaction block
COMMIT: make all the change permanant stored in database

transactions can be cascade.
example:

BEGIN
SET a 10
GET a
// 10
BEGIN
SET a 20
GET a
// 20
ROLLBACK
GET a
// 10
COMMIT
GET a
// 10
ROLLBACK
// NO TRANSACTION




