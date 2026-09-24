CREATE TABLE IF NOT EXISTS command_count (
    command VARCHAR(5) PRIMARY KEY,
    total BIGINT NOT NULL DEFAULT 0
);

INSERT INTO command_count (command, total)
SELECT command, 0 FROM (VALUES ('RIGHT'), ('LEFT'), ('FRONT'), ('BACK'), ('OPEN'), ('CLOSE')) AS commands(command)
WHERE NOT EXISTS (SELECT 1 FROM command_count existing WHERE existing.command = commands.command);
