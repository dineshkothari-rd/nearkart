ALTER TABLE refresh_tokens
    ALTER COLUMN token_hash TYPE varchar(64),
    ALTER COLUMN replaced_by_hash TYPE varchar(64);
