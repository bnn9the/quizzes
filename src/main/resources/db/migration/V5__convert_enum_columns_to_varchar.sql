-- Convert Postgres enum columns to VARCHAR for compatibility with JPA
ALTER TABLE users
    ALTER COLUMN role DROP DEFAULT,
    ALTER COLUMN role TYPE VARCHAR(20) USING role::text,
    ALTER COLUMN role SET DEFAULT 'STUDENT';

ALTER TABLE questions
    ALTER COLUMN question_type DROP DEFAULT,
    ALTER COLUMN question_type TYPE VARCHAR(20) USING question_type::text,
    ALTER COLUMN question_type SET DEFAULT 'SINGLE_CHOICE';

-- Drop legacy enum types if they exist
DROP TYPE IF EXISTS user_role;
DROP TYPE IF EXISTS question_type;
