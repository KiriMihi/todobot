ALTER TABLE Task ADD Ordering Int
ALTER TABLE Task ADD UserID Int

Create Table User (
ID BIGSERIAL Primary key
UserID Int not null,
FirstName varchar not null);