-- Seed known providers
MERGE INTO providers (name) KEY(name) VALUES ('Agoda');
MERGE INTO providers (name) KEY(name) VALUES ('Booking');
MERGE INTO providers (name) KEY(name) VALUES ('Expedia');

-- Seed Known Hotels
MERGE INTO hotels (id,name) KEY(id,name) VALUES (10984,'Oscar Saigon Hotel');
