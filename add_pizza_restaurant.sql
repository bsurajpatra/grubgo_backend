-- SQL script to add a restaurant for pizza@example.com user (ID: 18)
INSERT INTO restaurants (name, address, phone, cuisine_type, opening_hours, owner_id, rating)
VALUES ('Pizza Palace', '123 Pizza St', '555-0101', 'Italian', '10:00-22:00', 18, 4.5);
 
-- Verify the insertion
SELECT * FROM restaurants WHERE owner_id = 18; 