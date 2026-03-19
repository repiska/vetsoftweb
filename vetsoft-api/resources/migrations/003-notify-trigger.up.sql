CREATE OR REPLACE FUNCTION notify_price_change()
RETURNS TRIGGER AS $$
BEGIN
  PERFORM pg_notify('price_changes', TG_TABLE_NAME);
  RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

--;;

CREATE TRIGGER price_change_trigger AFTER INSERT OR UPDATE OR DELETE ON license_tiers
  FOR EACH ROW EXECUTE FUNCTION notify_price_change();

--;;

CREATE TRIGGER price_change_trigger AFTER INSERT OR UPDATE OR DELETE ON extra_items
  FOR EACH ROW EXECUTE FUNCTION notify_price_change();

--;;

CREATE TRIGGER price_change_trigger AFTER INSERT OR UPDATE OR DELETE ON modules
  FOR EACH ROW EXECUTE FUNCTION notify_price_change();

--;;

CREATE TRIGGER price_change_trigger AFTER INSERT OR UPDATE OR DELETE ON support_rates
  FOR EACH ROW EXECUTE FUNCTION notify_price_change();

--;;

CREATE TRIGGER price_change_trigger AFTER INSERT OR UPDATE OR DELETE ON services
  FOR EACH ROW EXECUTE FUNCTION notify_price_change();

--;;

CREATE TRIGGER price_change_trigger AFTER INSERT OR UPDATE OR DELETE ON price_metadata
  FOR EACH ROW EXECUTE FUNCTION notify_price_change();
