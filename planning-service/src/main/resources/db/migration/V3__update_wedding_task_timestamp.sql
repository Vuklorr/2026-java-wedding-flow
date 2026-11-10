CREATE OR REPLACE FUNCTION set_wedding_task_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = clock_timestamp();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_wedding_task_updated_at
    BEFORE UPDATE ON wedding_tasks
    FOR EACH ROW
    EXECUTE FUNCTION set_wedding_task_updated_at();